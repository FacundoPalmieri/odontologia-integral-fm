package com.odontologiaintegralfm.feature.consultation.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.catalogs.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.catalogs.service.interfaces.ITreatmentService;
import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.ToothDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.TreatmentRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.enums.ConsultationStatusType;
import com.odontologiaintegralfm.feature.consultation.core.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationHistory;
import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationOdontogram;
import com.odontologiaintegralfm.feature.consultation.core.repository.IConsultationOdontogramRepository;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationHistoryService;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationService;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationOdontogramService;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.infrastructure.systemparameter.service.interfaces.ISystemParameterService;
import com.odontologiaintegralfm.infrastructure.websocket.enums.WebSocketEventType;
import com.odontologiaintegralfm.infrastructure.websocket.service.IWebSocketEventPublisher;
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
import java.util.stream.Collectors;

import static com.odontologiaintegralfm.infrastructure.systemparameter.enums.SystemParameterKey.ODONTOGRAM_CORRECTIONS;

@Service
public class ConsultationOdontogramService implements IConsultationOdontogramService {

    private final IConsultationService consultationService;
    private final IConsultationOdontogramRepository consultationOdontogramRepository;
    private final ISystemParameterService systemParameterService;
    private final AuthenticatedUserService authenticatedUserService;
    private final ITreatmentService treatmentService;
    private final IWebSocketEventPublisher webSocketEventPublisher;
    private final IConsultationHistoryService consultationHistoryService;
    private final MessageSource messageSource;


    public ConsultationOdontogramService(IConsultationService consultationService,
                                         IConsultationOdontogramRepository consultationOdontogramRepository,
                                         ISystemParameterService systemParameterService,
                                         AuthenticatedUserService authenticatedUserService,
                                         ITreatmentService treatmentService,
                                         IWebSocketEventPublisher webSocketEventPublisher,
                                         IConsultationHistoryService consultationHistoryService,
                                         MessageSource messageSource) {
        this.consultationService = consultationService;
        this.consultationOdontogramRepository = consultationOdontogramRepository;
        this.systemParameterService = systemParameterService;
        this.authenticatedUserService = authenticatedUserService;
        this.treatmentService = treatmentService;
        this.webSocketEventPublisher = webSocketEventPublisher;
        this.consultationHistoryService = consultationHistoryService;
        this.messageSource = messageSource;
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
    public Response<Void> createOdontogram(Long idConsultation, List<ToothDTO> odontogram) {

        //Recupera y valida el estado de la consulta.
        Consultation consultation = validateConsultation(idConsultation);

        //Valida si existe odontogramas activos previos.
        List<ConsultationOdontogram> previous = validateExistingOdontograms(consultation);

        //Deshabilita odontograma anterior.
        disablePreviousOdontograms(previous);

        //Construcción de nuevos odontogramas.
        List<ConsultationOdontogram> newOdontograms = buildOdontograms(consultation, odontogram);

        // Persistimos nuevo odontograma.
        consultationOdontogramRepository.saveAll(newOdontograms);

        //Actualizamos el estado en la consulta a pendiente de pago.
        updateConsultationStatusToPendingPayment(consultation);

        // Registramos evento en la bitácora de la consulta.
        registerConsultationHistory(consultation);

        // Disparamos notificación por WebSocket
        publishAttentionFinishedEvent(consultation);

        return new Response<>(true,messageSource.getMessage("consultationOdontogramService.ok", null, LocaleContextHolder.getLocale()) , null);
    }


    /**
     * Recupera y valida el estado de la consulta.
     * Se valida que no sea "WAITING_ROOM", ya que es el único estado en el que no se puede crear Odontogramas.
     * En otros estados sería posible si se trata de correcciones.
     */
    private Consultation validateConsultation(Long idConsultation) {
        // Recuperamos la consulta.
        Consultation consultation = consultationService.getById(idConsultation);

        // Validamos estado de la consulta
        if (consultation.getStatus() == ConsultationStatusType.WAITING_ROOM) {
            throw new ConflictException("exception.odontogramStatus.user", null, "exception.odontogramStatus.log", new Object[]{idConsultation, consultation.getStatus().toString(), "ConsultationOdontogramService", "create"}, LogLevel.ERROR);

        }

        return consultation;

    }


    /**
     * Recupera odontogramas previos, y en caso de existir valída que no se supere el número de correcciones posibles.
     */
    private List<ConsultationOdontogram> validateExistingOdontograms(Consultation consultation) {
        // Validamos si existe odontograma activo.
        List<ConsultationOdontogram> consultationOdontograms = consultationOdontogramRepository.findAllByConsultationId(consultation.getId());

        // Validamos la consulta tiene más de un odontograma.
        if (consultationOdontograms.size() > 1) {

            //Existe más de un odontograma. Se valida la cantidad de posibles actualizaciones
            int maxCorrection = Integer.parseInt(systemParameterService.getByKey(ODONTOGRAM_CORRECTIONS));

            if (consultationOdontograms.size() > maxCorrection) {
                throw new ConflictException("exception.odontogram.maxCorrection.user", null, "exception.odontogram.maxCorrection.log", new Object[]{consultation.getId(), consultationOdontograms.size(), maxCorrection, "ConsultationOdontogramService", "create"}, LogLevel.ERROR);
            }

        }
        return consultationOdontograms;
    }


    /**
     * Deshabilita los odontogramas anteriores (debería ser solo uno el habilitado)
     */
    private void disablePreviousOdontograms(List<ConsultationOdontogram> odontograms) {

        // Damos de baja lógica al odontograma anterior.
        List<ConsultationOdontogram> odontogramsOld = new ArrayList<>();

        for (ConsultationOdontogram o : odontograms) {
            if (o.isEnabled()) {
                o.setEnabled(false);
                o.setDisabledBy(authenticatedUserService.getAuthenticatedUser());
                o.setDisabledAt(LocalDateTime.now());
                odontogramsOld.add(o);
            }
        }

        //Persistimos la actualización
        consultationOdontogramRepository.saveAll(odontogramsOld);
    }


    /**
     * Construye el nuevo Odontograma.
     */
    private List<ConsultationOdontogram> buildOdontograms(Consultation consultation, List<ToothDTO> odontogram) {


        //Recuperamos todos los tratamientos para evitar múltiples llamadas a la BD dentro del foreach.
        Map<Long, Treatment> treatmentMap = treatmentService.getAll()
                .stream()
                .collect(Collectors.toMap(
                        treatment -> treatment.getId(),
                        treatment -> treatment
                ));


        // Mapeamos a la entidad.
        List<ConsultationOdontogram> consultationOdontogramsNew = new ArrayList<>();

        for (ToothDTO o : odontogram) {

            //Obtenemos el tratamiento.
            for (TreatmentRequestDTO t : o.treatments()) {
                Treatment treatment = treatmentMap.get(t.idTreatment());
                if (treatment == null) {
                    throw new ConflictException("exception.treatment.notFound.user", null, "exception.treatment.notFound.log", new Object[]{t.idTreatment(), "ConsultationOdontogramService", "getAll"}, LogLevel.ERROR);
                }

                ConsultationOdontogram consultationOdontogram = ConsultationOdontogram.build(
                        consultation,
                        o.tooth(),
                        t.toothFace(),
                        treatment
                );

                //Campos de auditoría.
                consultationOdontogram.setCreatedAt(LocalDateTime.now());
                consultationOdontogram.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
                consultationOdontogram.setEnabled(true);

                consultationOdontogramsNew.add(consultationOdontogram);
            }
        }
        return consultationOdontogramsNew;
    }


    /**
     * Actualiza el estado de la consulta a pendiente de pago.
     */
    private void updateConsultationStatusToPendingPayment(Consultation consultation) {
        //Actualizamos el estado en la consulta
        consultationService.updateStatus(
                consultation.getId(),
                new ConsultationUpdateRequestDTO(
                        ConsultationStatusType.PENDING_PAYMENT,
                        null)
        );
    }


    /**
     * Registra historial de consulta y persiste.
     */
    private void registerConsultationHistory(Consultation consultation) {
        // Registramos evento en la bitácora de la consulta.
        ConsultationHistory consultationHistory = ConsultationHistory.build(
                consultation,
                ConsultationStatusType.PENDING_PAYMENT
        );

        //Campos auditoria
        consultationHistory.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        consultationHistory.setCreatedAt(LocalDateTime.now());
        consultationHistory.setEnabled(true);

        consultationHistoryService.create(consultationHistory);
    }


    /**
     * Dispara la notificación WebSocket.
     */
    private void publishAttentionFinishedEvent(Consultation consultation) {
        ConsultationResponseDTO consultationResponseDTO = new ConsultationResponseDTO(
                consultation.getId(),
                consultation.getPatient().getPerson().getLastName() + "," + consultation.getPatient().getPerson().getFirstName(),
                consultation.getDentist().getPerson().getLastName() + "," + consultation.getDentist().getPerson().getFirstName(),
                consultation.getStatus().getLabel()
        );

        // Disparamos notificación por WebSocket
        webSocketEventPublisher.publish(
                WebSocketEventType.ATTENTION_FINISHED,
                consultationResponseDTO
        );
    }



    /**
     * Busca un odontograma con estado "enabled = true"
     *
     * @param idConsultation : id Consulta
     */
    @Override
    public Optional<ConsultationOdontogram> getById(Long idConsultation) {
        try {
            return consultationOdontogramRepository.findById(idConsultation);
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ConsultationOdontogramService", idConsultation, null, "getById");
        }
    }
}
