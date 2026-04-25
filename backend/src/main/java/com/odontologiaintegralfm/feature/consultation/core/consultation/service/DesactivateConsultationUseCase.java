package com.odontologiaintegralfm.feature.consultation.core.consultation.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.core.consultation.mapper.ConsultationMapper;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.ConsultationEvent;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationRepository;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationEventType;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationStatusType;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.infrastructure.websocket.enums.WebSocketEventType;
import com.odontologiaintegralfm.infrastructure.websocket.service.IWebSocketEventPublisher;
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

@Service
public class DesactivateConsultationUseCase {
    private final IConsultationRepository consultationRepository;
    private final ConsultationMapper consultationMapper;
    private final AuthenticatedUserService authenticatedUserService;
    private final MessageSource messageSource;
    private final ConsultationEventService consultationEventService;
    private final IWebSocketEventPublisher webSocketEventPublisher;

    public DesactivateConsultationUseCase(IConsultationRepository consultationRepository,
                                          ConsultationMapper consultationMapper,
                                          AuthenticatedUserService authenticatedUserService,
                                          @Qualifier("messageSource") MessageSource messageSource,
                                          ConsultationEventService consultationEventService,
                                          IWebSocketEventPublisher webSocketEventPublisher
                                        ) {
        this.consultationRepository = consultationRepository;
        this.consultationMapper = consultationMapper;
        this.authenticatedUserService = authenticatedUserService;
        this.messageSource = messageSource;
        this.consultationEventService = consultationEventService;
        this.webSocketEventPublisher = webSocketEventPublisher;

    }


    /**
     * Elimina una consulta.
     * Solo puede ocurrir si el estado es WAITING_ROOM
     * Es decir, se recepciona por error a un paciente que no se presentó.
     *
     * @param idConsultation : id Consulta
     * @param observation : Observación
     */

    @Transactional
    @LogAction(
            value = "desactiveConsultationUseCase.logAction.execute.ok",
            args = {"#idConsultation", "#observationCorrection"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO

    )
    public Response<Void> execute(Long idConsultation, String observation) {
        // Recuperamos la consulta.
        Optional<Consultation> consultationOptional = consultationRepository.findById(idConsultation);
        if(consultationOptional.isEmpty()){
            throw new NotFoundException("exception.consultation.notFound.user", null, "exception.consultation.notFound.log", new Object[]{idConsultation, "desactiveConsultationUseCase", "execute"}, LogLevel.ERROR);
        }
        Consultation consultation = consultationOptional.get();



        //Si la consulta tiene otro estado, no puede cancelarse.
        if(consultation.getStatus() != ConsultationStatusType.WAITING_ROOM ){
            throw new ConflictException("exception.consultation.delete.user", null, "exception.consultation.delete.log", new Object[]{idConsultation, consultation.getStatus().toString(), "desactiveConsultationUseCase", "delete"}, LogLevel.ERROR);
        }

        //Se verifica por las dudas que tampoco cuente con un odontograma.


        //Elimina la consulta
        consultation.disable(authenticatedUserService.getAuthenticatedUser());
        consultationRepository.save(consultation);


        //Crea evento corrección
        ConsultationEvent consultationEvent = ConsultationEvent.build(
                consultation,
                ConsultationEventType.CONSULTATION_CANCELED,
                observation
        );
        consultationEventService.create(consultationEvent);


        //Evento WebSocket
        webSocketEventPublisher.publish(
                WebSocketEventType.CONSULTATION_REMOVED,
                consultationMapper.toDTO(consultation)
        );


        return new Response<>(
                true,
                messageSource.getMessage("desactiveConsultationUseCase.execute.ok", null, LocaleContextHolder.getLocale()),
                null
        );

    }


}
