package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto.AppointmentCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto.AppointmentResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentActionRequester;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.AppointmentStatusHistory;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.repository.IAppointmentRepository;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
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

@Service
public class CreateAppointmentUseCase {

    private final IAppointmentRepository appointmentRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final MessageSource messageSource;
    private final AppointmentDomainService appointmentDomainService;
    private final AppointmentStatusHistoryService appointmentStatusHistoryService;
    private final AppointmentNotificationService appointmentNotificationService;


    public CreateAppointmentUseCase(
            IAppointmentRepository appointmentRepository,
            AuthenticatedUserService authenticatedUserService,
            MessageSource messageSource,
            AppointmentDomainService appointmentDomainService,
            AppointmentStatusHistoryService appointmentStatusHistoryService,
            AppointmentNotificationService appointmentNotificationService
    ){
        this.appointmentRepository = appointmentRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.messageSource = messageSource;
        this.appointmentDomainService = appointmentDomainService;
        this.appointmentStatusHistoryService = appointmentStatusHistoryService;
        this.appointmentNotificationService = appointmentNotificationService;
    }



    /**
     * Crea un nuevo turno.
     *
     * <p>Validaciones realizadas:</p>
     * <ul>
     *     <li>El dentista debe existir.</li>
     *     <li>El paciente debe existir.</li>
     *     <li>No debe existir otro turno reservado para el mismo dentista en la misma fecha y hora.</li>
     *     <li>La fecha y hora deben coincidir con la jornada laboral del dentista.</li>
     *     <li>No debe ser un día feriado para ese dentista.</li>
     *     <li>No debe existir un bloqueo de calendario que impida asignar el turno.</li>
     * </ul>
     *
     * <p>Si todas las validaciones son correctas, se persiste el turno con estado
     * {@link AppointmentStatus#RESERVED} y se devuelve un objeto de respuesta con los
     * datos más relevantes del turno creado.</p>
     *
     * @param appointmentCreateRequestDTO DTO con los datos necesarios para crear el turno:
     *                                    id del dentista, id del paciente y fecha-hora del turno.
     *
     * @return {@link Response} que contiene un {@link AppointmentResponseDTO}
     * con la información del turno creado (ID, dentista, paciente, fecha-hora y estado).
     *
     * @throws ConflictException si:
     * <ul>
     *     <li>El dentista no existe.</li>
     *     <li>Existe ya un turno para el dentista en ese horario.</li>
     *     <li>La fecha-hora no corresponde a la jornada laboral del dentista.</li>
     *     <li>La fecha es feriado para ese dentista.</li>
     *     <li>Existe un bloqueo de calendario en ese horario.</li>
     *</ul>
     */
    @Transactional
    @LogAction(
            value = "createAppointmentUseCase.logAction.create.ok",
            args = {"#result.data.id", "#result.data.appointmentDateTime", "#result.data.dentistName", "#result.data.patientName"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<AppointmentResponseDTO>execute(AppointmentCreateRequestDTO appointmentCreateRequestDTO) {

        // Valida y construye el turno
        Appointment appointment = appointmentDomainService.validateAndBuildAppointment(appointmentCreateRequestDTO);


        //Se crea y persiste en el historial.
        AppointmentStatusHistory appointmentStatusHistory = AppointmentStatusHistory.build(appointment, AppointmentStatus.RESERVED, AppointmentActionRequester.PATIENT, null);

        //Campos auditoria.
        appointmentStatusHistory.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        appointmentStatusHistory.setCreatedAt(LocalDateTime.now());
        appointmentStatusHistory.setEnabled(true);


        //persiste turno e historial.
        Appointment appointmentSaved;
        try{
            appointmentSaved = appointmentRepository.save(appointment);
            appointmentStatusHistoryService.save(appointmentStatusHistory);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentQueryService", null, null, "create");
        }



        //Notificación por mail.
        appointmentNotificationService.sendAppointmentEmail(
                appointment,
                "createAppointmentUseCase.notifyEmail.subject",
                "createAppointmentUseCase.notifyEmail.title",
                "createAppointmentUseCase.notifyEmail.message"
        );



        return new Response<>(
                true,
                messageSource.getMessage(
                        "createAppointmentUseCase.create.ok",
                        null,
                        LocaleContextHolder.getLocale()
                ),
                AppointmentResponseDTO.build(appointmentSaved)
        );

    }

}
