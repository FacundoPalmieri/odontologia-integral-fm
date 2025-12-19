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
import com.odontologiaintegralfm.infrastructure.systemparameter.service.interfaces.ISystemParameterService;
import com.odontologiaintegralfm.infrastructure.websocket.enums.WebSocketEventType;
import com.odontologiaintegralfm.infrastructure.websocket.service.IWebSocketEventPublisher;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
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



    public ConsultationOdontogramService(IConsultationService consultationService,
                                         IConsultationOdontogramRepository consultationOdontogramRepository,
                                         ISystemParameterService systemParameterService,
                                         AuthenticatedUserService authenticatedUserService,
                                         ITreatmentService treatmentService,
                                         IWebSocketEventPublisher webSocketEventPublisher,
                                         IConsultationHistoryService consultationHistoryService
    ) {
        this.consultationService = consultationService;
        this.consultationOdontogramRepository = consultationOdontogramRepository;
        this.systemParameterService = systemParameterService;
        this.authenticatedUserService = authenticatedUserService;
        this.treatmentService = treatmentService;
        this.webSocketEventPublisher = webSocketEventPublisher;
        this.consultationHistoryService = consultationHistoryService;
    }



    /**
     * Crea un odontograma
     *
     * @param idConsultation : Id de la consulta
     * @param odontogram     : Representación del odontograma.
     */
    @Override
    @Transactional
    public Response<Void> createOdontogram(Long idConsultation, List<ToothDTO> odontogram) {

        // Recuperamos la consulta.
        Consultation consultation = consultationService.getById(idConsultation);

        // Validamos estado de la consulta
        if (consultation.getStatus() == ConsultationStatusType.WAITING_ROOM) {
            throw new ConflictException("exception.odontogramStatus.user", null, "exception.odontogramStatus.log", new Object[]{idConsultation, consultation.getStatus().toString(), "ConsultationOdontogramService", "create"}, LogLevel.ERROR);

        }

        // Validamos si existe odontograma activo.
        List<ConsultationOdontogram> consultationOdontograms = consultationOdontogramRepository.findAllByConsultationId(idConsultation);

        // Validamos la consulta tiene más de un odontograma.
        if (consultationOdontograms.size() > 1) {

            //Existe más de un odontograma. Se valida la cantidad de posibles actualizaciones
            int maxCorrection = Integer.parseInt(systemParameterService.getByKey(ODONTOGRAM_CORRECTIONS));

            if (consultationOdontograms.size() > maxCorrection) {
                throw new ConflictException("exception.odontogram.maxCorrection.user", null, "exception.odontogram.maxCorrection.log", new Object[]{idConsultation, consultationOdontograms.size(), maxCorrection, "ConsultationOdontogramService", "create"}, LogLevel.ERROR);
            }

            // Damos de baja lógica al odontograma anterior.
            List<ConsultationOdontogram> odontogramsOld = new ArrayList<>();
            for (ConsultationOdontogram o : consultationOdontograms) {
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

        // Mapeamos a la entidad.
        List<ConsultationOdontogram> consultationOdontogramsNew = new ArrayList<>();

        //Recuperamos todos los tratamientos para evitar múltiples llamadas a la BD dentro del foreach.
        Map<Long,Treatment> treatmentMap = treatmentService.getAll()
                .stream()
                .collect(Collectors.toMap(
                        treatment -> treatment.getId(),
                        treatment -> treatment
                ));

        for (ToothDTO o : odontogram) {

            //Obtenemos el tratamiento.
            for(TreatmentRequestDTO t : o.treatments()){
                Treatment treatment = treatmentMap.get(t.idTreatment());
                if(treatment == null){
                    throw  new ConflictException("exception.treatment.notFound.user",null,"exception.treatment.notFound.log",new Object[]{t.idTreatment(),"ConsultationOdontogramService","getAll" }, LogLevel.ERROR);
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

        // Persistimos nuevo odontograma.
        consultationOdontogramRepository.saveAll(consultationOdontogramsNew);

        //Actualizamos el estado en la consulta
        consultationService.updateStatus(consultation.getId(), new ConsultationUpdateRequestDTO(ConsultationStatusType.PENDING_PAYMENT, null));


        // Registramos evento en la bitácora de la consulta.
        ConsultationHistory consultationHistory = ConsultationHistory.build(consultation, ConsultationStatusType.PENDING_PAYMENT);

        //Campos auditoria
        consultationHistory.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        consultationHistory.setCreatedAt(LocalDateTime.now());
        consultationHistory.setEnabled(true);

        consultationHistoryService.create(consultationHistory);



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


        return new Response<>(true, "consultationOdontogramService.ok", null);
    }





    /**
     * Busca un odontograma con estado "enabled = true"
     *
     * @param idConsultation : id Consulta
     */
    @Override
    public Optional<ConsultationOdontogram> getById(Long idConsultation) {
        try{
            return consultationOdontogramRepository.findById(idConsultation);
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "ConsultationOdontogramService", idConsultation, null, "getById");
        }
    }
}
