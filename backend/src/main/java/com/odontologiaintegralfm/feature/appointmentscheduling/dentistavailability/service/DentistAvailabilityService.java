package com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.dto.DentistAvailabilityContextInternalDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.dto.DentistAvailabilityResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.dto.WorkingDayDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums.OriginConflict;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.model.DentistAvailability;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.repository.IDentistAvailabilityRepository;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.util.CalendarUtils;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.interfaces.IDentistService;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.dto.Response;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.time.*;
import java.util.*;

import static com.odontologiaintegralfm.feature.appointmentscheduling.calendar.util.CalendarUtils.isDateTimeWithinBreak;



@Service
@Slf4j
public class DentistAvailabilityService implements IDentistAvailabilityService {


    private final IDentistService dentistService;
    private final IDentistAvailabilityRepository dentistAvailabilityRepository;
    private final MessageSource messageSource;
    private final AuthenticatedUserService authenticatedUserService;

    public DentistAvailabilityService(
            IDentistService dentistService,
            IDentistAvailabilityRepository dentistAvailabilityRepository,
            MessageSource messageSource,
            AuthenticatedUserService authenticatedUserService
    ){
        this.dentistService = dentistService;
        this.dentistAvailabilityRepository = dentistAvailabilityRepository;
        this.messageSource = messageSource;
        this.authenticatedUserService = authenticatedUserService;
    }


    /**
     * Método de dominio para mapear, y persistir la entidad.
     *
     * @param availability: disponibilidad a persistir.
     */
    @Override
    public List<DentistAvailability> create( List<WorkingDayDTO> days,DentistAvailabilityContextInternalDTO availability, UserSec userSec) {

        //Mapeo a entidad
        List<DentistAvailability> newAvailabilities = entityFromDto(days, availability, userSec);

        //Se persiste la nueva relación.
         return  dentistAvailabilityRepository.saveAll(newAvailabilities);
    }





    /**
     * Método para obtener la jornada laboral de un dentista.
     * - Inicio de jornada.
     * - Fin de jornada.
     * - Duración de turno.
     *
     * @param id : Id del dentista
     */
    @Override
    public Response<DentistAvailabilityResponseDTO> get(Long id) {
        try {


            //Valida que exista dentista
            Dentist dentist = dentistService.getById(id)
                    .orElseThrow(() -> new ConflictException("exception.dentistNotFound.user", null, "exception.dentistNotFound.log", new Object[]{id, "Dentist Availability Service", "update"}, LogLevel.ERROR));

            //Buscar si existe relación:
            List<DentistAvailability> dentistAvailability = dentistAvailabilityRepository.findAllByDentistIdAndEnabledTrue(id);

            if (dentistAvailability.isEmpty()) {
                String messageUser = messageSource.getMessage("dentistAvailabilityService.notFound.user", null, LocaleContextHolder.getLocale());
                return new Response<>(true, messageUser, null);
            }

            return new Response<>(true, null, DentistAvailabilityResponseDTO.build(dentistAvailability,null));

        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistAvailabilityService", id, null, "get");
        }
    }


    /**
     * Método Interno para obtener la jornada laboral de un dentista.
     */
    @Override
    public List<DentistAvailability> getByIdInternal(Long idDentist) {
        try {
            return dentistAvailabilityRepository.findAllByDentistIdAndEnabledTrue(idDentist);
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistAvailabilityService", idDentist, null, "getByIdInternal");
        }
    }









    /**
     * Método para deshabilitar jornadas laborales.
     *
     * @param dentistAvailability      : Lista de disponibilidades (días)
     * @param authenticatedUserService : Usuario autenticado.
     * @param now                      : Fecha y hora actual.
     */
    @Override
    public void disabledAvailability(List<DentistAvailability> dentistAvailability, AuthenticatedUserService authenticatedUserService, LocalDateTime now) {
        List<DentistAvailability> disabledAvailability = new ArrayList<>();

        for (DentistAvailability da : dentistAvailability) {
            da.setEnabled(false);
            da.setDisabledBy(authenticatedUserService.getAuthenticatedUser());
            da.setDisabledAt(now);
            disabledAvailability.add(da);
        }

        dentistAvailabilityRepository.saveAll(disabledAvailability);
    }


    /**
     * Método que verifica si una fecha dada es coincidente con la alguna jornada laboral de dentista.
     *
     * @param dentist : id Dentist.
     * @param date    : Fecha a consultar
     * @return : La jornada laboral.
     */
    @Override
    public DentistAvailability getDentistAvailabilityByDate(Long dentist, LocalDate date,List<DentistAvailability> dentistAvailabilities) {

        //Verifica jornada específica, ya que si es así solo puede haber un elemento en la lista.
        if (((dentistAvailabilities.get(0).getSpecificDate())!= null)) {
            if(dentistAvailabilities.get(0).getSpecificDate().equals(date)){
                return dentistAvailabilities.get(0);
            }

        }

        //Si la jornada no es específica, recorremos todas las jornadas y verificamos recurrencia.
        for (DentistAvailability da : dentistAvailabilities) {
            if (da.getRecurrence() != null) {
                if (da.getRecurrence().matches(da.getEffectiveDate(), date)) {
                    return da;
                }
            }
        }
        return null;
    }




    /**
     * Valída que una fecha/hora esté dentro de la jornada laboral del dentista.
     *
     * @param idDentist           : idDentista
     * @param appointmentDateTime : Fecha y hora a evaluar.
     */
    @Override
    public void isDateTimeWithinAvailability(Long idDentist, LocalDateTime appointmentDateTime) {

        //Obtener disponibilidad
        List<DentistAvailability> dentistAvailability = dentistAvailabilityRepository.findAllByDentistIdAndEnabledTrue(idDentist);

        //Separo Dias de horas
        LocalDate date = appointmentDateTime.toLocalDate();
        LocalTime time = appointmentDateTime.toLocalTime();

        boolean match = false;

        for (DentistAvailability availability : dentistAvailability) {


            // 1. Validar jornada laboral
            boolean withinWorkingHours = CalendarUtils.isDateTimeWithinEvent(
                    appointmentDateTime,
                    availability.getEffectiveDate(),      // inicio real de vigencia
                    availability.getEffectiveDate(),      // fecha a evaluar
                    availability.getKeyName() != null ? availability.getKeyName().toDayOfWeek() : null,
                    availability.getStartTime(),
                    availability.getEndTime(),
                    availability.getRecurrence()
            );

            if (!withinWorkingHours) {
                continue;
            }


            // 2. Validar break (si existe)
            if (availability.getBreakStartTime() != null && availability.getBreakEndTime() != null) {
                if (isDateTimeWithinBreak(time, availability.getBreakStartTime(), availability.getBreakEndTime())) {
                    throw new ConflictException("exception.dentistAvailabilityService.isDateTimeWithinAvailability.user", null, "exception.dentistAvailabilityService.isDateTimeWithinAvailability.log", new Object[]{idDentist, appointmentDateTime, "dentistAvailabilityService", "isDateTimeWithinAvailability"}, LogLevel.ERROR);

                }
            }

            // 3. Si llega acá, la jornada es válida
            match = true;
            break;
        }

        if (!match) {
            throw new ConflictException("exception.dentistAvailabilityService.isDateTimeWithinAvailability.user", null, "exception.dentistAvailabilityService.isDateTimeWithinAvailability.log", new Object[]{idDentist, appointmentDateTime, "dentistAvailabilityService", "isDateTimeWithinAvailability"}, LogLevel.ERROR);
        }

    }



    /**
     * Método privado del servicio que permite mapea cada jornada laboral de la request a una entidad.
     * @param days : DTO con la jornada
     * @param dentistAvailabilityExisting : DTO interno del servicio que posea un dentista y una lista de disponibilidades.
     */
    @Override
    public List<DentistAvailability> entityFromDto(List<WorkingDayDTO> days, DentistAvailabilityContextInternalDTO dentistAvailabilityExisting, UserSec authenticatedUserService) {

        return days.stream()
                .map(dto -> {

//                    //Valída que la hora de inicio y fin cubra al menos la parametrización de la duración de un turno.
//                    if(validateDurationLessThanAppointmentDuration(dto.getStartTime(),dto.getEndTime(), dto.getAppointmentDuration())){
//                        throw new ConflictException("exception.dentistHolidayService.create.validateDurationLessThanAppointmentDuration.user",null,"exception.dentistHolidayService.create.validateDurationLessThanAppointmentDuration.log", new Object[]{dto.getStartTime(),dto.getEndTime(), dto.getAppointmentDuration(),"DentistAvailabilityService","entityFromDto"},LogLevel.ERROR);
//                    }


                    DentistAvailability dentistAvailability = DentistAvailability.build(dentistAvailabilityExisting.dentist(), dto);

                    //Campos auditoría.
                    dentistAvailability.setCreatedBy(authenticatedUserService);
                    dentistAvailability.setCreatedAt(LocalDateTime.now());
                    dentistAvailability.setEnabled(true);

                    return dentistAvailability;
                })
                .toList();

    }



    /**
     * Valída lo siguiente:
     * - Fin del break no puede ser anterior al inicio.
     * - Si un campo tiene datos, el otro también.
     */
    @Override
    public void validateBreak(List <DentistAvailability> dentistAvailability) {
        dentistAvailability.stream()
                .forEach(da -> {
                    if (da.getBreakStartTime() != null && da.getBreakEndTime() != null) {
                        if (da.getBreakEndTime().isBefore(da.getBreakStartTime())) {
                            throw new BadRequestException("exception.dentistAvailability.validateBreak.endBeforeStart.user",null,"exception.dentistAvailability.validateBreak.endBeforeStart.log", new Object[]{da.getId(),da.getBreakStartTime(),da.getBreakEndTime(),"dentistAvailability","validateBreak"}, LogLevel.ERROR);

                        }
                    }

                    if (da.getBreakStartTime() != null && da.getBreakEndTime() == null) {
                        throw new BadRequestException("exception.dentistAvailability.validateBreak.breakEndEmpty.user",null,"exception.dentistAvailability.validateBreak.breakEndEmpty.log", new Object[]{da.getId(),da.getBreakStartTime(),da.getBreakEndTime(),"dentistAvailability","validateBreak"}, LogLevel.ERROR);
                    }

                    if (da.getBreakStartTime() == null && da.getBreakEndTime() != null) {
                        throw new BadRequestException("exception.dentistAvailability.validateBreak.breakStartEmpty.user",null,"exception.dentistAvailability.validateBreak.breakStartEmpty.log", new Object[]{da.getId(),da.getBreakStartTime(),da.getBreakEndTime(),"dentistAvailability","validateBreak"}, LogLevel.ERROR);
                    }

                });
    }





    /**
     * Detecta y genera conflictos de turnos que se encuentran fuera de la nueva jornada laboral de un dentista.
     * <p>
     * Este método compara cada turno futuro del dentista con la lista de {@link WorkingDayDTO} que define la nueva
     * disponibilidad laboral. Para cada turno que no se encuentra dentro de los días y horarios permitidos,
     * se genera un objeto {@link AppointmentConflict} indicando que está fuera de horario.
     * </p>
     *
     * @param appointments Lista de {@link Appointment} que representa los turnos futuros del dentista.
     * @param workingDays  Lista de {@link WorkingDayDTO} que define la nueva jornada laboral a evaluar.
     * @return Lista de {@link AppointmentConflict} representando los turnos que no se ajustan a la nueva jornada laboral.
     */
    @Override
    public List<AppointmentConflict> evaluateAppointmentDentistAvailability(List<Appointment> appointments, List<WorkingDayDTO> workingDays) {

        List<AppointmentConflict> conflicts = new ArrayList<>();

        for (Appointment appointment : appointments) {
            boolean covered = false;
            Long idOriginConflict = null;
            OriginConflict originConflict = null;


            for (WorkingDayDTO workingDay : workingDays) {

                LocalDate startDate;
                LocalDate endDate;
                DayOfWeek day;


                // Jornada recurrente (semanal)
                if (workingDay.getSpecificDate() == null) {

                    startDate = workingDay.getEffectiveDate();

                    //Fecha fín (Hasta donde evalúa). Último turno encontrado.
                    endDate = appointments.stream()
                            .map(a -> a.getDate().toLocalDate())
                            .max(Comparator.naturalOrder())
                            .orElse(null);

                    day = workingDay.getDayName().toDayOfWeek();
                }





                // Jornada por fecha específica
                else {
                    startDate = workingDay.getEffectiveDate();
                    endDate = workingDay.getEffectiveDate();
                    day = null;
                }




                // Evalúa si el turno NO está cubierto por la jornada.
                if (!CalendarUtils.isDateTimeWithinEvent(appointment.getDate(), startDate, endDate, day, workingDay.getStartTime(), workingDay.getEndTime(), workingDay.getRecurrence())) {

                    // Guardamos último origen posible (si ninguna cubre)
                    idOriginConflict = workingDay.getIdOriginConflict();
                    originConflict = workingDay.getOriginConflict();

                    continue;
                }


                // A partir de acá el turno cae dentro de la jornada

                //Evalúa si hay break
                if (workingDay.getBreakStartTime() != null && workingDay.getBreakEndTime() != null) {

                    //Si el turno cae dentro del break, no está cubierta.
                    if (CalendarUtils.isDateTimeWithinBreak(appointment.getDate().toLocalTime(), workingDay.getBreakStartTime(), workingDay.getBreakEndTime())) {

                        // Guardamos último origen posible (si ninguna cubre)
                        idOriginConflict = workingDay.getIdOriginConflict();
                        originConflict = workingDay.getOriginConflict();

                        continue; //corta el flujo, itera al siguiente.
                    }
                }

                //Si no hay break, la jornada está totalmente cubierta.
                covered = true;


            }

            if (!covered) {

                AppointmentConflict ac = AppointmentConflict.build(
                        appointment,
                        idOriginConflict,
                        originConflict.name()

                );
                ac.setCreatedAt(LocalDateTime.now());
                ac.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
                ac.setEnabled(true);

                conflicts.add(ac);
            }
        }


        return conflicts;
    }










}
