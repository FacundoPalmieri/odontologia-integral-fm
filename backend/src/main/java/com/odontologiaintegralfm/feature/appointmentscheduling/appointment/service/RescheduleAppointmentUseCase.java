package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto.AppointmentRescheduleRequestDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto.AppointmentResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentActionRequester;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.AppointmentStatusHistory;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.repository.IAppointmentRepository;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RescheduleAppointmentUseCase {



    private final IAppointmentRepository appointmentRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final MessageSource messageSource;
    private final AppointmentStatusHistoryService appointmentStatusHistoryService;
    private final AppointmentConflictService appointmentConflictService;
    private final AppointmentDomainService  appointmentDomainService;
    private final AppointmentNotificationService  appointmentNotificationService;

    public RescheduleAppointmentUseCase(
            IAppointmentRepository appointmentRepository,
            AuthenticatedUserService authenticatedUserService,
            MessageSource messageSource,
            AppointmentStatusHistoryService appointmentStatusHistoryService,
            AppointmentConflictService appointmentConflictService,
            AppointmentDomainService appointmentDomainService,
            AppointmentNotificationService appointmentNotificationService) {
        this.appointmentRepository = appointmentRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.messageSource = messageSource;
        this.appointmentStatusHistoryService = appointmentStatusHistoryService;
        this.appointmentConflictService = appointmentConflictService;
        this.appointmentDomainService = appointmentDomainService;
        this.appointmentNotificationService =  appointmentNotificationService;
    }

    /**
     * Reprograma un turno existente.
     * <p>
     * Este método actualiza la fecha y hora de un turno previamente reservado, dejando su estado
     * principal como {@link AppointmentStatus#RESERVED}, ya que la reprogramación no representa
     * un nuevo estado del turno sino un evento. La acción se registra en la tabla de historial
     * mediante un registro con estado {@link AppointmentStatus#RESCHEDULED}.
     * </p>
     *
     * <h3>Validaciones realizadas:</h3>
     * <ul>
     *     <li><b>Existencia del turno:</b> Se verifica que el turno exista y se encuentre en un estado que permita reprogramación
     *         (únicamente {@link AppointmentStatus#RESERVED}).</li>
     *
     *     <li><b>Tiempo mínimo de reprogramación:</b> Se valida que la operación cumpla el tiempo mínimo
     *         permitido según quién solicita la modificación ({@link AppointmentActionRequester} paciente o dentista).
     *         Esta validación se realiza mediante {@code validateMinimumHours()}.</li>
     *
     *     <li><b>Validación de dentista y paciente:</b> Se verifica que ambos existan y sean válidos
     *         según los datos enviados en la solicitud.</li>
     *
     *     <li><b>Conflicto de turnos:</b> Se comprueba que el dentista no tenga otro turno asignado
     *         en la nueva fecha y hora solicitada.</li>
     *
     *     <li><b>Jornada laboral:</b> Se valida que la nueva fecha del turno esté dentro de la jornada
     *         del dentista.</li>
     *
     *     <li><b>Feriados:</b> Se verifica que la fecha no coincida con un feriado configurado.</li>
     *
     *     <li><b>Bloqueos de calendario:</b> Se comprueba que no exista un bloqueo activo para el dentista
     *         en la fecha y hora solicitada.</li>
     * </ul>
     *
     * <h3>Acciones realizadas:</h3>
     * <ul>
     *     <li>Actualiza la fecha del turno existente.</li>
     *     <li>Registra un nuevo historial en {@code AppointmentStatusHistory} con estado
     *         {@link AppointmentStatus#RESCHEDULED}, incluyendo usuario autenticado, fecha/hora y observación.</li>
     *     <li>Persiste tanto el turno como el registro de historial.</li>
     * </ul>
     *
     * @param idAppointment ID del turno que se desea reprogramar.
     * @param appointmentRescheduleRequestDTO Datos necesarios para la reprogramación (nueva fecha,
     *                                        origen de la solicitud y observación opcional).
     *
     * @return Una respuesta exitosa que contiene los datos actualizados del turno reprogramado.
     *
     * @throws ConflictException Si el turno no existe, no está reservado, o se detecta algún conflicto
     *                           de negocio (otro turno en la misma fecha, dentista inexistente, etc.).
     * @throws BadRequestException Si no se cumple el tiempo mínimo permitido para reprogramar.
     * @throws DataBaseException Sí ocurre un error al persistir los cambios.
     */

    @LogAction(
            value = "rescheduleAppointmentUseCase.logAction.reschedule.ok",
            args = {"#result.data.id", "#result.data.appointmentDateTime", "#result.data.dentistName", "#result.data.patientName"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    @Transactional
    public Response<AppointmentResponseDTO> execute(Long idAppointment, AppointmentRescheduleRequestDTO appointmentRescheduleRequestDTO) {

        //Valída que exista el turno y que se encuentra en un estado que permita su reprogramación.
        Appointment appointment = appointmentRepository.findById(idAppointment)
                .orElseThrow(() -> new ConflictException("exception.appointmentNotFound.user",null,"exception.appointmentNotFound.log",new Object[]{idAppointment, "AppointmentQueryService", "reschedule"}, LogLevel.ERROR));

        if(appointment.getStatus() != AppointmentStatus.RESERVED){
            throw new ConflictException("exception.reschedule.conflictStatus.user",new Object[]{appointment.getStatus()},"exception.reschedule.conflictStatus.log",new Object[]{idAppointment,appointment.getStatus(), "AppointmentQueryService", "reschedule"}, LogLevel.ERROR);

        }

        //Valída que se cumpla el tiempo mínimo de reprogramación del turno, según quien lo realiza (Dentista o Paciente).
        appointmentDomainService.validateMinimumHoursForReschedule(appointmentRescheduleRequestDTO.requestSource(), appointment.getDate());


        //Actualizamos estado
        appointment.setStatus(AppointmentStatus.RESCHEDULED);


        // Valida y construye el turno
        Appointment appointmentNew = appointmentDomainService.validateAndBuildAppointment(appointmentRescheduleRequestDTO.appointment());

        //Actualizamos la referencia del nuevo turno.
        appointmentNew.setRescheduledFrom(appointment);



        //Actualiza historial de turnos.
        AppointmentStatusHistory appointmentStatusHistory = AppointmentStatusHistory.build(
                appointmentNew,
                AppointmentStatus.RESCHEDULED,
                appointmentRescheduleRequestDTO.requestSource(),
                appointmentRescheduleRequestDTO.observation()
        );
        //Campos auditoria historial.
        appointmentStatusHistory.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        appointmentStatusHistory.setCreatedAt(LocalDateTime.now());
        appointmentStatusHistory.setEnabled(true);

        //persistencia de turno anterior(deshabilitado), turno nuevo, historial.
        try{
            appointmentRepository.save(appointment);
            appointmentRepository.save(appointmentNew);
            appointmentStatusHistoryService.save(appointmentStatusHistory);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentQueryService", idAppointment, null, "reschedule");
        }


        //Limpia si existe turnos en conflictos

        //Obtiene conflicto
        AppointmentConflict appointmentConflict =
                appointmentConflictService.findAppointmentConflictByAppointmentAndResolved(
                        appointment,
                        false
                );

        //Resuelve solo si hay conflicto.
        if(appointmentConflict!=null){
            appointmentConflictService.resolvedAll(
                    List.of(appointmentConflict.getId()),
                    LocalDateTime.now(),
                    authenticatedUserService.getAuthenticatedUser()

            );
        }


        //Notificación por mail.
        appointmentNotificationService.sendAppointmentEmail(
                appointment,
                "rescheduleAppointmentUseCase.notifyEmail.subject",
                "rescheduleAppointmentUseCase.notifyEmail.title",
                "rescheduleAppointmentUseCase.notifyEmail.message"
        );



        return new Response<>(
                true,
                messageSource.getMessage(
                        "rescheduleAppointmentUseCase.reschedule.ok",
                        null,
                        LocaleContextHolder.getLocale()
                ),
                AppointmentResponseDTO.build(appointment)
        );


    }
}
