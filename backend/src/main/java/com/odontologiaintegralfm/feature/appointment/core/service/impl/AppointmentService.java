package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.Holiday;
import com.odontologiaintegralfm.feature.appointment.catalogs.service.HolidayService;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentCreateResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistAvailability;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLock;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLockDetail;
import com.odontologiaintegralfm.feature.appointment.core.repository.IAppointmentRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentService;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.implement.DentistService;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
import com.odontologiaintegralfm.feature.patient.core.service.implement.PatientService;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
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


    /**
     * @param appointmentCreateRequestDTO
     * @return
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

        Appointment appointmentSaved = appointmentRepository.save(appointment);

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

