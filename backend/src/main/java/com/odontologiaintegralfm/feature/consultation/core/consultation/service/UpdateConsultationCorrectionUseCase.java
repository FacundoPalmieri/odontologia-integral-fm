package com.odontologiaintegralfm.feature.consultation.core.consultation.service;


import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.ConsultationEvent;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationRepository;
import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationCorrectionRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationEventType;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationStatusType;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


/**
 * Servicio que se encarga de gestionar las consultas.
 */
@Service
public class UpdateConsultationCorrectionUseCase {

    private final IConsultationRepository consultationRepository;
    private final MessageSource messageSource;
    private final ConsultationEventService consultationEventService;
    private final ChangeConsultationStatusUseCase changeConsultationStatusUseCase;

    public UpdateConsultationCorrectionUseCase(IConsultationRepository consultationRepository,
                                               @Qualifier("messageSource") MessageSource messageSource,
                                               ConsultationEventService consultationEventService,
                                               ChangeConsultationStatusUseCase changeConsultationStatusUseCase) {
        this.consultationRepository = consultationRepository;
        this.messageSource = messageSource;
        this.consultationEventService = consultationEventService;
        this.changeConsultationStatusUseCase = changeConsultationStatusUseCase;
    }


    /**
     * Revierte el estado de una consulta al estado anterior.
     *
     * <p>
     * Este método permite realizar una corrección de usuario ante un error operativo
     * (por ejemplo, iniciar una consulta por error o avanzar de estado incorrectamente).
     * La corrección no permite elegir un estado destino, sino que
     * el sistema determina automáticamente el estado previo válido según el flujo
     * de estados definido en {@link ConsultationStatusType}.
     * </p>
     *
     * <h3>Reglas de negocio</h3>
     * <ul>
     *   <li>No se permite corregir una consulta en estado {@code FINISHED}.</li>
     *   <li>No se permite corregir una consulta en estado {@code WAITING_ROOM},ya que no posee un estado previo.</li>
     *   <li>Si la consulta posee odontogramas registrados, no se permite volver al estado {@code WAITING_ROOM}.</li>
     * </ul>
     *
     * <p>
     * En caso de éxito, el método:
     * </p>
     * <ol>
     *   <li>Actualiza el estado de la consulta al estado previo.</li>
     *   <li>Registra un evento de tipo {@code CONSULTATION_CORRECTED} con la
     *       observación ingresada por el usuario.</li>
     *   <li>Devuelve la representación actualizada de la consulta.</li>
     * </ol>
     *
     * @param idConsultation identificador de la consulta a corregir
     * @param correction DTO que contiene la observación asociada a la corrección
     *
     * @return respuesta con la consulta actualizada luego de la corrección
     *
     * @throws ConflictException si la corrección no es válida según las reglas de negocio
     */

    @Transactional
    @LogAction(
            value = "updateConsultationCorrectionUseCase.logAction.execute.ok",
            args = {"#idConsultation", "#result.data.patientName", "#result.data.dentistName", "#result.data.consultationStatus"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO

    )
    public Response<ConsultationResponseDTO> execute(Long idConsultation, ConsultationCorrectionRequestDTO correction) {

        // Recuperamos la consulta.
        Optional<Consultation> consultationOptional = consultationRepository.findById(idConsultation);
        if(consultationOptional.isEmpty()){
            throw new NotFoundException("exception.consultation.notFound.user", null, "exception.consultation.notFound.log", new Object[]{idConsultation, "UpdateConsultationCorrectionUseCase", "execute"}, LogLevel.ERROR);
        }

        Consultation consultation = consultationOptional.get();

        //Validamos que la consulta no se encuentre finalizada.
        if (consultation.getStatus() == ConsultationStatusType.FINISHED) {
            throw new ConflictException("exception.consultation.finished.user", null, "exception.consultation.finished.log", new Object[]{idConsultation, consultation.getStatus().toString(), "updateConsultationCorrectionUseCase", "execute"}, LogLevel.ERROR);
        }

        //Validamos que la consulta no esté en estado "WAITING_ROOM" Ya que no tiene estado previo.
        if(consultation.getStatus() == ConsultationStatusType.WAITING_ROOM){
            throw new ConflictException("exception.consultation.previous.user", null, "exception.consultation.previous.log", new Object[]{consultation.getId(), consultation.getStatus(), "updateConsultationCorrectionUseCase", "execute"}, LogLevel.ERROR);

        }

        //Si la consulta ya tiene un odontograma, no puede volver a un estado WAITING_ROOM



        //Actualiza la consulta + crea historial + envía webSocket.
        ConsultationResponseDTO consultationResponseDTO = changeConsultationStatusUseCase.execute(consultation,consultation.getStatus().previous());



        //Crea evento corrección
        ConsultationEvent consultationEvent = ConsultationEvent.build(
                consultation,
                ConsultationEventType.CONSULTATION_CORRECTED,
                correction.observationCorrection()
        );


        consultationEventService.create(consultationEvent);

        return new Response<>(
                true,
                messageSource.getMessage("updateConsultationCorrectionUseCase.execute.ok",null, LocaleContextHolder.getLocale()),
                consultationResponseDTO

        );
    }







}
