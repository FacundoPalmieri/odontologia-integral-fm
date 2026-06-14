package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto.AppointmentCancelAllRequestDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentActionRequester;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.AppointmentStatusHistory;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.repository.IAppointmentRepository;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CancelAppointmentByDateUseCase {


    private final IAppointmentRepository appointmentRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final MessageSource messageSource;
    private final AppointmentStatusHistoryService appointmentStatusHistoryService;
    private final AppointmentNotificationService appointmentNotificationService;


    public CancelAppointmentByDateUseCase(
            IAppointmentRepository appointmentRepository,
            AuthenticatedUserService authenticatedUserService,
            MessageSource messageSource,
            AppointmentStatusHistoryService appointmentStatusHistoryService,
            AppointmentNotificationService appointmentNotificationService) {
        this.appointmentRepository = appointmentRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.messageSource = messageSource;
        this.appointmentStatusHistoryService = appointmentStatusHistoryService;
        this.appointmentNotificationService = appointmentNotificationService;
    }




    /**
     * Cancela todos los turnos con estado {@link AppointmentStatus#RESERVED} pertenecientes a un dentista en una fecha determinada.
     * <p>
     * Este método se utiliza ante situaciones imprevistas del dentista (emergencias,
     * enfermedad, ausencias repentinas, etc.) donde no puede atender durante un día
     * completo y es necesario cancelar todos sus turnos.
     * </p>
     * <ul>
     *     <li>Valída que la fecha ingresada sea posterior a la fecha actual. No permite cancelar turnos del mismo día.</li>
     *     <li>Obtiene todos los turnos RESERVED futuros correspondientes al dentista.</li>
     *     <li>Actualiza el estado de cada turno a {@link AppointmentStatus#CANCELED}.</li>
     *     <li>Registra cada cambio en el historial de estados.</li>
     *     <li>Agrupa los turnos por paciente para facilitar el envío posterior de notificaciones.</li>
     *     <li>Persiste los cambios en la base de datos (turnos e historiales).</li>
     *     <li>Envía un correo electrónico a cada paciente notificando la cancelación.</li>
     * </ul>
     *
     * <h3>Notificaciones:</h3>
     * Por cada turno cancelado se envía un correo.
     * @param idDentist  identificador del dentista cuyos turnos deben cancelarse
     * @param date  fecha para la cual deben cancelarse los turnos; debe ser posterior al día actual
     * @param appointmentCancelRequestDTO datos adicionales de cancelación (fuente, observación, etc.)
     *
     * @return Response<Integer> número total de turnos cancelados
     *
     * @throws BadRequestException si la fecha no es posterior a la fecha actual
     * @throws DataBaseException sí ocurre un error al persistir los cambios
     */

    @LogAction(
            value = "cancelAppointmentByDateUseCase.logAction.execute.ok",
            args = {"#idDentist", "#result.data"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<Integer> execute(Long idDentist , LocalDate date, AppointmentCancelAllRequestDTO appointmentCancelRequestDTO) {

        //Valída que la fecha sea mayor al día actual.
        if(!date.isAfter(LocalDate.now())){
            throw new BadRequestException("exception.cancelAppointmentByDate.dateNotFuture.user", null, "exception.cancelAppointmentByDate.dateNotFuture.log", new Object[]{date, LocalDate.now(), "CancelAppointmentByDateUseCase", "execute"}, LogLevel.ERROR);
        }


        //Recuperar turnos con estado "Reserved" para el dentista y día solicitado.
        List<Appointment> appointments = appointmentRepository.findFutureAppointmentsReservedByDentist(idDentist, LocalDateTime.now(), AppointmentStatus.RESERVED);

        List<AppointmentStatusHistory> appointmentStatusHistory = new ArrayList<>();

        //Lista para guardar los mails de los pacientes para posterior envío.
        Map<Patient, List<Appointment>> appointmentsByPatient = new HashMap<>();

        // Recorre turnos
        for (Appointment a : appointments) {

            a.setStatus(AppointmentStatus.CANCELED);
            a.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
            a.setUpdatedAt(LocalDateTime.now());

            // Agrupar por paciente
            appointmentsByPatient
                    .computeIfAbsent(a.getPatient(), p -> new ArrayList<>())
                    .add(a);

            // Crear historial
            AppointmentStatusHistory ah =
                    AppointmentStatusHistory.build(
                            a,
                            AppointmentStatus.CANCELED,
                            AppointmentActionRequester.PATIENT,
                            appointmentCancelRequestDTO.observation()
                    );

            //Datos auditoría.
            ah.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
            ah.setCreatedAt(LocalDateTime.now());
            ah.setEnabled(true);

            appointmentStatusHistory.add(ah);
        }

        //persiste
        List<Appointment> appointmentsSaved;
        try {
            appointmentsSaved = appointmentRepository.saveAll(appointments);
            appointmentStatusHistoryService.saveAll(appointmentStatusHistory);

        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "CancelAppointmentByDateUseCase", null, null, "execute");
        }


        // Notificación por mail
        appointments.forEach(a ->
                appointmentNotificationService.sendAppointmentEmail(
                        a,
                        "cancelAppointmentByDateUseCase.notifyEmail.subject",
                        "cancelAppointmentByDateUseCase.notifyEmail.title",
                        "cancelAppointmentByDateUseCase.notifyEmail.message"
                )
        );


        return new Response<>(
                true,
                messageSource.getMessage(
                        "cancelAppointmentByDateUseCase.cancelAllByDate.ok",
                        new Object[]{appointmentsSaved.size()},
                        LocaleContextHolder.getLocale()
                ),
                appointmentsSaved.size()

        );

    }

}
