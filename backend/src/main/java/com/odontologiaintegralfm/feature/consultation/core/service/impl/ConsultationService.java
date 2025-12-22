package com.odontologiaintegralfm.feature.consultation.core.service.impl;


import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentService;
import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.enums.ConsultationStatusType;
import com.odontologiaintegralfm.feature.consultation.core.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationEvent;
import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationHistory;
import com.odontologiaintegralfm.feature.consultation.core.repository.IConsultationHistoryRepository;
import com.odontologiaintegralfm.feature.consultation.core.repository.IConsultationRepository;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationEventService;
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

/**
 * Servicio que se encarga de gestionar las consultas.
 */
@Service
public class ConsultationService implements IConsultationService {


    private final IAppointmentService appointmentService;
    private final IConsultationRepository consultationRepository;
    private final IConsultationHistoryRepository consultationHistoryRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final IWebSocketEventPublisher webSocketEventPublisher;
    private final IConsultationEventService consultationEventService;
    private final MessageSource messageSource;

    public ConsultationService(IAppointmentService appointmentService,
                               IConsultationRepository consultationRepository,
                               IConsultationHistoryRepository consultationHistoryRepository,
                               AuthenticatedUserService authenticatedUserService,
                               IWebSocketEventPublisher webSocketEventPublisher,
                               IConsultationEventService consultationEventService,
                               @Qualifier("messageSource") MessageSource messageSource){
        this.appointmentService = appointmentService;
        this.consultationRepository = consultationRepository;
        this.consultationHistoryRepository = consultationHistoryRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.webSocketEventPublisher = webSocketEventPublisher;
        this.consultationEventService = consultationEventService;
        this.messageSource = messageSource;
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
    public Response<ConsultationResponseDTO> create(Long idAppointment) {

        //Buscamos y validamos la existencia del turno.
        Appointment appointment = appointmentService.getById(idAppointment);

        //Validamos que el turno corresponde al día de la fecha.
        if (!appointment.getDate().toLocalDate().equals(LocalDate.now())) {
            throw new ConflictException("exception.appointmentNotEqualsNow.user", null, "exception.appointmentNotEqualsNow.log", new Object[]{idAppointment, appointment.getDate(), "ConsultationService", "create"}, LogLevel.ERROR);
        }

        //Creamos la consulta
        Consultation consultation  = Consultation.build(appointment);

        //Campos auditoria.
        consultation.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        consultation.setCreatedAt(LocalDateTime.now());
        consultation.setEnabled(true);

        Consultation consultationSaved = consultationRepository.save(consultation);

        //Creamos la Historia de la consulta
        ConsultationHistory consultationHistory = ConsultationHistory.build(consultationSaved, ConsultationStatusType.WAITING_ROOM);

        //Campos de auditoria.
        consultationHistory.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        consultationHistory.setCreatedAt(LocalDateTime.now());
        consultationHistory.setEnabled(true);

        consultationHistoryRepository.save(consultationHistory);

        //Mapea respuesta
        ConsultationResponseDTO consultationResponseDTO = new ConsultationResponseDTO(
                consultationSaved.getId(),
                consultationSaved.getPatient().getPerson().getLastName() + "," + consultationSaved.getPatient().getPerson().getFirstName(),
                consultationSaved.getDentist().getPerson().getLastName() + "," + consultationSaved.getDentist().getPerson().getFirstName(),
                consultationSaved.getStatus().getLabel()
        );

        //Envío webSocket
        webSocketEventPublisher.publish(
                WebSocketEventType.PATIENT_RECEIVED,
                consultationResponseDTO
        );

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
     * @param update objeto que contiene el nuevo estado de la consulta y, opcionalmente,
     *               la información de corrección
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
    public Response<ConsultationResponseDTO> updateStatus(Long idConsultation, ConsultationUpdateRequestDTO update) {

        // Recuperamos la consulta.
        Consultation consultation = getById(idConsultation);

        //Validamos que la consulta no se encuentre finalizada.
        if (consultation.getStatus() == ConsultationStatusType.FINISHED) {
            throw new ConflictException("exception.consultationService.finished.user", null, "exception.consultationService.finished.log", new Object[]{idConsultation, consultation.getStatus().toString(), "ConsultationService", "update"}, LogLevel.ERROR);
        }

        //Valída si no es corrección, el estado tiene que ser futuro. Caso contrario, crear el evento de corrección.
        if (update.correction() == null) {
            if(update.status().isAfter(consultation.getStatus())){
                throw new ConflictException("exception.consultationService.update.statusAfter.user",null,"exception.consultationService.update.statusAfter.log",new Object[]{idConsultation, consultation.getStatus(), update.status(),"ConsultationService", "update"} ,LogLevel.ERROR);
            }

        } else {
            ConsultationEvent consultationEvent = ConsultationEvent.build(
                            consultation,
                            update.correction().eventType(),
                            update.correction().observation()
                    );


            //Campos auditoria
            consultationEvent.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
            consultationEvent.setCreatedAt(LocalDateTime.now());
            consultationEvent.setEnabled(true);

            consultationEventService.create(consultationEvent);

        }

        //Actualiza la consulta.
        consultation.setStatus(update.status());
        consultation.setUpdatedAt(LocalDateTime.now());
        consultation.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());

        Consultation consultationSaved;
        try {
            consultationSaved = consultationRepository.save(consultation);
        } catch (CannotCreateTransactionException | DataAccessException e) {
            throw new DataBaseException(e, "ConsultationService", idConsultation, null, "updateStatus");
        }

        return new Response<>(
                true,
                messageSource.getMessage("consultationService.update.ok", null, LocaleContextHolder.getLocale()),
                new ConsultationResponseDTO(
                        consultationSaved.getId(),
                        consultationSaved.getPatient().getPerson().getLastName() + "," + consultationSaved.getPatient().getPerson().getFirstName(),
                        consultationSaved.getDentist().getPerson().getLastName() + "," + consultationSaved.getDentist().getPerson().getFirstName(),
                        consultationSaved.getStatus().getLabel()
                )
        );

    }
}
