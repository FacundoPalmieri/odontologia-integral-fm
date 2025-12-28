package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistAvailabilityResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.WorkingDayDTO;
import com.odontologiaintegralfm.feature.appointment.core.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.core.enums.OriginConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistAvailability;
import com.odontologiaintegralfm.feature.appointment.core.repository.IDentistAvailabilityRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IConflictManagerService;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IDentistAvailabilityService;
import com.odontologiaintegralfm.feature.appointment.core.util.CalendarUtils;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.interfaces.IDentistService;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.enums.LogType;
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
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import static com.odontologiaintegralfm.feature.appointment.core.util.CalendarUtils.findFirstMatchingDate;


/**
 * @author [Facundo Palmieri]
 */
@Service
@Slf4j
public class DentistAvailabilityService implements IDentistAvailabilityService {


    private final IDentistService dentistService;
    private final IDentistAvailabilityRepository dentistAvailabilityRepository;
    private final MessageSource messageSource;
    private final AuthenticatedUserService authenticatedUserService;
    private final IConflictManagerService conflictManagerService;

    public DentistAvailabilityService(
            IDentistService dentistService,
            IDentistAvailabilityRepository dentistAvailabilityRepository,
            MessageSource messageSource,
            AuthenticatedUserService authenticatedUserService,
            IConflictManagerService conflictManagerService
    ) {
        this.dentistService = dentistService;
        this.dentistAvailabilityRepository = dentistAvailabilityRepository;
        this.messageSource = messageSource;
        this.authenticatedUserService = authenticatedUserService;
        this.conflictManagerService = conflictManagerService;
    }




    /**
     * Crea o actualiza la disponibilidad de turnos de un dentista.
     *
     * <p>Este método realiza las siguientes acciones:</p>
     * <ol>
     *     <li>Valida que el dentista exista.</li>
     *     <li>Deshabilita las disponibilidades actuales activas para evitar inconsistencias con la nueva jornada.</li>
     *     <li>Crea y persiste las nuevas disponibilidades según la lista de {@link WorkingDayDTO} recibida.</li>
     *     <li>Asocia cada disponibilidad creada con un posible origen de conflicto ({@link OriginConflict AVAILABILITY}).</li>
     *     <li>Verifica si existen turnos existentes que se vean afectados por la nueva disponibilidad, generando {@link AppointmentConflictResponseDTO}.</li>
     *     <li>Construye y retorna un {@link DentistAvailabilityResponseDTO} con las nuevas disponibilidades y los conflictos detectados.</li>
     * </ol>
     *
     * <p>Notas importantes:</p>
     * <ul>
     *     <li>La jornada siempre entra en vigencia a partir del día siguiente de la actualización para evitar superposición con jornadas activas y turnos ya generados.</li>
     *     <li>Si existen disponibilidades previas, se deshabilitan antes de persistir las nuevas.</li>
     *     <li>Las disponibilidades contienen información de auditoría: {@code createdAt} y {@code createdBy}.</li>
     * </ul>
     *
     * @param id   ID del dentista cuya disponibilidad se actualizará. No puede ser {@code null}.
     * @param days Lista de {@link WorkingDayDTO} que define la nueva jornada laboral.
     * @return {@link Response} con un {@link DentistAvailabilityResponseDTO} que contiene:
     * <ul>
     *     <li>El ID del dentista.</li>
     *     <li>La lista de nuevas disponibilidades con horarios, recurrencias y duración de turno.</li>
     *     <li>Los conflictos detectados respecto a turnos existentes ({@link AppointmentConflictResponseDTO}).</li>
     * </ul>
     * @throws ConflictException si el dentista no existe en la base de datos.
     * @throws DataBaseException si ocurre un error de acceso a la base de datos o fallo en la transacción.
     */

    @LogAction(
            value = "dentistAvailabilityService.SystemLog.update",
            args = {"#id"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    @Override
    @Transactional
    public Response<DentistAvailabilityResponseDTO> create(Long id, List<WorkingDayDTO> days) {
        try {
            //Valida que exista dentista
            Dentist dentist = dentistService.getById(id)
                    .orElseThrow(() -> new ConflictException("exception.dentistNotFound.user", null, "exception.dentistNotFound.log", new Object[]{id, "Dentist Availability Service", "update"}, LogLevel.ERROR));


            //Buscar si existe relación:
            List<DentistAvailability> dentistAvailabilityExisting = dentistAvailabilityRepository.findAllByDentistIdAndEnabledTrue(id);


            //Si la lista no está vacía existe relación previa entre dentista y disponibilidad. Se deshabilitan las mismas.
            if (!dentistAvailabilityExisting.isEmpty()) {
                disabledAvailability(dentistAvailabilityExisting);
            }


            //Se persiste la nueva relación.
            List<DentistAvailability> newAvailabilities = days.stream()
                    .map(dto -> new DentistAvailability(
                            dentist,
                            dto.getDayName(),
                            dto.getSpecificDate(),
                            dto.getRecurrence(),
                            dto.getStartTime(),
                            dto.getEndTime(),
                            dto.getAppointmentDuration(),
                            (dto.getSpecificDate() == null) ? CalendarUtils.findFirstMatchingDate(LocalDate.now().plusDays(1), dto.getDayName().toDayOfWeek()) : dto.getSpecificDate(),
                            LocalDateTime.now(),
                            authenticatedUserService.getAuthenticatedUser(),
                            true
                    ))
                    .toList();


            List<DentistAvailability> dentistAvailabilitiesSaved = dentistAvailabilityRepository.saveAll(newAvailabilities);


            //Se completa el mapeo del DTO con los datos para posible origen de conflicto.
            for (int x = 0; x < newAvailabilities.size(); x++) {
                WorkingDayDTO day = days.get(x);
                DentistAvailability dentistAvailability = dentistAvailabilitiesSaved.get(x);

                day.setEffectiveDate(dentistAvailability.getEffectiveDate());
                day.setIdOriginConflict(dentistAvailability.getId());
                day.setOriginConflict(OriginConflict.DENTIST_AVAILABILITIES);

            }


            //Verificar si hay turnos existentes que se vean afectados.
            List<AppointmentConflictResponseDTO> appointmentsConflict = conflictManagerService.verifyConflictsByDentistAvailability(id, days);

            String messageUser = messageSource.getMessage("dentistAvailabilityService.update.ok", null, LocaleContextHolder.getLocale());

            return new Response<>(true, messageUser, DentistAvailabilityResponseDTO.build(dentistAvailabilitiesSaved,appointmentsConflict));
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistAvailabilityService", id, null, "update");
        }
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
     * Método para obtener el tiempo de duración de un turno por ID de dentista.
     *
     * @param idDentist
     */
    @Override
    public Integer getAppointmentDuration(Long idDentist) {
        return dentistAvailabilityRepository.findAppointmentDurationByDentistId(idDentist);
    }


    /**
     * Método privado que valída que la fecha de inicio y fin cubra al menos la parametrización de la duración de un turno.
     * @param idDentist: Id Dentista
     * @param startTime: Hora inicio jornada de feriado
     * @param endTime    : Hora fin jornada de feriado
     */
    public boolean validateDurationLessThanAppointmentDuration(Long idDentist, LocalTime startTime, LocalTime endTime) {
        Integer appointmentDuration = getAppointmentDuration(idDentist);
        long holidayDurationMinutes = Duration.between(startTime, endTime).toMinutes();

        return holidayDurationMinutes >= appointmentDuration;
    }



    /**
     * Método que verifica si una fecha dada es coincidente con la alguna jornada laboral de dentista.
     *
     * @param dentist : id Dentist.
     * @param date    : Fecha a consultar
     * @return : La jornada laboral.
     */
    @Override
    public DentistAvailability getDentistAvailabilityByDate(Long dentist, LocalDate date) {

        //Obtiene todas las jornadas laborales.
        List<DentistAvailability> dentistAvailabilities = dentistAvailabilityRepository.findAllByDentistIdAndEnabledTrue(dentist);

        if (dentistAvailabilities.isEmpty()) {
            throw new ConflictException("exception.dentistAvailability.empty.user", null, "exception.dentistAvailability.empty.log", new Object[]{dentist, "DentistAvailabilityService", "getDentistAvailabilityByDate"}, LogLevel.ERROR);
        }

        //Verifica jornada específica, ya que si es así solo puede haber un elemento en la lista.
        if (((dentistAvailabilities.get(0).getSpecificDate())!= null) && dentistAvailabilities.get(0).getSpecificDate().equals(date)) {
            return dentistAvailabilities.get(0);
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
     * Valída que un bloqueo de calendario propuesto coincida con la jornada laboral del dentista.
     *
     * <p>Dependiendo del tipo de recurrencia del bloqueo:</p>
     * <ul>
     *     <li><b>DAILY:</b> El bloqueo debe cubrir todos los días laborales del dentista dentro del rango de fechas.</li>
     *     <li><b>WEEKLY / MONTHLY / YEARLY / NONE:</b> El bloqueo debe coincidir con al menos un día laboral del dentista.</li>
     * </ul>
     *
     * <p>El método obtiene las disponibilidades del dentista y verifica si las fechas y horarios del bloqueo
     * están completamente cubiertos según la recurrencia.</p>
     *
     * @param idDentist Id del dentista cuyo calendario se valida.
     * @param blocksDate Fechas de bloqueos
     * @param startTimeBlock Hora de inicio del bloqueo.
     * @param endTimeBlock Hora de fin del bloqueo.
     *
     * @throws BadRequestException Si el bloqueo no cumple con la cobertura requerida según la recurrencia y jornada del dentista.
     */
    @Override
    public void validateCoverage(Long idDentist,List<LocalDate> blocksDate, LocalTime startTimeBlock, LocalTime endTimeBlock) {

        List<DentistAvailability> availabilities = dentistAvailabilityRepository.findAllByDentistIdAndEnabledTrue(idDentist);

        for (LocalDate blockDate : blocksDate) {

            boolean covered = false;

            for (DentistAvailability availability : availabilities) {

                // 1. Día de la semana
                if (availability.getKeyName() != DayName.fromDayOfWeek(blockDate.getDayOfWeek())) {
                    continue;
                }

                // 2. Match por recurrencia
                if (!availability.getRecurrence().matches(availability.getEffectiveDate(), blockDate)){
                    continue;
                }

                // 3. Rango horario
                if (availability.getStartTime().isAfter(startTimeBlock) || availability.getEndTime().isBefore(endTimeBlock)) {
                    continue;
                }

                covered = true;
                break;
            }

            if (!covered) {
                throw new BadRequestException("exception.dentistLockCalendarService.validateDentistAvailability.user", null, "exception.dentistLockCalendarService.validateDentistAvailability.log", new Object[]{idDentist, blockDate}, LogLevel.ERROR);
            }
        }
    }




    /**
     * Valída que una fecha/hora esté dentro de la jornada laboral del dentista.
     * @param idDentist : idDentista
     * @param appointmentDateTime : Fecha y hora a evaluar.
     */
    @Override
    public void isDateTimeWithinAvailability(Long idDentist, LocalDateTime appointmentDateTime) {

        //Obtener disponibilidad
        List<DentistAvailability> dentistAvailability = dentistAvailabilityRepository.findAllByDentistIdAndEnabledTrue(idDentist);

        //Separo Dias de horas
        LocalDate date = appointmentDateTime.toLocalDate();

        boolean match = false;


        if(dentistAvailability.size() == 1){
            match = CalendarUtils.isDateTimeWithinEvent(appointmentDateTime, dentistAvailability.get(0).getSpecificDate(), dentistAvailability.get(0).getSpecificDate(), null,dentistAvailability.get(0).getStartTime() , dentistAvailability.get(0).getEndTime(),null);
        }else{
            for (DentistAvailability d : dentistAvailability) {
                //Obtiene los Date de la jornada de los próximos 7 días.
                LocalDate startDate = findFirstMatchingDate(LocalDate.now().plusDays(1), d.getKeyName().toDayOfWeek());
                match = CalendarUtils.isDateTimeWithinEvent(appointmentDateTime, startDate, date, d.getKeyName().toDayOfWeek(), d.getStartTime(), d.getEndTime(), d.getRecurrence());
                if(match){
                    break;
                }
            }

        }

        if (!match) {
            throw new ConflictException("exception.dentistAvailabilityService.isDateTimeWithinAvailability.user", null,"exception.dentistAvailabilityService.isDateTimeWithinAvailability.log", new Object[]{idDentist,appointmentDateTime ,"dentistAvailabilityService", "isDateTimeWithinAvailability"}, LogLevel.ERROR);
        }

    }





    /**
     * Deshabilita una jornada laboral
     */
    private void disabledAvailability(List<DentistAvailability> dentistAvailability) {

        List<DentistAvailability> disabledAvailability = new ArrayList<>();

        for (DentistAvailability da : dentistAvailability) {
            da.setEnabled(false);
            da.setDisabledBy(authenticatedUserService.getAuthenticatedUser());
            da.setDisabledAt(LocalDateTime.now());
            disabledAvailability.add(da);
        }

        dentistAvailabilityRepository.saveAll(disabledAvailability);
    }





}
