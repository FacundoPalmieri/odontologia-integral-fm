package com.odontologiaintegralfm.feature.consultation.core.service.impl;


import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentService;
import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationCorrectionRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.enums.ConsultationEventType;
import com.odontologiaintegralfm.feature.consultation.core.enums.ConsultationStatusType;
import com.odontologiaintegralfm.feature.consultation.core.model.*;
import com.odontologiaintegralfm.feature.consultation.core.repository.IConsultationOdontogramHeaderRepository;
import com.odontologiaintegralfm.feature.consultation.core.repository.IConsultationRepository;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.infrastructure.websocket.enums.WebSocketEventType;
import com.odontologiaintegralfm.infrastructure.websocket.service.IWebSocketEventPublisher;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationService;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio que se encarga de gestionar las consultas.
 */
@Service
public class ConsultationService implements IConsultationService {


    private final IAppointmentService appointmentService;
    private final IConsultationRepository consultationRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final IWebSocketEventPublisher webSocketEventPublisher;
    private final MessageSource messageSource;
    private final ConsultationHistoryService consultationHistoryService;
    private final IConsultationOdontogramHeaderRepository consultationOdontogramHeaderRepository;
    private final ConsultationEventService consultationEventService;

    public ConsultationService(IAppointmentService appointmentService,
                               IConsultationRepository consultationRepository,
                               AuthenticatedUserService authenticatedUserService,
                               IWebSocketEventPublisher webSocketEventPublisher,
                               @Qualifier("messageSource") MessageSource messageSource, ConsultationHistoryService consultationHistoryService,
                               IConsultationOdontogramHeaderRepository consultationOdontogramHeaderRepository, ConsultationEventService consultationEventService) {
        this.appointmentService = appointmentService;
        this.consultationRepository = consultationRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.webSocketEventPublisher = webSocketEventPublisher;
        this.messageSource = messageSource;
        this.consultationHistoryService = consultationHistoryService;
        this.consultationOdontogramHeaderRepository = consultationOdontogramHeaderRepository;
        this.consultationEventService = consultationEventService;
    }

    /**
     * Crea una nueva consulta a partir de un turno existente.
     * <p>
     * Este método implementa el flujo principal de admisión del paciente:
     * <ul>
     *     <li>Valida que el turno exista.</li>
     *     <li>Verifica que el turno corresponda al día actual.</li>
     *     <li>Construye una nueva {@link Consultation} en estado {@code WAITING_ROOM} utilizando factory.</li>
     *     <li>Persiste la consulta recién creada.</li>
     *     <li>Registra la entrada inicial en el historial de estados mediante {@link ConsultationHistory#build}.</li>
     *     <li>Retorna información relevante al cliente mediante un DTO simplificado.</li>
     * </ul>
     *
     * Además, genera un log estructurado mediante la anotación {@link LogAction},
     * permitiendo auditoría y trazabilidad del evento.
     *
     * @param idAppointment ID del turno sobre el cual se crea la consulta.
     * @return {@link Response} conteniendo un {@link ConsultationResponseDTO}
     * con la información básica de la consulta recién creada.
     * @throws ConflictException si el turno no corresponde al día de la fecha.
     */

    @Override
    @LogAction(
            value = "consultationService.logAction.create.ok",
            args = {"#idAppointment", "#result.data.patientName", "#result.data.dentistName"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO

    )
    @Transactional
    public Response<ConsultationResponseDTO> create(Long idAppointment) {

        //Buscamos y validamos la existencia del turno.
        Appointment appointment = appointmentService.getById(idAppointment);

        //Validamos que el turno corresponde al día de la fecha.
        if (!appointment.getDate().toLocalDate().equals(LocalDate.now())) {
            throw new ConflictException("exception.appointmentNotEqualsNow.user", null, "exception.appointmentNotEqualsNow.log", new Object[]{idAppointment, appointment.getDate(), "ConsultationService", "create"}, LogLevel.ERROR);
        }

        //Creamos la consulta
        Consultation consultation  = Consultation.build(appointment);

        //Campos auditoría
        consultation.setEnabled(true);
        consultation.setCreatedAt(LocalDateTime.now());
        consultation.setCreatedBy(authenticatedUserService.getAuthenticatedUser());

        //Actualiza la consulta + crea historial + envía webSocket.
        //Si bien el estado es el mismo que al crear, se reutiliza para creár historial y webSocket
        ConsultationResponseDTO consultationResponseDTO =  changeStatus(consultation, consultation.getStatus());


        return new Response<>(
                true,
                messageSource.getMessage("consultationServices.create.ok", null, LocaleContextHolder.getLocale()),
                consultationResponseDTO
        );
    }

    /**
     * Recupera una consulta por su ID, si no existe arroja NotFound exception.
     *
     * @param id : id de la consulta.
     */
    @Override
    public Consultation getById(Long id) {
        try{
            return consultationRepository.findById(id)
                    .orElseThrow(()->new NotFoundException("exception.consultation.notFound.user", null, "exception.consultation.notFound.log",new Object[]{id, "ConsultationService","getById"}, LogLevel.ERROR));
        }catch(CannotCreateTransactionException | DataAccessException e){
            throw new DataBaseException(e, "ConsultationService", id, null, "getById");
        }
    }







    /**
     * Actualiza el estado de una consulta odontológica aplicando las reglas de negocio correspondientes.
     * <p>
     * Reglas principales:
     * <ul>
     *   <li>No se permite modificar una consulta que ya se encuentre finalizada.</li>
     *   <li>Si la actualización no es una corrección, el nuevo estado debe respetar la progresión
     *       válida de estados definida para la consulta.</li>
     *   <li>Si la actualización corresponde a una corrección, se registra un evento de corrección
     *       asociado a la consulta, incluyendo el usuario autenticado.</li>
     * </ul>
     * <p>
     * La operación es transaccional y actualiza información de auditoría
     * (usuario y fecha de última modificación).
     *
     * @param idConsultation identificador único de la consulta a actualizar
     * @return {@link Response} con la información actualizada de la consulta
     *
     * @throws ConflictException si la consulta ya se encuentra finalizada o si el cambio
     *                           de estado no es válido según las reglas de negocio
     * @throws DataBaseException si ocurre un error al persistir los cambios en la base de datos
     */
    @Override
    @Transactional
    @LogAction(
            value = "consultationService.logAction.update.ok",
            args = {"#idConsultation", "#result.data.patientName", "#result.data.dentistName", "#result.data.consultationStatus"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO

    )
    public Response<ConsultationResponseDTO> updateStatus(Long idConsultation) {

        // Recuperamos la consulta.
        Consultation consultation = getById(idConsultation);

        //Validamos que la consulta no se encuentre finalizada.
        if (consultation.getStatus() == ConsultationStatusType.FINISHED) {
            throw new ConflictException("exception.consultationService.finished.user", null, "exception.consultationService.finished.log", new Object[]{idConsultation, consultation.getStatus().toString(), "ConsultationService", "update"}, LogLevel.ERROR);
        }

        //Actualiza la consulta + crea historial + envía webSocket.
        ConsultationResponseDTO consultationResponseDTO = changeStatus(consultation, consultation.getStatus().next());


        return new Response<>(
                true,
                messageSource.getMessage("consultationService.update.ok", null, LocaleContextHolder.getLocale()),
                consultationResponseDTO
        );

    }




    /**
     * Elimina una consulta.
     * Solo puede ocurrir si el estado es WAITING_ROOM
     * Es decir, se recepciona por error a un paciente que no se presentó.
     *
     * @param idConsultation : id Consulta
     * @param observation : Observación
     */

    @Override
    @Transactional
    @LogAction(
            value = "consultationService.logAction.disabled.ok",
            args = {"#idConsultation", "#observationCorrection"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO

    )
    public Response<Void> disabled(Long idConsultation, String observation) {
        // Recuperamos la consulta.
        Consultation consultation = getById(idConsultation);


        //Si la consulta tiene otro estado, no puede cancelarse.
        if(consultation.getStatus() != ConsultationStatusType.WAITING_ROOM ){
            throw new ConflictException("exception.consultationService.delete.user", null, "exception.consultationService.delete.log", new Object[]{idConsultation, consultation.getStatus().toString(), "ConsultationService", "delete"}, LogLevel.ERROR);
        }

        //Se verifica por las dudas que tampoco cuente con un odontograma.
        List<ConsultationOdontogramHeader> consultationOdontogramList = consultationOdontogramHeaderRepository.findAllByConsultationId(consultation.getId());
        if(!consultationOdontogramList.isEmpty()){
            throw new ConflictException("exception.consultationService.odontogram.user", null, "exception.consultationService.odontogram.log", new Object[]{consultation.getId(), consultation.getStatus(),consultation.getStatus(), "ConsultationService", "updateCorrectionStatus"}, LogLevel.ERROR);
        }

        //Elimina la consulta
        consultation.setEnabled(false);
        consultation.setDisabledAt(LocalDateTime.now());
        consultation.setDisabledBy(authenticatedUserService.getAuthenticatedUser());
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
                ConsultationResponseDTO.build(consultation)
        );


        return new Response<>(
                true,
                messageSource.getMessage("consultationService.disabled.ok", null, LocaleContextHolder.getLocale()),
                null
        );

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

    @Override
    @Transactional
    @LogAction(
            value = "consultationService.logAction.updateCorrectionStatus.ok",
            args = {"#idConsultation", "#result.data.patientName", "#result.data.dentistName", "#result.data.consultationStatus"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO

    )
    public Response<ConsultationResponseDTO> updateCorrectionStatus(Long idConsultation, ConsultationCorrectionRequestDTO correction) {

        // Recuperamos la consulta.
        Consultation consultation = getById(idConsultation);

        //Validamos que la consulta no se encuentre finalizada.
        if (consultation.getStatus() == ConsultationStatusType.FINISHED) {
            throw new ConflictException("exception.consultationService.finished.user", null, "exception.consultationService.finished.log", new Object[]{idConsultation, consultation.getStatus().toString(), "ConsultationService", "updateCorrectionStatus"}, LogLevel.ERROR);
        }

        //Validamos que la consulta no esté en estado "WAITING_ROOM" Ya que no tiene estado previo.
        if(consultation.getStatus() == ConsultationStatusType.WAITING_ROOM){
            throw new ConflictException("exception.consultationService.previous.user", null, "exception.consultationService.previous.log", new Object[]{consultation.getId(), consultation.getStatus(), "ConsultationService", "updateCorrectionStatus"}, LogLevel.ERROR);

        }

        //Si la consulta ya tiene un odontograma, no puede volver a un estado WAITING_ROOM
        List<ConsultationOdontogramHeader> consultationOdontogramList = consultationOdontogramHeaderRepository.findAllByConsultationId(consultation.getId());
        if(!consultationOdontogramList.isEmpty()){
            throw new ConflictException("exception.consultationService.odontogram.user", null, "exception.consultationService.odontogram.log", new Object[]{consultation.getId(), consultation.getStatus(), "ConsultationService", "updateCorrectionStatus"}, LogLevel.ERROR);
        }


        //Actualiza la consulta + crea historial  + envía webSocket.
        ConsultationResponseDTO consultationResponseDTO = changeStatus(consultation,consultation.getStatus().previous());



        //Crea evento corrección
        ConsultationEvent consultationEvent = ConsultationEvent.build(
                consultation,
                ConsultationEventType.CONSULTATION_CORRECTED,
                correction.observationCorrection()
        );

        //Campos auditoría
        consultationEvent.setEnabled(true);
        consultationEvent.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        consultationEvent.setCreatedAt(LocalDateTime.now());

        consultationEventService.create(consultationEvent);

        return new Response<>(
                true,
                messageSource.getMessage("consultationService.correction.ok",null, LocaleContextHolder.getLocale()),
                consultationResponseDTO

        );
    }


    private ConsultationResponseDTO changeStatus(Consultation consultation, ConsultationStatusType newStatus) {

        //1. Actualiza consulta.
        consultation.setStatus(newStatus);
        consultation.setUpdatedAt(LocalDateTime.now());
        consultation.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());

        Consultation consultationSaved;
        try {
            consultationSaved = consultationRepository.save(consultation);
        } catch (CannotCreateTransactionException | DataAccessException e) {
            throw new DataBaseException(e, "ConsultationService", consultation.getId(), null, "changeStatus");
        }



        //2. Crea historial
        ConsultationHistory consultationHistory =  ConsultationHistory.build(consultationSaved, newStatus);

        //Campos de auditoria.
        consultationHistory.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        consultationHistory.setCreatedAt(LocalDateTime.now());
        consultationHistory.setEnabled(true);

        consultationHistoryService.create(consultationHistory);


        //3. Envío webSocket
       ConsultationResponseDTO consultationResponseDTO = ConsultationResponseDTO.build(consultationSaved);

        webSocketEventPublisher.publish(
                consultationSaved.getStatus().webSocketEvent(),
                consultationResponseDTO
        );


        return consultationResponseDTO;
    }





}
