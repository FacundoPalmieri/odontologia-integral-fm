package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.TemplateEmail;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentCancelRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentRescheduleRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentActionRequester;
import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.Holiday;
import com.odontologiaintegralfm.feature.appointment.catalogs.service.HolidayService;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.*;
import com.odontologiaintegralfm.feature.appointment.core.repository.IAppointmentRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentService;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.implement.DentistService;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
import com.odontologiaintegralfm.feature.patient.core.service.implement.PatientService;
import com.odontologiaintegralfm.feature.person.core.model.ContactEmail;
import com.odontologiaintegralfm.feature.user.service.IUserService;
import com.odontologiaintegralfm.infrastructure.email.service.IEmailService;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.infrastructure.systemparameter.enums.SystemParameterKey;
import com.odontologiaintegralfm.infrastructure.systemparameter.service.implement.SystemParameterService;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
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
import java.time.format.DateTimeFormatter;
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
    @Autowired
    private IEmailService emailService;
    @Autowired
    private IUserService userService;


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

    @Override
    @Transactional
    @LogAction(
            value = "appointmentService.logAction.create.ok",
            args = {"#result.data.id", "#result.data.appointmentDateTime", "#result.data.dentistName", "#result.data.patientName"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<AppointmentResponseDTO> create(AppointmentCreateRequestDTO appointmentCreateRequestDTO) {

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
        validateAvailabilityForAppointment(dentist.getId(), appointmentCreateRequestDTO.dateTime());


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
                AppointmentActionRequester.PATIENT,
                null,
                authenticatedUserService.getAuthenticatedUser(),
                LocalDateTime.now(),
                true
        );

        appointmentStatusHistoryService.save(appointmentStatusHistory);


        //Notificación por mail.
        sendAppointmentEmail(
                appointment,
                "appointmentService.create.notifyEmail.subject",
                "appointmentService.create.notifyEmail.title",
                "appointmentService.create.notifyEmail.message"
        );




        return new Response<>(
                true,
                messageSource.getMessage(
                        "appointmentService.create.ok",
                        null,
                        LocaleContextHolder.getLocale()
                ),
                new AppointmentResponseDTO(
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

    @Override
    @LogAction(
            value = "appointmentService.logAction.reschedule.ok",
            args = {"#result.data.id", "#result.data.appointmentDateTime", "#result.data.dentistName", "#result.data.patientName"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<AppointmentResponseDTO> reschedule(Long idAppointment, AppointmentRescheduleRequestDTO appointmentRescheduleRequestDTO) {

        //Valída que exista el turno y que se encuentra en un estado que permita su reprogramación.
        Appointment appointment = appointmentRepository.findById(idAppointment)
                .orElseThrow(() -> new ConflictException("exception.appointmentNotFound.user",null,"exception.appointmentNotFound.log",new Object[]{idAppointment, "AppointmentService", "reschedule"}, LogLevel.ERROR));

        if(appointment.getStatus() != AppointmentStatus.RESERVED){
            throw new ConflictException("exception.reschedule.conflictStatus.user",new Object[]{appointment.getStatus()},"exception.reschedule.conflictStatus.log",new Object[]{idAppointment,appointment.getStatus(), "AppointmentService", "reschedule"}, LogLevel.ERROR);

        }


        //Valída que se cumpla el tiempo mínimo de reprogramación del turno, según quien lo realiza (Dentista o Paciente).
        validateMinimumHoursForReschedule(appointmentRescheduleRequestDTO.requestSource(), appointment.getDate());


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
        validateAvailabilityForAppointment(dentist.getId(), appointmentRescheduleRequestDTO.appointment().dateTime());


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
                appointmentRescheduleRequestDTO.requestSource(),
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


        //Notificación por mail.
        sendAppointmentEmail(
                appointment,
                "appointmentService.reschedule.notifyEmail.subject",
                "appointmentService.reschedule.notifyEmail.title",
                "appointmentService.reschedule.notifyEmail.message"
        );



        return new Response<>(
                true,
                messageSource.getMessage(
                        "appointmentService.reschedule.ok",
                        null,
                        LocaleContextHolder.getLocale()
                ),
                new AppointmentResponseDTO(
                        appointment.getId(),
                        appointment.getDentist().getPerson().getLastName() + "," + appointment.getDentist().getPerson().getFirstName(),
                        appointment.getPatient().getPerson().getLastName() + "," + appointment.getPatient().getPerson().getFirstName(),
                        appointment.getDate(),
                        appointment.getStatus()
                )

        );


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

    @Override
    @LogAction(
            value = "appointmentService.logAction.cancel.ok",
            args = {"#result.data.id", "#result.data.appointmentDateTime", "#result.data.dentistName", "#result.data.patientName"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<AppointmentResponseDTO> cancel(Long idAppointment, AppointmentCancelRequestDTO appointmentCancelRequestDTO) {

        //Validar que el turno exista.
        Appointment appointment = appointmentRepository.findById(idAppointment)
                .orElseThrow(() -> new ConflictException("exception.appointmentNotFound.user",null,"exception.appointmentNotFound.log",new Object[]{idAppointment, "AppointmentService", "reschedule"}, LogLevel.ERROR));

        //Validar que esté en un estado "Cancelable"
        if(appointment.getStatus() != AppointmentStatus.RESERVED){
            throw new ConflictException("exception.cancel.conflictStatus.user",new Object[]{appointment.getStatus()},"exception.cancel.conflictStatus.log",new Object[]{idAppointment,appointment.getStatus(), "AppointmentService", "cancel"}, LogLevel.ERROR);

        }

        //Validar mínimo de horas de cancelación.
        validateMinimumHoursForCancel(appointmentCancelRequestDTO.requestSource(), appointment.getDate());

        //Actualizar estado Turno.
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointment.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
        appointment.setUpdatedAt(LocalDateTime.now());

        //persiste turno
        try{
            appointmentRepository.save(appointment);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentService", idAppointment, null, "cancel");
        }

        //Actualizar estado Historial y persiste.
        AppointmentStatusHistory appointmentStatusHistory = new AppointmentStatusHistory(
                appointment,
                AppointmentStatus.CANCELED,
                appointmentCancelRequestDTO.requestSource(),
                appointmentCancelRequestDTO.observation(),
                authenticatedUserService.getAuthenticatedUser(),
                LocalDateTime.now(),
                true
        );
        appointmentStatusHistoryService.save(appointmentStatusHistory);



        //Notificación por mail.
        sendAppointmentEmail(
                appointment,
                "appointmentService.cancel.notifyEmail.subject",
                "appointmentService.cancel.notifyEmail.title",
                "appointmentService.cancel.notifyEmail.message"
        );



        return new Response<>(
                true,
                messageSource.getMessage(
                        "appointmentService.cancel.ok",
                        null,
                        LocaleContextHolder.getLocale()
                ),
                new AppointmentResponseDTO(
                        appointment.getId(),
                        appointment.getDentist().getPerson().getLastName() + "," + appointment.getDentist().getPerson().getFirstName(),
                        appointment.getPatient().getPerson().getLastName() + "," + appointment.getPatient().getPerson().getFirstName(),
                        appointment.getDate(),
                        appointment.getStatus()
                )

        );

    }






    /**
     * Cancela todos los turnos con estado {@link AppointmentStatus#RESERVED} pertenecientes a un dentista en una fecha determinada.
     * <p>
     * Este método se utiliza ante situaciones imprevistas del dentista (emergencias,
     * enfermedad, ausencias repentinas, etc.) donde no puede atender durante un día
     * completo y es necesario cancelar todos sus turnos.
     * </p>
     * <ul>
     *     <li>Valida que la fecha ingresada sea posterior a la fecha actual. No permite cancelar turnos del mismo día.</li>
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
     * @throws DataBaseException si ocurre un error al persistir los cambios
     */

    @Override
    @LogAction(
            value = "appointmentService.logAction.cancelAllByDate.ok",
            args = {"#idDentist", "#result.data"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<Integer> cancelAllByDate(Long idDentist ,LocalDate date, AppointmentCancelRequestDTO appointmentCancelRequestDTO) {

        //Valída que la fecha sea mayor al día actual.
        if(!date.isAfter(LocalDate.now())){
            throw new BadRequestException("exception.validateMinimumHoursForCancel.user", null, "exception.validateMinimumHoursForCancel.log", new Object[]{date, LocalDate.now(), "AppointmentService", "cancelAllByDate"}, LogLevel.ERROR);
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
            appointmentStatusHistory.add(
                    new AppointmentStatusHistory(
                            a,
                            AppointmentStatus.CANCELED,
                            appointmentCancelRequestDTO.requestSource(),
                            appointmentCancelRequestDTO.observation(),
                            authenticatedUserService.getAuthenticatedUser(),
                            LocalDateTime.now(),
                            true
                    )
            );
        }

        //persiste
        List<Appointment> appointmentsSaved;
        try {
            appointmentsSaved = appointmentRepository.saveAll(appointments);
            appointmentStatusHistoryService.saveAll(appointmentStatusHistory);

        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentService", null, null, "cancelAllByDate");
        }


        // Notificación por mail
        appointments.forEach(a ->
                sendAppointmentEmail(
                        a,
                        "appointmentService.cancel.notifyEmail.subject",
                        "appointmentService.cancel.notifyEmail.title",
                        "appointmentService.cancel.notifyEmail.message"
                )
        );


        return new Response<>(
                true,
                messageSource.getMessage(
                        "appointmentService.cancelAllByDate.ok",
                        new Object[]{appointmentsSaved.size()},
                        LocaleContextHolder.getLocale()
                ),
                appointmentsSaved.size()

        );

    }


    /**
     * Obtiene la lista de turnos de un día para un dentista específico.
     * @param idDentist : id Dentista.
     * @param date : Fecha
     */
    @Override
    public List<Appointment> getAppointmentByDentistAndDate(Long idDentist, LocalDate date, AppointmentStatus status) {
        try{
            return appointmentRepository.findByDentistIdAndDateBetweenAndStatus(idDentist,date.atStartOfDay(),date.plusDays(1).atStartOfDay(), status);
        }
        catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentService", idDentist, "<- ID Dentist", "getAppointmentByDentistAndDate");
        }
    }

    /**
     * Obtiene la información de un turno. En caso de no encontrarlo arroja exception.
     *
     * @param idAppointment : id del turno
     */
    @Override
    public Appointment getById(Long idAppointment) {

        try{
            return appointmentRepository.findById(idAppointment)
                    .orElseThrow(() ->new NotFoundException("exception.appointment.notFound.user",null,"exception.appointment.notFound.log", new Object[]{idAppointment,"AppointmentService","getById"},LogLevel.ERROR));        }
        catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentService", idAppointment, "<- ID Appointment", "getById");
        }

    }


    /**
     * Envía email  a todos los contactos asociados al paciente del turno recibido, utilizando un template HTML.
     *
     * <p>Este método construye el correo a partir de la información del turno (`Appointment`):
     * <ul>
     *     <li>Nombre del paciente</li>
     *     <li>Fecha y hora del turno</li>
     *     <li>Profesional asignado</li>
     * </ul>
     * <p>El correo se envía a todos los emails registrados en el paciente
     *
     * <p>Este método encapsula la lógica común utilizada tanto para cancelación,
     * creación o reprogramación de turnos
     *
     * @param appointment         Turno del cual se extraerá la información para completar el template del correo.
     * @param subjectMessageKey   Key del archivo de mensajes para obtener el asunto del correo.
     * @param titleMessageKey     Key del archivo de mensajes para completar el título del template.
     * @param bodyMessageKey      Key del archivo de mensajes para completar el cuerpo principal del correo.
     *
     * @throws org.springframework.context.NoSuchMessageException
     *         Si alguna de las keys provistas no existe en el archivo de mensajes.
     *
     * @implNote Este método no maneja excepciones del envío de correo, dado que el
     *           `emailService.sendTemplateEmail()` es asíncrono por diseño.
     */
    private void sendAppointmentEmail(
            Appointment appointment,
            String subjectMessageKey,
            String titleMessageKey,
            String bodyMessageKey
    ) {

        // Obtener emails destino
        List<String> emails = appointment.getPatient()
                .getPerson()
                .getContactEmails()
                .stream()
                .map(ContactEmail::getEmail)
                .toList();

        // Construir el cuerpo del mail
        Map<String, Object> templateData = Map.of(
                TemplateEmail.title.toString(),
                messageSource.getMessage(titleMessageKey, null, LocaleContextHolder.getLocale()),

                TemplateEmail.patient.toString(),
                appointment.getPatient().getPerson().getFirstName() + ", " +
                        appointment.getPatient().getPerson().getLastName(),

                TemplateEmail.message.toString(),
                messageSource.getMessage(bodyMessageKey, null, LocaleContextHolder.getLocale()),

                TemplateEmail.date.toString(),
                appointment.getDate().toLocalDate()
                        .format(DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", new Locale("es", "ES"))),

                TemplateEmail.time.toString(),
                appointment.getDate().toLocalTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm")) + " hs",

                TemplateEmail.dentist.toString(),
                appointment.getDentist().getPerson().getLastName() + ", " +
                        appointment.getDentist().getPerson().getFirstName()
        );

        // Envio
        emailService.sendTemplateEmail(
                emails,
                messageSource.getMessage(subjectMessageKey, null, LocaleContextHolder.getLocale()),
                templateData
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
     * @param appointmentActionRequester indica quién solicita la reprogramación
     *                                 (PACIENTE o DENTISTA), lo cual determina
     *                                 qué parámetro de sistema se utiliza.
     *
     * @param appointmentDateTime      la fecha y hora original del turno a evaluar.
     *
     * @throws BadRequestException si la reprogramación no cumple con el tiempo
     *                             mínimo de anticipación definido por el negocio.
     */

    private void validateMinimumHoursForReschedule(AppointmentActionRequester appointmentActionRequester, LocalDateTime appointmentDateTime) {

        int minimumHours = Integer.parseInt(
                systemParameterService.getByKey
                        ((appointmentActionRequester == AppointmentActionRequester.PATIENT)
                                ? SystemParameterKey.APPOINTMENT_RESCHEDULE_PATIENT
                                : SystemParameterKey.APPOINTMENT_RESCHEDULE_DENTIST
                        )
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = appointmentDateTime.minusHours(minimumHours);

        if (limit.isBefore(now)) {
            throw new BadRequestException("exception.validateMinimumHoursForReschedule.user", null, "exception.validateMinimumHoursForReschedule.log", new Object[]{appointmentDateTime, limit, "AppointmentService", "validateMinimumHours"}, LogLevel.ERROR);
        }
    }





    /**
     * Valída que la cancelación de un turno se realice con la anticipación mínima requerida según quien lo solicite.
     * <p>El sistema define dos parámetros de configuración:
     * uno para solicitudes realizadas por pacientes y otro para solicitudes realizadas por profesionales del consultorio.
     * Dichos valores,expresados en horas, se utilizan para determinar si la reprogramación está permitida.</p>
     * <p>
     * La validación consiste en restar ese tiempo mínimo a la fecha y hora del turno. Si el resultado es anterior al momento actual, significa que
     * la acción se está intentando fuera del tiempo permitido y se lanza una {@link BadRequestException}.</p>
     * @param appointmentActionRequester indica quién solicita la reprogramación
     *                                 (PACIENTE o DENTISTA), lo cual determina
     *                                 qué parámetro de sistema se utiliza.
     *
     * @param appointmentDateTime      la fecha y hora original del turno a evaluar.
     *
     * @throws BadRequestException si la reprogramación no cumple con el tiempo
     *                             mínimo de anticipación definido por el negocio.
     */
    private void validateMinimumHoursForCancel(AppointmentActionRequester appointmentActionRequester, LocalDateTime appointmentDateTime) {

        int minimumHours = Integer.parseInt(
                systemParameterService.getByKey
                        ((appointmentActionRequester == AppointmentActionRequester.PATIENT)
                                ? SystemParameterKey.APPOINTMENT_CANCEL_PATIENT
                                : SystemParameterKey.APPOINTMENT_CANCEL_DENTIST
                        )
        );

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = appointmentDateTime.minusHours(minimumHours);

        if (limit.isBefore(now)) {
            throw new BadRequestException("exception.validateMinimumHoursForCancel.user", null, "exception.validateMinimumHoursForCancel.log", new Object[]{appointmentDateTime, limit, "AppointmentService", "validateMinimumHours"}, LogLevel.ERROR);
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

    private void validateAvailabilityForAppointment(Long idDentist, LocalDateTime appointmentDateTime) {

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
                if(match){
                    break;
                }
            }

        }

        if (!match) {
            throw new ConflictException("exception.appointmentService.validateAvailability.user", null,"exception.appointmentService.validateAvailability.log", new Object[]{idDentist,appointmentDateTime ,"Appointment Service", "validateAvailability"}, LogLevel.ERROR);
        }

    }


}

