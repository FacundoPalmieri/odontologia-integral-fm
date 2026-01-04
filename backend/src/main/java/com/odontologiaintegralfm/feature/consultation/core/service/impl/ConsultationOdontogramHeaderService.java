package com.odontologiaintegralfm.feature.consultation.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.catalogs.service.interfaces.ITreatmentService;
import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationOdontogramCorrectionRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationOdontogramCreateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.ToothDTO;
import com.odontologiaintegralfm.feature.consultation.core.enums.ConsultationEventType;
import com.odontologiaintegralfm.feature.consultation.core.enums.ConsultationStatusType;
import com.odontologiaintegralfm.feature.consultation.core.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationEvent;
import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationOdontogramHeader;
import com.odontologiaintegralfm.feature.consultation.core.repository.IConsultationOdontogramHeaderRepository;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationService;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationOdontogramHeaderService;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.infrastructure.systemparameter.service.interfaces.ISystemParameterService;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

import static com.odontologiaintegralfm.infrastructure.systemparameter.enums.SystemParameterKey.ODONTOGRAM_CORRECTIONS;

@Service
public class ConsultationOdontogramHeaderService implements IConsultationOdontogramHeaderService {

    private final IConsultationService consultationService;
    private final IConsultationOdontogramHeaderRepository consultationOdontogramHeaderRepository;
    private final ISystemParameterService systemParameterService;
    private final AuthenticatedUserService authenticatedUserService;
    private final MessageSource messageSource;
    private final ConsultationOdontogramDetailsService consultationOdontogramDetailsService;


    public ConsultationOdontogramHeaderService(IConsultationService consultationService,
                                               IConsultationOdontogramHeaderRepository consultationOdontogramHeaderRepository,
                                               ISystemParameterService systemParameterService,
                                               AuthenticatedUserService authenticatedUserService,
                                               ITreatmentService treatmentService,
                                               MessageSource messageSource, ConsultationOdontogramDetailsService consultationOdontogramDetailsService) {
        this.consultationService = consultationService;
        this.consultationOdontogramHeaderRepository = consultationOdontogramHeaderRepository;
        this.systemParameterService = systemParameterService;
        this.authenticatedUserService = authenticatedUserService;
        this.messageSource = messageSource;
        this.consultationOdontogramDetailsService = consultationOdontogramDetailsService;
    }




    /**
     * Crea un odontograma
     *
     * @param idConsultation : Id de la consulta
     * @param odontogram     : Representación del odontograma.
     */
    @Override
    @Transactional
    @LogAction(
            value = "consultationOdontogramService.logAction.create.ok",
            args = {"#idConsultation"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO

    )
    public Response<Void> create(Long idConsultation, ConsultationOdontogramCreateRequestDTO odontogram) {

        //Valída y procesa la creación del odontograma
        processOdontogram(idConsultation,odontogram.tooths(), odontogram.observation());

        //Actualizamos el estado en la consulta a pendiente de pago + crear historial + dispara webSocket.
        consultationService.updateStatus(idConsultation);
        
        return new Response<>(
                true,
                messageSource.getMessage("consultationOdontogramService.create.ok", null, LocaleContextHolder.getLocale()),
                null);
    }

    /**
     * Actualiza cabecera de odontograma
     *
     * @param idConsultation : Id de la consulta
     * @param requestDTO     : Representación del odontograma + Observación.
     */

    @Override
    @LogAction(
            value = "consultationOdontogramService.logAction.update.ok",
            args = {"#idConsultation"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO

    )
    public Response<Void> update(Long idConsultation, ConsultationOdontogramCorrectionRequestDTO requestDTO) {

        //Valída y procesa la creación del odontograma
        processOdontogram(idConsultation,requestDTO.odontogram().tooths(), requestDTO.odontogram().observation());


        //Crea evento de corrección
        ConsultationEvent.build(
                consultationService.getById(idConsultation),
                ConsultationEventType.ODONTOGRAM_CORRECTED,
                requestDTO.observationCorrection()
        );


        return new Response<>(
                true,
                messageSource.getMessage("consultationOdontogramService.update.ok", null, LocaleContextHolder.getLocale()) ,
                null);

    }


    private void processOdontogram(Long idConsultation, List<ToothDTO> odontogram, String observation){

        //Recupera y valida el estado de la consulta.
        Consultation consultation = validateStateWaitingRoom(idConsultation);

        //Valida si existe odontogramas activos previos.
        List<ConsultationOdontogramHeader> previous = validateExistingOdontograms(consultation);

        //Obtiene el único odontograma activo.
        Optional<ConsultationOdontogramHeader> lastOdontogram = lastOdontogramEnabled(previous);

        //Deshabilita odontograma anterior.
        lastOdontogram.ifPresent(this::disablePreviousOdontograms);

        //Construye y persiste el nuevo odontograma.
        buildOdontograms(consultation, odontogram, observation);

    }


    /**
     * Recupera y valida el estado de la consulta.
     * Se valida que no sea "WAITING_ROOM", ya que es el único estado en el que no se puede crear Odontogramas.
     * En otros estados sería posible si se trata de correcciones.
     */
    private Consultation validateStateWaitingRoom(Long idConsultation) {
        // Recuperamos la consulta.
        Consultation consultation = consultationService.getById(idConsultation);

        // Validamos estado de la consulta
        if (consultation.getStatus() == ConsultationStatusType.WAITING_ROOM) {
            throw new ConflictException("exception.odontogramStatus.user", null, "exception.odontogramStatus.log", new Object[]{idConsultation, consultation.getStatus().toString(), "ConsultationOdontogramHeaderService", "create"}, LogLevel.ERROR);

        }

        return consultation;

    }


    /**
     * Recupera odontogramas previos, y en caso de existir valída que no se supere el número de correcciones posibles.
     */
    private List<ConsultationOdontogramHeader> validateExistingOdontograms(Consultation consultation) {
        // Validamos si existe odontograma activo.
        List<ConsultationOdontogramHeader> consultationOdontograms = consultationOdontogramHeaderRepository.findAllByConsultationId(consultation.getId());

        // Validamos la consulta tiene más de un odontograma.
        if (consultationOdontograms.size() > 1) {

            //Existe más de un odontograma. Se valida la cantidad de posibles actualizaciones
            int maxCorrection = Integer.parseInt(systemParameterService.getByKey(ODONTOGRAM_CORRECTIONS));

            if (consultationOdontograms.size() > maxCorrection) {
                throw new ConflictException("exception.odontogram.maxCorrection.user", null, "exception.odontogram.maxCorrection.log", new Object[]{consultation.getId(), consultationOdontograms.size(), maxCorrection, "ConsultationOdontogramHeaderService", "create"}, LogLevel.ERROR);
            }

        }
        return consultationOdontograms;
    }


    /**
     * Filtra y obtiene el odontograma activo, asociado a la consulta.
     * @param previous: Listado de todos los odontogramas asociados a la consulta (activos como deshabilitados)
     * @return : El único odontograma activo.
     */
    private Optional<ConsultationOdontogramHeader> lastOdontogramEnabled(List<ConsultationOdontogramHeader> previous){
        return previous.stream()
                .filter(ConsultationOdontogramHeader::isEnabled)
                .findFirst();
    }



    private void disablePreviousOdontograms(ConsultationOdontogramHeader lastOdontogram){

        //Deshabilita la cabecera.
        lastOdontogram.setEnabled(false);
        lastOdontogram.setDisabledAt(LocalDateTime.now());
        lastOdontogram.setDisabledBy(authenticatedUserService.getAuthenticatedUser());
        consultationOdontogramHeaderRepository.save(lastOdontogram);

        //Deshabilita los detalles.
        consultationOdontogramDetailsService.disable(lastOdontogram);

    }





    /**
     * Construye el nuevo Odontograma.
     */
    private void buildOdontograms(Consultation consultation, List<ToothDTO> odontogram, String observation) {

        //Construye
        ConsultationOdontogramHeader consultationOdontogramHeader = ConsultationOdontogramHeader.build(consultation, observation);

        //Datos auditoría.
        consultationOdontogramHeader.setEnabled(true);
        consultationOdontogramHeader.setCreatedAt(LocalDateTime.now());
        consultationOdontogramHeader.setCreatedBy(authenticatedUserService.getAuthenticatedUser());

        //Persiste cabecera.
        ConsultationOdontogramHeader consultationOdontogramHeaderSaved = consultationOdontogramHeaderRepository.save(consultationOdontogramHeader);


        //Construye y persiste detalles.
        consultationOdontogramDetailsService.create(consultationOdontogramHeaderSaved,odontogram);
    }







    /**
     * Busca cabecera de odontograma con estado "enabled = true"
     *
     * @param idConsultation : id Consulta
     */
    @Override
    public Optional<ConsultationOdontogramHeader> getById(Long idConsultation) {
        try {
            return consultationOdontogramHeaderRepository.findById(idConsultation);
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ConsultationOdontogramHeaderService", idConsultation, null, "getById");
        }
    }
}
