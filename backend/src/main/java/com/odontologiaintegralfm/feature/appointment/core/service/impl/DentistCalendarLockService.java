package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.CalendarLockType;
import com.odontologiaintegralfm.feature.appointment.catalogs.service.ICalendarLockTypeService;
import com.odontologiaintegralfm.feature.appointment.catalogs.service.IHolidayService;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentConflictReason;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLock;
import com.odontologiaintegralfm.feature.appointment.core.repository.IDentistCalendarLockRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IDentistLockCalendarService;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.interfaces.IDentistService;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.shared.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DentistCalendarLockService implements IDentistLockCalendarService {

    @Autowired
    private IDentistService dentistService;

    @Autowired
    private ICalendarLockTypeService calendarLockTypeService;

    @Autowired
    private IHolidayService holidayService;

    @Autowired
    private DentistAvailabilityService dentistAvailabilityService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private AppointmentConflictService appointmentConflictService;

    @Autowired
    private IDentistCalendarLockRepository dentistLockCalendarRepository;

    @Qualifier("messageSource")
    @Autowired
    private MessageSource messageSource;


    /**
     * Crea un nuevo evento de bloqueo en la agenda de un dentista.
     *
     * <p>Este método permite registrar un bloqueo de agenda, validando previamente la existencia del dentista,
     * el tipo de evento, la recurrencia y posibles conflictos con feriados, jornadas laborales o turnos reservados.</p>
     *
     * <p>Si se detectan turnos existentes dentro del rango de bloqueo, estos se registran como conflictos
     * mediante {@code AppointmentConflictService}, pero no impiden la creación del bloqueo.
     * La respuesta incluirá una bandera para notificar al usuario.</p>
     *
     * <p>El método está anotado con {@link LogAction} para registrar el evento en el sistema de auditoría.</p>
     *
     * @param idDentist el ID del dentista que crea el bloqueo.
     * @param dentistCalendarLockCreateRequestDTO los datos del evento de bloqueo, incluyendo fechas, horas, tipo, recurrencia y observación.
     * @return un objeto {@link Response} con los datos del bloqueo creado y un indicador de conflicto.
     *
     * @throws NotFoundException si el dentista o el tipo de bloqueo no existen.
     * @throws ConflictException si el rango de fechas coincide con un feriado.
     * @throws DataBaseException si ocurre un error al acceder a la base de datos.
     */
    @Override
    @Transactional
    @LogAction(
            value = "dentistCalendarLockService.logAction.create",
            args  = {"#idDentist", "#result.data.startDate", "#result.data.endDate","#result.data.recurrence","#result.data.conflict"},
            type  = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<DentistCalendarLockResponseDTO> create(Long idDentist, DentistCalendarLockCreateRequestDTO dentistCalendarLockCreateRequestDTO) {
        try{

            //Validar Dentista
            Dentist dentist = dentistService.getById(idDentist)
                    .orElseThrow(() -> new NotFoundException("exception.dentistNotFound.user", null, "exception.dentistNotFound.user", new Object[]{idDentist, "DentistCalendarLockService", "create"}, LogLevel.ERROR));

            //Validar Evento
            CalendarLockType calendarLockType = calendarLockTypeService.getByIdInternal(dentistCalendarLockCreateRequestDTO.idLockType());

            //Validar Recurrencia
            CalendarLockRecurrenceName recurrence= CalendarLockRecurrenceName.fromString(dentistCalendarLockCreateRequestDTO.recurrence());

            //Validar Feriado.
            boolean valid = holidayService.validateExistsHoliday(dentistCalendarLockCreateRequestDTO.startDate(), dentistCalendarLockCreateRequestDTO.endDate());
            if(valid){
                throw new ConflictException(
                        "exception.validateExistsHoliday.user", null, "exception.validateExistsHoliday.log", new Object[]{"DentistCalendarLockService", "create"}, LogLevel.WARN
                );
            }

            //Validar Jornada laboral.
            validateDentistAvailability(idDentist,dentistCalendarLockCreateRequestDTO.startDate(), dentistCalendarLockCreateRequestDTO.endDate(), recurrence);


            //Validar con turnos.
            boolean conflict = validateAppointment(idDentist,dentistCalendarLockCreateRequestDTO.startDate(),dentistCalendarLockCreateRequestDTO.startTime(), dentistCalendarLockCreateRequestDTO.endDate(), dentistCalendarLockCreateRequestDTO.endTime());

            String messageConflict = null;
            if(conflict){
                messageConflict = messageSource.getMessage("exception.dentistLockCalendar.validateAppointment.user",null, LocaleContextHolder.getLocale());
            }


            //Crea el bloqueo.
            DentistCalendarLock dentistCalendarLock = new DentistCalendarLock();
            dentistCalendarLock.setDentist(dentist);
            dentistCalendarLock.setStartDate(dentistCalendarLockCreateRequestDTO.startDate());
            dentistCalendarLock.setEndDate(dentistCalendarLockCreateRequestDTO.endDate());
            dentistCalendarLock.setStartTime(dentistCalendarLockCreateRequestDTO.startTime());
            dentistCalendarLock.setEndTime(dentistCalendarLockCreateRequestDTO.endTime());
            dentistCalendarLock.setType(calendarLockType);
            dentistCalendarLock.setRecurrence(recurrence);
            dentistCalendarLock.setObservation( dentistCalendarLockCreateRequestDTO.observation());
            dentistCalendarLock.setCreatedAt(LocalDateTime.now());
            dentistCalendarLock.setCreatedBy(  authenticatedUserService.getAuthenticatedUser());
            dentistCalendarLock.setEnabled(true);

            DentistCalendarLock dentistCalendarLockSaved = dentistLockCalendarRepository.save(dentistCalendarLock);


            //Mapea la respuesta al DTO.
            DentistCalendarLockResponseDTO dentistCalendarLockResponseDTO = new DentistCalendarLockResponseDTO(
                    dentistCalendarLockSaved.getId(),
                    dentistCalendarLockSaved.getDentist().getId(),
                    dentistCalendarLockSaved.getType().getName(),
                    dentistCalendarLockSaved.getRecurrence().getLabel(),
                    dentistCalendarLockSaved.getStartDate(),
                    dentistCalendarLockSaved.getEndDate(),
                    dentistCalendarLockSaved.getStartTime(),dentistCalendarLockSaved.getEndTime(),
                    dentistCalendarLockSaved.getObservation(),
                    conflict
            );

            String messageUser = messageSource.getMessage("dentistCalendarLockService.create.ok.user", null, LocaleContextHolder.getLocale());

            String finalMessage = messageUser + (messageConflict != null ? " - " + messageConflict : "");

            return new Response<>(true, finalMessage, dentistCalendarLockResponseDTO);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistCalendarLockService", idDentist, null, "create");
        }
    }


    /**
     * Valída que un bloqueo de calendario no coincida con turnos futuros.
     * @param idDentist
     */
    private boolean validateAppointment(Long idDentist, LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime ) {

        LocalDateTime blockStart =  LocalDateTime.of(startDate, startTime);
        LocalDateTime blockEnd   =  LocalDateTime.of(endDate, endTime);

        List<AppointmentConflict>appointmentConflicts =new ArrayList<>();


        List<Appointment> appointments = appointmentService.getAppointmentsReservedByDentistInternal(idDentist);

        for(Appointment appointment: appointments){
            if(!appointment.getDate().isBefore(blockStart) && !appointment.getDate().isAfter(blockEnd)) {
                AppointmentConflict appointmentConflict = new AppointmentConflict(
                        null,
                        appointment,
                        AppointmentConflictReason.DENTIST_UNAVAILABLE,
                        false,
                        LocalDateTime.now(),
                        authenticatedUserService.getAuthenticatedUser(),
                        true
                );

                appointmentConflicts.add(appointmentConflict);


            }
        }

        appointmentConflictService.update(appointmentConflicts);

        return !appointmentConflicts.isEmpty();
    }


    /**
     * Valída que un bloqueo de calendario coincida con la jornada laboral del dentista.
     *
     * <p>Dependiendo del tipo de recurrencia del bloqueo:</p>
     * <ul>
     *     <li><b>DAILY:</b> El bloqueo debe cubrir <b>todos</b> los días laborales del dentista dentro del rango de fechas.</li>
     *     <li><b>WEEKLY / MONTHLY / YEARLY / NONE:</b> El bloqueo debe coincidir con <b>al menos un</b> día laboral del dentista.</li>
     * </ul>
     *
     * <p>Este método obtiene los días laborales del dentista y genera las fechas efectivas
     * del bloqueo según la recurrencia. Luego convierte esas fechas en días de la semana
     * y valida la coincidencia según las reglas anteriores.</p>
     *
     * @param idDentist ID del dentista cuyo calendario se valida. No puede ser {@code null}.
     * @param startDate Fecha de inicio del bloqueo.
     * @param endDate Fecha de fin del bloqueo.
     * @param recurrence Tipo de recurrencia del bloqueo.
     *
     * @throws BadRequestException Si:
     * <ul>
     *     <li>Recurrencia DAILY y el bloqueo no cubre todos los días laborales.</li>
     *     <li>Recurrencia diferente de DAILY y el bloqueo no coincide con al menos un día laboral.</li>
     * </ul>
     */
    private void validateDentistAvailability(Long idDentist, LocalDate startDate, LocalDate endDate, CalendarLockRecurrenceName recurrence) {

        //Obtiene el nombre de los días de la jornada laboral del dentista.
        List<DayName> dayNameList = dentistAvailabilityService.getByIdInternal(idDentist);

        //Genera fechas de bloqueo.
        List<LocalDate> lockDate = this.generateEffectiveDates(startDate,endDate,recurrence);

        //Convertimos las fechas generadas de bloqueo en días para comparar con dayNameList
        List<DayName> lockDays = lockDate.stream()
                .map(date -> DayName.fromDayOfWeek(date.getDayOfWeek()))
                .toList();



        boolean isValid;

        // Debe cubrir todos los días laborales del dentista
        if (recurrence == CalendarLockRecurrenceName.DAILY) {
            isValid = dayNameList.stream()
                    .allMatch(day -> lockDays.contains(day));
            if (!isValid) {
                throw new BadRequestException("exception.dentistLockCalendar.validateDentistAvailability.daily.user",null,"exception.dentistLockCalendar.validateDentistAvailability.daily.log",new Object[]{idDentist,"DentistCalendarLockService","validateDentistAvailability"},LogLevel.ERROR);
            }

        // Debe coincidir con al menos un día laboral
        } else {
            isValid = lockDays.stream().anyMatch(dayNameList::contains);
            if (!isValid) {
                throw new BadRequestException("exception.dentistLockCalendar.validateDentistAvailability.notDaily.user",null,"exception.dentistLockCalendar.validateDentistAvailability.notDaily.log",new Object[]{idDentist,"DentistCalendarLockService","validateDentistAvailability"},LogLevel.ERROR);
            }
        }

    }

    /**
     * Genera una lista de fechas efectivas para un bloqueo de calendario según la recurrencia especificada.
     *
     * <p>Dependiendo del tipo de recurrencia, el método calcula todas las fechas
     * que corresponden al período definido entre {@code startDate} y {@code endDate}:</p>
     *
     * <ul>
     *     <li><b>NONE:</b> Solo la fecha de inicio.</li>
     *     <li><b>DAILY:</b> Todos los días entre startDate y endDate (inclusive).</li>
     *     <li><b>WEEKLY:</b> Todos los días de la semana que coinciden con el día de startDate.</li>
     *     <li><b>MONTHLY:</b> Todos los días del mes que coinciden con el día del mes de startDate.</li>
     *     <li><b>YEARLY:</b> Todos los días del año que coinciden con la fecha de startDate.</li>
     * </ul>
     *
     * <p>Este método solo genera las fechas según la recurrencia y no valida
     * si coinciden con los días laborales del dentista.</p>
     *
     * @param startDate Fecha de inicio del bloqueo. No puede ser {@code null}.
     * @param endDate Fecha de fin del bloqueo. No puede ser {@code null} para recurrencias que requieren rango.
     * @param recurrence Tipo de recurrencia del bloqueo.
     * @return Lista de {@link LocalDate} con todas las fechas efectivas generadas.
     */
    private List<LocalDate> generateEffectiveDates(LocalDate startDate,LocalDate endDate, CalendarLockRecurrenceName recurrence) {

        List<LocalDate> dates = new ArrayList<>();

        switch (recurrence) {
            case NONE:
                dates.add(startDate);
                break;
            case DAILY:
                LocalDate d = startDate;
                while (!d.isAfter(endDate)) {
                    dates.add(d);
                    d = d.plusDays(1);
                }
                break;
            case WEEKLY:
                DayOfWeek targetDay = startDate.getDayOfWeek();
                d = startDate;
                while (!d.isAfter(endDate)) {
                    if (d.getDayOfWeek() == targetDay) dates.add(d);
                    d = d.plusDays(1);
                }
                break;
            case MONTHLY:
                int dayOfMonth = startDate.getDayOfMonth();
                d = startDate;
                while (!d.isAfter(endDate)) {
                    if (d.getDayOfMonth() == dayOfMonth) dates.add(d);
                    d = d.plusDays(1);
                }
                break;
            case YEARLY:
                int dayOfYear = startDate.getDayOfYear();
                d = startDate;
                while (!d.isAfter(endDate)) {
                    if (d.getDayOfYear() == dayOfYear) dates.add(d);
                    d = d.plusDays(1);
                }
                break;
        }

        return dates;
    }

}
