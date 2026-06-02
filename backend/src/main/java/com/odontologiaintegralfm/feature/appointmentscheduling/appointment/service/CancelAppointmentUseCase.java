package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto.AppointmentCancelRequestDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto.AppointmentResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
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
import java.time.LocalDateTime;

@Service
public class CancelAppointmentUseCase {


    private final IAppointmentRepository appointmentRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final MessageSource messageSource;
    private final AppointmentStatusHistoryService appointmentStatusHistoryService;
    private final AppointmentDomainService appointmentDomainService;
    private final AppointmentNotificationService appointmentNotificationService;


    public CancelAppointmentUseCase(
            IAppointmentRepository appointmentRepository,
            AuthenticatedUserService authenticatedUserService,
            MessageSource messageSource,
            AppointmentStatusHistoryService appointmentStatusHistoryService,
            AppointmentDomainService appointmentDomainService,
            AppointmentNotificationService appointmentNotificationService) {
        this.appointmentRepository = appointmentRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.messageSource = messageSource;
        this.appointmentStatusHistoryService = appointmentStatusHistoryService;
        this.appointmentDomainService = appointmentDomainService;
        this.appointmentNotificationService = appointmentNotificationService;
    }



    /**
     * Cancela un turno existente.
     * <p>
     * Este método:
     * <ol>
     *     <li>Verifica que el turno exista.</li>
     *     <li>Comprueba que se encuentre en un estado que permita su cancelación (solo RESERVED).</li>
     *     <li>Valida el tiempo mínimo requerido para cancelar, según quién solicite la acción.</li>
     *     <li>Actualiza el estado del turno a {@link AppointmentStatus#CANCELED}.</li>
     *     <li>Registra el cambio en el historial de estados.</li>
     * </ol>
     * <p>
     * No modifica la fecha original del turno: simplemente lo marca como cancelado.
     *
     * @param idAppointment ID del turno a cancelar.
     * @param appointmentCancelRequestDTO Datos de la solicitud de cancelación,
     *                                    incluyendo motivo y quién la pidió (dentista o paciente).
     * @return Un {@link Response} con la información actualizada del turno ya cancelado.
     * @throws ConflictException Si el turno no existe o no está en un estado que permita cancelación.
     * @throws BadRequestException Si no se cumple el tiempo mínimo de cancelación.
     * @throws DataBaseException Si ocurre un error al persistir la información en la base de datos.
     */

    @LogAction(
            value = "cancelAppointmentUseCase.logAction.cancel.ok",
            args = {"#result.data.id", "#result.data.appointmentDateTime", "#result.data.dentistName", "#result.data.patientName"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<AppointmentResponseDTO> execute(Long idAppointment, AppointmentCancelRequestDTO appointmentCancelRequestDTO) {

        //Validar que el turno exista.
        Appointment appointment = appointmentRepository.findById(idAppointment)
                .orElseThrow(() -> new ConflictException("exception.appointmentNotFound.user",null,"exception.appointmentNotFound.log",new Object[]{idAppointment, "CancelAppointmentUseCase", "execute"}, LogLevel.ERROR));

        //Validar que esté en un estado "Cancelable"
        if(appointment.getStatus() != AppointmentStatus.RESERVED){
            throw new ConflictException("exception.cancel.conflictStatus.user",new Object[]{appointment.getStatus()},"exception.cancel.conflictStatus.log",new Object[]{idAppointment,appointment.getStatus(), "CancelAppointmentUseCase", "execute"}, LogLevel.ERROR);

        }

        //Validar mínimo de horas de cancelación.
        appointmentDomainService.validateMinimumHoursForCancel(appointmentCancelRequestDTO.requestSource(), appointment.getDate());

        //Actualizar estado Turno.
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointment.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
        appointment.setUpdatedAt(LocalDateTime.now());

        //persiste turno
        try{
            appointmentRepository.save(appointment);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "CancelAppointmentUseCase", idAppointment, null, "execute");
        }

        //Actualizar estado Historial y persiste.
        AppointmentStatusHistory appointmentStatusHistory = AppointmentStatusHistory.build(
                appointment,
                AppointmentStatus.CANCELED,
                appointmentCancelRequestDTO.requestSource(),
                appointmentCancelRequestDTO.observation()
        );
        //Campos auditoría
        appointmentStatusHistory.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        appointmentStatusHistory.setCreatedAt(LocalDateTime.now());
        appointmentStatusHistory.setEnabled(true);
        appointmentStatusHistoryService.save(appointmentStatusHistory);



        //Notificación por mail.
        appointmentNotificationService.sendAppointmentEmail(
                appointment,
                "cancelAppointmentUseCase.notifyEmail.subject",
                "cancelAppointmentUseCase.notifyEmail.title",
                "cancelAppointmentUseCase.notifyEmail.message"
        );



        return new Response<>(
                true,
                messageSource.getMessage(
                        "cancelAppointmentUseCase.cancel.ok",
                        null,
                        LocaleContextHolder.getLocale()
                ),
                AppointmentResponseDTO.build(appointment)

        );

    }

}
