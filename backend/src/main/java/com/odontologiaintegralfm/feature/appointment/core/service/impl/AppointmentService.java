package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentRescheduleRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentRequestSource;
import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.Holiday;
import com.odontologiaintegralfm.feature.appointment.catalogs.service.HolidayService;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentCreateResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.*;
import com.odontologiaintegralfm.feature.appointment.core.repository.IAppointmentRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentService;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.implement.DentistService;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
import com.odontologiaintegralfm.feature.patient.core.service.implement.PatientService;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.infrastructure.systemparameter.enums.SystemParameterKey;
import com.odontologiaintegralfm.infrastructure.systemparameter.service.implement.SystemParameterService;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.response.Response;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AppointmentService implements IAppointmentService {

    @Autowired
    private IAppointmentRepository appointmentRepository;
    @Autowired
    private DentistService dentistService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private DentistAvailabilityService dentistAvailabilityService;
    @Autowired
    private ConflictManagerService conflictManagerService;
    @Autowired
    private DentistHolidayService dentistHolidayService;
    @Autowired
    private HolidayService holidayService;
    @Autowired
    private DentistCalendarLockService dentistCalendarLockService;
    @Autowired
    private DentistCalendarLockDetailService dentistCalendarLockDetailService;
    @Autowired
    private AuthenticatedUserService authenticatedUserService;
    @Qualifier("messageSource")
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private AppointmentStatusHistoryService appointmentStatusHistoryService;
    @Autowired
    private SystemParameterService systemParameterService;


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
     * @return {@link Response} que contiene un {@link AppointmentCreateResponseDTO}
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

    @Override
    @Transactional
    @LogAction(
            value = "appointmentService.logAction.create.ok",
            args = {"#result.data.id", "#result.data.appointmentDateTime", "#result.data.dentistName", "#result.data.patientName"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<AppointmentCreateResponseDTO> create(AppointmentCreateRequestDTO appointmentCreateRequestDTO) {

        //Validar dentista
        Dentist dentist = dentistService.getById(appointmentCreateRequestDTO.idDentist())
                .orElseThrow(() -> new ConflictException("exception.dentistNotFound.user", null, "exception.dentistNotFound.log", new Object[]{appointmentCreateRequestDTO.idDentist(), "Appointment Service", "create"}, LogLevel.ERROR));

        //Validar patient
        Patient patient = patientService.getByIdInternal(appointmentCreateRequestDTO.idPatient());

        //Validar que no haya otro turno
        Optional<Appointment> appointmentExisting = appointmentRepository.findByDentistIdAndDate(dentist.getId(), appointmentCreateRequestDTO.dateTime());
        if (appointmentExisting.isPresent()) {
            throw new ConflictException("exception.appointmentConflict.user", null, "exception.appointmentConflict.log", new Object[]{appointmentExisting.get().getId(), "Appointment Service", "create"}, LogLevel.ERROR);
        }


        //Validar jornada del dentista para la fecha y hora enviada.
        validateAvailability(dentist.getId(), appointmentCreateRequestDTO.dateTime());


        //Validar feriado para la fecha enviada.
        validateHoliday(dentist.getId(), appointmentCreateRequestDTO.dateTime().toLocalDate());

        //Validar bloqueos para la fecha y hora enviada.
        validateCalendarLock(dentist.getId(), appointmentCreateRequestDTO.dateTime());


        //Crear turno
        Appointment appointment = new Appointment(
                patient,
                dentist,
                appointmentCreateRequestDTO.dateTime(),
                AppointmentStatus.RESERVED,
                authenticatedUserService.getAuthenticatedUser(),
                LocalDateTime.now(),
                true
        );

        //Se persiste turno e historial de acciones del mismo.
        Appointment appointmentSaved;
        try{
             appointmentSaved = appointmentRepository.save(appointment);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentService", null, null, "create");
        }

        //Se crea y persiste en el historial.
        AppointmentStatusHistory appointmentStatusHistory = new AppointmentStatusHistory(
                appointment,
                AppointmentStatus.RESERVED,
                null,
                authenticatedUserService.getAuthenticatedUser(),
                LocalDateTime.now(),
                true
        );

        appointmentStatusHistoryService.save(appointmentStatusHistory);



        return new Response<>(
                true,
                messageSource.getMessage(
                        "appointmentService.create.ok",
                        null,
                        LocaleContextHolder.getLocale()
                ),
                new AppointmentCreateResponseDTO(
                        appointmentSaved.getId(),
                        appointmentSaved.getDentist().getPerson().getLastName() + "," + appointmentSaved.getDentist().getPerson().getFirstName(),
                        appointmentSaved.getPatient().getPerson().getLastName() + "," + appointmentSaved.getPatient().getPerson().getFirstName(),
                        appointmentSaved.getDate(),
                        appointmentSaved.getStatus()
                )
        );

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
     *         permitido según quién solicita la modificación ({@link AppointmentRequestSource} paciente o dentista).
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

    @Override
    @LogAction(
            value = "appointmentService.logAction.reschedule.ok",
            args = {"#result.data.id", "#result.data.appointmentDateTime", "#result.data.dentistName", "#result.data.patientName"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<AppointmentCreateResponseDTO> reschedule(Long idAppointment, AppointmentRescheduleRequestDTO appointmentRescheduleRequestDTO) {

        //Valída que exista el turno y que se encuentra en un estado que permita su reprogramación.
        Appointment appointment = appointmentRepository.findById(idAppointment)
                .orElseThrow(() -> new ConflictException("exception.appointmentNotFound.user",null,"exception.appointmentNotFound.log",new Object[]{idAppointment, "AppointmentService", "reschedule"}, LogLevel.ERROR));

        if(appointment.getStatus() != AppointmentStatus.RESERVED ){
            throw new ConflictException("exception.reschedule.conflictStatus.user",new Object[]{appointment.getStatus()},"exception.reschedule.conflictStatus.log",new Object[]{idAppointment,appointment.getStatus(), "AppointmentService", "reschedule"}, LogLevel.ERROR);

        }

        //Valída que se cumpla el tiempo mínimo de reprogramación del turno, según quien lo realiza (Dentista o Paciente).
        validateMinimumHours(appointmentRescheduleRequestDTO.requestSource(), appointment.getDate());


        //Validar dentista
        Dentist dentist = dentistService.getById(appointmentRescheduleRequestDTO.appointment().idDentist())
                .orElseThrow(() -> new ConflictException("exception.dentistNotFound.user", null, "exception.dentistNotFound.log", new Object[]{appointmentRescheduleRequestDTO.appointment().idDentist(), "Appointment Service", "create"}, LogLevel.ERROR));

        //Validar patient
        Patient patient = patientService.getByIdInternal(appointmentRescheduleRequestDTO.appointment().idPatient());

        //Validar que no haya otro turno
        Optional<Appointment> appointmentExisting = appointmentRepository.findByDentistIdAndDate(dentist.getId(), appointmentRescheduleRequestDTO.appointment().dateTime());
        if (appointmentExisting.isPresent()) {
            throw new ConflictException("exception.appointmentConflict.user", null, "exception.appointmentConflict.log", new Object[]{appointmentExisting.get().getId(), "Appointment Service", "create"}, LogLevel.ERROR);
        }

        //Validar jornada del dentista para la fecha y hora enviada.
        validateAvailability(dentist.getId(), appointmentRescheduleRequestDTO.appointment().dateTime());


        //Validar feriado para la fecha enviada.
        validateHoliday(dentist.getId(), appointmentRescheduleRequestDTO.appointment().dateTime().toLocalDate());

        //Validar bloqueos para la fecha y hora enviada.
        validateCalendarLock(dentist.getId(), appointmentRescheduleRequestDTO.appointment().dateTime());


        //Actualiza el appointment con la nueva fecha.
        appointment.setDate(appointmentRescheduleRequestDTO.appointment().dateTime());

        //Actualiza historial de turnos.
        AppointmentStatusHistory appointmentStatusHistory = new AppointmentStatusHistory(
                appointment,
                AppointmentStatus.RESCHEDULED,
                appointmentRescheduleRequestDTO.observation(),
                authenticatedUserService.getAuthenticatedUser(),
                LocalDateTime.now(),
                true
        );

        //persiste
        try{
            appointmentRepository.save(appointment);
            appointmentStatusHistoryService.save(appointmentStatusHistory);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentService", idAppointment, null, "reschedule");
        }

        return new Response<>(
                true,
                "appointmentService.reschedule.ok",
                new AppointmentCreateResponseDTO(
                        appointment.getId(),
                        appointment.getDentist().getPerson().getLastName() + "," + appointment.getDentist().getPerson().getFirstName(),
                        appointment.getPatient().getPerson().getLastName() + "," + appointment.getPatient().getPerson().getFirstName(),
                        appointment.getDate(),
                        appointment.getStatus()
                )

        );


    }


    /**
     * Valída que la reprogramación de un turno se realice con la anticipación mínima requerida según quien lo solicite.
     * <p>El sistema define dos parámetros de configuración:
     * uno para solicitudes realizadas por pacientes y otro para solicitudes realizadas por profesionales del consultorio.
     * Dichos valores,expresados en horas, se utilizan para determinar si la reprogramación está permitida.</p>
     * <p>
     * La validación consiste en restar ese tiempo mínimo a la fecha y hora del turno. Si el resultado es anterior al momento actual, significa que
     * la acción se está intentando fuera del tiempo permitido y se lanza una {@link BadRequestException}.</p>
     * @param appointmentRequestSource indica quién solicita la reprogramación
     *                                 (PACIENTE o DENTISTA), lo cual determina
     *                                 qué parámetro de sistema se utiliza.
     *
     * @param appointmentDateTime      la fecha y hora original del turno a evaluar.
     *
     * @throws BadRequestException si la reprogramación no cumple con el tiempo
     *                             mínimo de anticipación definido por el negocio.
     */

    private void validateMinimumHours(AppointmentRequestSource appointmentRequestSource, LocalDateTime appointmentDateTime) {

        int minimumHours = Integer.parseInt(
                systemParameterService.getByKey
                        ((appointmentRequestSource == AppointmentRequestSource.PATIENT)
                                ? SystemParameterKey.APPOINTMENT_RESCHEDULE_PATIENT
                                : SystemParameterKey.APPOINTMENT_RESCHEDULE_DENTIST
                        )
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = appointmentDateTime.minusHours(minimumHours);

        if (limit.isBefore(now)) {
            throw new BadRequestException("exception.validateMinimumHours.user", null, "exception.validateMinimumHours.log", new Object[]{appointmentDateTime, limit, "AppointmentService", "validateMinimumHours"}, LogLevel.ERROR);
        }
    }


    /**
     * Valída que el turno a crea no esté dentro un bloqueo de calendario.
     *
     * @param idDentist           : idDentista
     * @param appointmentDateTime : Fecha y hora del turno.
     */
    private void validateCalendarLock(Long idDentist, LocalDateTime appointmentDateTime) {
        //Obtener bloqueos.
        List<DentistCalendarLock> dentistCalendarLock = dentistCalendarLockService.getAllCurrentByDentistId(idDentist);

        //Validar esos bloqueos con la fecha del turno.
        for (DentistCalendarLock dc : dentistCalendarLock) {

            //Obtener detalles de bloqueos.
            List<DentistCalendarLockDetail> dentistCalendarLockDetails = dentistCalendarLockDetailService.getAllByDentistCalendarLock(dc.getId());

            //Si la lista está vacía, el bloqueo es diario.
            if (dentistCalendarLockDetails.isEmpty()) {
                if (conflictManagerService.hasAppointmentMatchWithEvent(appointmentDateTime, dc.getStartDate(), dc.getEndDate(), null, dc.getStartTime(), dc.getEndTime(), CalendarLockRecurrenceName.DAILY)) {
                    throw new ConflictException("exception.appointmentService.validateCalendarLock.user", null, "exception.appointmentService.validateCalendarLock.log", new Object[]{dc.getId(), appointmentDateTime, "Appointment Service", "validateCalendarLock"}, LogLevel.ERROR);
                }
            } else {
                for (DentistCalendarLockDetail dcld : dentistCalendarLockDetails) {
                    if (conflictManagerService.hasAppointmentMatchWithEvent(appointmentDateTime, dc.getStartDate(), dc.getEndDate(), dcld.getDayName().toDayOfWeek(), dc.getStartTime(), dc.getEndTime(), dc.getRecurrence())) {
                        throw new ConflictException("exception.appointmentService.validateCalendarLock.user", null, "exception.appointmentService.validateCalendarLock.log", new Object[]{dc.getId(), appointmentDateTime, "Appointment Service", "validateCalendarLock"}, LogLevel.ERROR);
                    }
                }
            }
        }
    }



    /**
     * Valída que si la fecha es un feriado, el dentista la trabaje.
     *
     * @param id        : id del dentista
     * @param localDate : fecha del turno.
     */
    private void validateHoliday(Long id, LocalDate localDate) {

        //Se valida que el turno no sea un feriado.
        Optional<Holiday> holiday = holidayService.getByDate(localDate);

        // Si es feriado, se valida que el dentista lo trabaje.
        if (holiday.isPresent()) {
           dentistHolidayService.getByDentistIdAndHolidayId(id,holiday.get().getId())
                   .orElseThrow(() -> new ConflictException("exception.appointmentService.validateHoliday.user", null, "exception.appointmentService.validateHoliday.log",new Object[]{id ,holiday.get().getId(), "appointmentService", "validateHoliday"}, LogLevel.ERROR));
        }
    }





    /**
     * Valída que el turno a crear esté dentro de la jornada laboral del dentista.
     * @param idDentist : idDentista
     * @param appointmentDateTime : Fecha y hora del turno.
     */

    private void validateAvailability(Long idDentist, LocalDateTime appointmentDateTime) {

        //Obtener disponibilidad
        List<DentistAvailability> dentistAvailability = dentistAvailabilityService.getByIdInternal(idDentist);

        //Separo Dias de horas
        LocalDate date = appointmentDateTime.toLocalDate();

        boolean match = false;


        if(dentistAvailability.size() == 1){
            match = conflictManagerService.hasAppointmentMatchWithEvent(appointmentDateTime, dentistAvailability.get(0).getSpecificDate(), dentistAvailability.get(0).getSpecificDate(), null,dentistAvailability.get(0).getStartTime() , dentistAvailability.get(0).getEndTime(),null);
        }else{
            for (DentistAvailability d : dentistAvailability) {
                //Obtiene los Date de la jornada de los próximos 7 días.
                LocalDate startDate = conflictManagerService.findFirstMatchingDate(LocalDate.now().plusDays(1), d.getKeyName().toDayOfWeek());
                match = conflictManagerService.hasAppointmentMatchWithEvent(appointmentDateTime, startDate, date, d.getKeyName().toDayOfWeek(), d.getStartTime(), d.getEndTime(), d.getRecurrence());
            }

        }

        if (!match) {
            throw new ConflictException("exception.appointmentService.validateAvailability.user", null,"exception.appointmentService.validateAvailability.log", new Object[]{idDentist,appointmentDateTime ,"Appointment Service", "validateAvailability"}, LogLevel.ERROR);
        }

    }


}

