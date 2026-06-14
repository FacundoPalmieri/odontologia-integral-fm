package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto.AppointmentCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentActionRequester;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.repository.IAppointmentRepository;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.model.DentistAvailability;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.service.IDentistAvailabilityService;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.service.IDentistHolidayService;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.service.IDentistCalendarLockService;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.interfaces.IDentistService;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
import com.odontologiaintegralfm.feature.patient.core.service.interfaces.IPatientService;
import com.odontologiaintegralfm.infrastructure.systemparameter.enums.SystemParameterKey;
import com.odontologiaintegralfm.infrastructure.systemparameter.service.implement.SystemParameterService;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentDomainService {

    private final IAppointmentRepository appointmentRepository;
    private final IDentistService dentistService;
    private final IPatientService patientService;
    private final IDentistAvailabilityService dentistAvailabilityService;
    private final IDentistHolidayService dentistHolidayService;
    private final IDentistCalendarLockService dentistCalendarLockService;
    private final AuthenticatedUserService authenticatedUserService;
    private final SystemParameterService systemParameterService;





    public AppointmentDomainService(
            IAppointmentRepository appointmentRepository,
            IDentistService dentistService,
            IPatientService patientService,
            IDentistAvailabilityService dentistAvailabilityService,
            IDentistHolidayService dentistHolidayService,
            IDentistCalendarLockService dentistCalendarLockService,
            AuthenticatedUserService authenticatedUserService,
            SystemParameterService systemParameterService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.dentistService = dentistService;
        this.patientService = patientService;
        this.dentistAvailabilityService = dentistAvailabilityService;
        this.dentistHolidayService = dentistHolidayService;
        this.dentistCalendarLockService = dentistCalendarLockService;
        this.authenticatedUserService = authenticatedUserService;
        this.systemParameterService = systemParameterService;
    }




    /**
     * Método privado que valida y crear un turno.
     * Este método es compartido por el "create" y "rescheduled"
     * @param appointmentCreateRequestDTO : Request
     * @return : Appointment
     */
    public Appointment validateAndBuildAppointment(AppointmentCreateRequestDTO appointmentCreateRequestDTO) {

        //Validar dentista
        Dentist dentist = dentistService.getById(appointmentCreateRequestDTO.getIdDentist())
                .orElseThrow(() -> new ConflictException("exception.dentistNotFound.user", null, "exception.dentistNotFound.log", new Object[]{appointmentCreateRequestDTO.getIdDentist(), "AppointmentDomainService", "validateAndBuildAppointment"}, LogLevel.ERROR));

        //Validar patient
        Patient patient = patientService.findById(appointmentCreateRequestDTO.getIdPatient());

        //Validar que no haya otro turno
        Optional<Appointment> appointmentExisting = appointmentRepository.findByDentistIdAndDate(dentist.getId(), appointmentCreateRequestDTO.getDateTime());
        if (appointmentExisting.isPresent()) {
            throw new ConflictException("exception.appointmentConflict.user", null, "exception.appointmentConflict.log", new Object[]{appointmentExisting.get().getId(), "AppointmentDomainService", "validateAndBuildAppointment"}, LogLevel.ERROR);
        }


        //Validar jornada del dentista para la fecha y hora enviada.
        dentistAvailabilityService.isDateTimeWithinAvailability(dentist.getId(), appointmentCreateRequestDTO.getDateTime());


        //Validar feriado para la fecha enviada.
        dentistHolidayService.validateDentistIdAndDate(dentist.getId(), appointmentCreateRequestDTO.getDateTime().toLocalDate());

        //Validar bloqueos para la fecha y hora enviada.
        dentistCalendarLockService.validateByIdDentistAndDateTime(dentist.getId(), appointmentCreateRequestDTO.getDateTime());

        //Normaliza fecha.
        normalizeTime(appointmentCreateRequestDTO);

        //Crear turno
        Appointment appointment = Appointment.build(patient, dentist, appointmentCreateRequestDTO.getDateTime(), AppointmentStatus.RESERVED);
        //Campos auditoria
        appointment.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setEnabled(true);

        return appointment;
    }


    public void normalizeTime(AppointmentCreateRequestDTO appointment){

        //Obtiene las jornadas laborales del dentista.
        List<DentistAvailability> dentistAvailabilities = dentistAvailabilityService.findById(appointment.getIdDentist());

        //Obtiene la disponibilidad que se encuentran dentro del turno elegido
        DentistAvailability dentistAvailability = dentistAvailabilityService.getDentistAvailabilityByDate(appointment.getIdDentist(), appointment.getDateTime().toLocalDate(), dentistAvailabilities);

        //Obtiene el slot
        int slot = dentistAvailability.getAppointmentDuration();


        //Calcula los minutos del turno para que coincidan con el inicio del slot.
        int minute = appointment.getDateTime().getMinute(); //Obtiene minutos del turno enviado en la request.
        int minuteNormalize = (minute/slot) * slot;


        LocalDateTime normalizedDateTime = appointment.getDateTime()
                .withMinute(minuteNormalize)
                .withSecond(0)
                .withNano(0);


        appointment.setDateTime(normalizedDateTime);

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

    public void validateMinimumHoursForReschedule(AppointmentActionRequester appointmentActionRequester, LocalDateTime appointmentDateTime) {

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
            throw new BadRequestException("exception.validateMinimumHoursForReschedule.user", null, "exception.validateMinimumHoursForReschedule.log", new Object[]{appointmentDateTime, limit, "validateAndBuildAppointment", "validateMinimumHoursForReschedule"}, LogLevel.ERROR);
        }
    }



    /**
     * Valída que la cancelación de un turno se realice con la anticipación mínima requerida según quien lo solicite.
     * <p>El sistema define dos parámetros de configuración:
     * uno para solicitudes realizadas por pacientes y otro para solicitudes realizadas por profesionales del consultorio.
     * Dichos valores, expresados en horas, se utilizan para determinar si la reprogramación está permitida.</p>
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
    public void validateMinimumHoursForCancel(AppointmentActionRequester appointmentActionRequester, LocalDateTime appointmentDateTime) {

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
            throw new BadRequestException("exception.validateMinimumHoursForCancel.user", null, "exception.validateMinimumHoursForCancel.log", new Object[]{appointmentDateTime, limit, "validateAndBuildAppointment", "validateMinimumHoursForCancel"}, LogLevel.ERROR);
        }
    }



}
