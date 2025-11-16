package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.CalendarLockType;
import com.odontologiaintegralfm.feature.appointment.catalogs.service.ICalendarLockTypeService;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.enums.OriginConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistAvailability;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLock;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLockDetail;
import com.odontologiaintegralfm.feature.appointment.core.repository.IDentistCalendarLockRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IConflictManagerService;
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
import java.util.Arrays;
import java.util.List;


@Service
public class DentistCalendarLockService implements IDentistLockCalendarService {

    @Autowired
    private IDentistService dentistService;

    @Autowired
    private ICalendarLockTypeService calendarLockTypeService;

    @Autowired
    private DentistAvailabilityService dentistAvailabilityService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private AppointmentConflictService appointmentConflictService;

    @Autowired
    private IDentistCalendarLockRepository dentistLockCalendarRepository;


    @Autowired
    private IConflictManagerService conflictManagerService;

    @Qualifier("messageSource")
    @Autowired
    private MessageSource messageSource;

    /**
     * Crea un nuevo bloqueo en el calendario de un dentista y valida posibles conflictos con turnos existentes.
     * <p>
     * Este método realiza los siguientes pasos:
     * <ol>
     *     <li>Obtiene el usuario y el dentista correspondiente al {@code idUser}.</li>
     *     <li>Valida que el tipo de bloqueo y la recurrencia sean correctos.</li>
     *     <li>Valida que la jornada laboral del dentista cubra el período del bloqueo.</li>
     *     <li>Verifica que la fecha de inicio no sea anterior a la fecha actual y que la fecha de fin no sea anterior a la de inicio.</li>
     *     <li>Crea y persiste el bloqueo en la base de datos.</li>
     *     <li>Asocia el ID del bloqueo y el origen de conflicto en el DTO.</li>
     *     <li>Verifica si existen turnos conflictivos y genera los {@link AppointmentConflictResponseDTO} correspondientes.</li>
     *     <li>Construye y retorna un {@link DentistCalendarLockResponseDTO} con la información del bloqueo y conflictos detectados.</li>
     * </ol>
     *
     * @param idPerson ID del Dentista
     * @param dentistCalendarLockRequestCreateDTO Datos del bloqueo a crear (fechas, horarios, días, observaciones, tipo y recurrencia).
     * @return {@link Response} que contiene el DTO del bloqueo creado y, si corresponde, los conflictos detectados.
     * @throws NotFoundException Si el dentista no se encuentra en la base de datos.
     * @throws ConflictException Si las fechas del bloqueo son inválidas (inicio antes de hoy o fin antes del inicio).
     * @throws DataBaseException Si ocurre un error al persistir los datos en la base.
     */

    @Override
    @Transactional
    @LogAction(
            value = "dentistCalendarLockService.logAction.create",
            args  = {"#idDentist", "#result.data.startDate", "#result.data.endDate","#result.data.recurrence","#result.data.conflict"},
            type  = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<DentistCalendarLockResponseDTO> create(Long idPerson, DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO) {
        try{

            //Obtiene el dentista
            Dentist dentists = dentistService.getById(idPerson)
                    .orElseThrow(()-> new NotFoundException("exception.dentistNotFound.user", null,"exception.dentistNotFound.log",new Object[]{idPerson,"DentistHolidayService","create"},LogLevel.ERROR));


            //Validar Evento
            CalendarLockType calendarLockType = calendarLockTypeService.getByIdInternal(dentistCalendarLockRequestCreateDTO.getIdLockType());


            //Validar Jornada laboral.
            validateDentistAvailability(idPerson,dentistCalendarLockRequestCreateDTO.getStartDate(), dentistCalendarLockRequestCreateDTO.getEndDate(), dentistCalendarLockRequestCreateDTO.getStartTime(), dentistCalendarLockRequestCreateDTO.getEndTime(),dentistCalendarLockRequestCreateDTO.getDays(), dentistCalendarLockRequestCreateDTO.getRecurrence());



            //Valída que el inicio no sea anterior al día actual.
            if(dentistCalendarLockRequestCreateDTO.getStartDate().isBefore(LocalDate.now())){
                throw new ConflictException("exception.dentistLockCalendarService.validateStarDateBeforeNow.user",null,"exception.dentistLockCalendarService.validateStarDateBeforeNow.log",new Object[]{dentists.getId(),dentistCalendarLockRequestCreateDTO.getStartDate(),"Dentist Calendar Lock Service","create" },LogLevel.ERROR);
            }

            //Valíd que la fecha de fin no sea anterior a la fecha de inicio.
            if(dentistCalendarLockRequestCreateDTO.getEndDate().isBefore(dentistCalendarLockRequestCreateDTO.getStartDate())){
                throw new ConflictException("exception.dentistLockCalendarService.validateEndDateBeforeStartDate.user",null,"exception.dentistLockCalendarService.validateEndDateBeforeStartDate.log",new Object[]{dentists.getId(),dentistCalendarLockRequestCreateDTO.getStartDate(),dentistCalendarLockRequestCreateDTO.getEndDate(),"Dentist Calendar Lock Service","create"},LogLevel.ERROR);
            }

            //Valida que si son vacaciones, comiencen un día lunes.



            //Crea el bloqueo.
            DentistCalendarLock dentistCalendarLock = new DentistCalendarLock();
            dentistCalendarLock.setDentist(dentists);
            dentistCalendarLock.setStartDate(dentistCalendarLockRequestCreateDTO.getStartDate());
            dentistCalendarLock.setEndDate(dentistCalendarLockRequestCreateDTO.getEndDate());
            dentistCalendarLock.setStartTime(dentistCalendarLockRequestCreateDTO.getStartTime());
            dentistCalendarLock.setEndTime(dentistCalendarLockRequestCreateDTO.getEndTime());
            dentistCalendarLock.setType(calendarLockType);
            dentistCalendarLock.setRecurrence(dentistCalendarLockRequestCreateDTO.getRecurrence());

            //Creo el detalle del bloqueo, si corresponde
            if(dentistCalendarLockRequestCreateDTO.getDays() != null){
                for (DayName day : dentistCalendarLockRequestCreateDTO.getDays()){
                    DentistCalendarLockDetail detail = new DentistCalendarLockDetail();
                    detail.setDayName(day);
                    detail.setLock(dentistCalendarLock);

                    dentistCalendarLock.getDays().add(detail);
                }
            }


            dentistCalendarLock.setObservation( dentistCalendarLockRequestCreateDTO.getObservation());
            dentistCalendarLock.setCreatedAt(LocalDateTime.now());
            dentistCalendarLock.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
            dentistCalendarLock.setEnabled(true);



            DentistCalendarLock dentistCalendarLockSaved = dentistLockCalendarRepository.saveAndFlush(dentistCalendarLock);

            //Seteo id y origen de posible conflicto en el DTO.
            dentistCalendarLockRequestCreateDTO.setIdOriginConflict(dentistCalendarLockSaved.getId());
            dentistCalendarLockRequestCreateDTO.setOriginConflict(OriginConflict.DENTIST_CALENDAR_LOCK);

            //Validar si existen turnos conflictivos.
            List<AppointmentConflictResponseDTO> appointmentConflicts = conflictManagerService.verifyConflictsByDentistCalendarLock(dentistCalendarLockRequestCreateDTO,dentists);




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
                    dentistCalendarLockSaved.getObservationUpdate(),
                    appointmentConflicts
            );


            return new Response<>(
                    true,
                    (appointmentConflicts.isEmpty())
                            ? messageSource.getMessage("dentistCalendarLockService.create.ok.user",null, LocaleContextHolder.getLocale())
                            : messageSource.getMessage("dentistLockCalendarService.create.okWithConflict.user", null, LocaleContextHolder.getLocale()),
                    dentistCalendarLockResponseDTO
            );

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistCalendarLockService", idPerson, null, "create");
        }
    }








    /**
     * Actualiza un bloqueo existente en el calendario de un dentista.
     * <p>
     * El método realiza las siguientes operaciones:
     * <ol>
     *     <li>Recupera el bloqueo a actualizar a partir del ID proporcionado.</li>
     *     <li>Verifica que el bloqueo aún esté vigente; si ya finalizó, lanza una excepción de conflicto.</li>
     *     <li>Resuelve los turnos en conflicto posteriores a la finalización anticipada del bloqueo, si corresponde.</li>
     *     <li>Actualiza la información del bloqueo (por ejemplo, la observación de actualización y la fecha de fin).</li>
     *     <li>Mapea los datos actualizados a un {@link DentistCalendarLockResponseDTO} para la respuesta.</li>
     * </ol>
     *
     * @param dentistCalendarLockRequestUpdateDTO DTO que contiene los datos de actualización del bloqueo, incluyendo ID del bloqueo y observación de actualización.
     * @return {@link Response} que contiene el DTO con la información actualizada del bloqueo.
     * @throws BadRequestException Si el bloqueo con el ID proporcionado no se encuentra.
     * @throws ConflictException Si el bloqueo ya finalizó y no se puede actualizar.
     */

    @Override
    @LogAction(
            value = "dentistCalendarLockService.logAction.update",
            args  = {"#dentistCalendarLockRequestUpdateDTO.idDentistCalendarLock","#result.data.endDate","#result.data.ObservationUpdate"},
            type  = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<DentistCalendarLockResponseDTO> update(DentistCalendarLockRequestUpdateDTO dentistCalendarLockRequestUpdateDTO) {

        //Recuperamos el Evento.
        DentistCalendarLock dentistCalendarLock = dentistLockCalendarRepository.findById(dentistCalendarLockRequestUpdateDTO.idDentistCalendarLock())
                .orElseThrow(()-> new BadRequestException("exception.dentistLockCalendarService.notFound.user", null,"exception.dentistLockCalendarService.notFound.log", new Object[]{dentistCalendarLockRequestUpdateDTO.idDentistCalendarLock(),"Dentist LockCalendar Service", "Update"}, LogLevel.ERROR));

        //Verificamos que esté vigente.
        if (dentistCalendarLock.getEndDate().isBefore(LocalDate.now()) ||
                (dentistCalendarLock.getEndDate().isEqual(LocalDate.now()) && !dentistCalendarLock.getEndTime().isAfter(LocalTime.now()))) {
            throw new ConflictException("exception.dentistLockCalendarService.validateLockBeforeNow.user", null, "exception.dentistLockCalendarService.validateLockBeforeNow.log", new Object[]{dentistCalendarLockRequestUpdateDTO.idDentistCalendarLock(), dentistCalendarLock.getEndDate(), dentistCalendarLock.getEndTime(),"Dentist CalendarLock Service", "Update"}, LogLevel.ERROR);
        }


        //Resuelve turnos en conflicto posterior a la finalización anticipada del bloqueo.
        validateAppointmentConflict(dentistCalendarLock);


        //Actualizamos la fecha de finalización del evento.
        DentistCalendarLock dentistCalendarLockSaved = updateCalendarLock(dentistCalendarLock,dentistCalendarLockRequestUpdateDTO.observationUpdate());

        //Mapeamos el DTO para respuesta.
        DentistCalendarLockResponseDTO dentistCalendarLockResponseDTO = new DentistCalendarLockResponseDTO(
                dentistCalendarLockSaved.getId(),
                dentistCalendarLockSaved.getDentist().getId(),
                dentistCalendarLockSaved.getType().getName(),
                dentistCalendarLockSaved.getRecurrence().getLabel(),
                dentistCalendarLockSaved.getStartDate(),
                dentistCalendarLockSaved.getEndDate(),
                dentistCalendarLockSaved.getStartTime(),
                dentistCalendarLockSaved.getEndTime(),
                dentistCalendarLockSaved.getObservation(),
                dentistCalendarLockSaved.getObservationUpdate(),
                null
        );

        return new Response<>(
                true,
                messageSource.getMessage("dentistCalendarLockService.update.ok.user",null, LocaleContextHolder.getLocale()),
                dentistCalendarLockResponseDTO
        );

    }


    /**
     * Actualiza un bloqueo existente estableciendo su finalización al momento actual y registrando la observación de actualización.
     * <p>
     * Se actualizan los campos de auditoría ({@code updatedAt}, {@code updatedBy}) y se persiste el cambio en la base de datos.
     *
     * @param dentistCalendarLock Bloqueo de calendario a actualizar.
     * @param observationUpdate Observación que describe la actualización realizada.
     * @return El {@link DentistCalendarLock} actualizado y persistido.
     */

    private DentistCalendarLock updateCalendarLock(DentistCalendarLock dentistCalendarLock,String observationUpdate) {
        dentistCalendarLock.setEndDate(LocalDate.now());
        dentistCalendarLock.setEndTime(LocalTime.now());
        dentistCalendarLock.setUpdatedAt(LocalDateTime.now());
        dentistCalendarLock.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
        dentistCalendarLock.setObservationUpdate(observationUpdate);

        return dentistLockCalendarRepository.save(dentistCalendarLock);
    }






    /**
     * Valida y resuelve los turnos en conflicto cuando un bloqueo de calendario se finaliza anticipadamente.
     * <p>
     * El método obtiene todos los conflictos asociados al bloqueo y, si existen, los marca como resueltos
     * mediante el servicio de gestión de conflictos.
     *
     * @param dentistCalendarLock Bloqueo de calendario que se está finalizando anticipadamente.
     */

    private void validateAppointmentConflict(DentistCalendarLock dentistCalendarLock) {

        List<AppointmentConflict> appointmentConflicts = appointmentConflictService.getAllByDentistIdAndCalendarLockConflictId(dentistCalendarLock.getDentist().getId(), dentistCalendarLock.getId());

        if(!appointmentConflicts.isEmpty()){
            conflictManagerService.updateResolvedConflicts(dentistCalendarLock.getDentist().getId(),appointmentConflicts);
        }

    }





    /**
     * Valida que un bloqueo de calendario propuesto coincida con la jornada laboral del dentista.
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
     * @param idDentist ID del dentista cuyo calendario se valida.
     * @param startDateBlock Semana de inicio del bloqueo.
     * @param endDateBlock Semana de fin del bloqueo.
     * @param starTimeBlock Hora de inicio del bloqueo.
     * @param endTimeBlock Hora de fin del bloqueo.
     * @param daysBlock Lista de días del bloqueo (DayOfWeek).
     * @param recurrenceBlock Tipo de recurrencia del bloqueo.
     *
     * @throws BadRequestException Si el bloqueo no cumple con la cobertura requerida según la recurrencia y jornada del dentista.
     */

    private void validateDentistAvailability(Long idDentist, LocalDate startDateBlock, LocalDate endDateBlock, LocalTime starTimeBlock, LocalTime endTimeBlock, List<DayName> daysBlock, CalendarLockRecurrenceName recurrenceBlock) {
        List<DentistAvailability> availabilities = dentistAvailabilityService.getByIdInternal(idDentist);

        // Si no especifican días, usar toda la semana
        List<DayOfWeek> daysToEvaluate =
                (daysBlock == null || daysBlock.isEmpty())
                        ? Arrays.asList(DayOfWeek.values())
                        : convertToDayOfWeek(daysBlock);

        for (DayOfWeek day : daysToEvaluate) {
            LocalDate effectiveStartDateBlock = conflictManagerService.findFirstMatchingDate(startDateBlock, day);
            LocalDate effectiveEndDateBlock = conflictManagerService.findLastMatchingDate(endDateBlock, day);
            boolean valid = conflictManagerService.hasBlockMatchWithAvailability(availabilities, effectiveStartDateBlock, effectiveEndDateBlock, starTimeBlock, endTimeBlock, recurrenceBlock);
            if (!valid) {
                throw new BadRequestException("exception.dentistLockCalendarService.validateDentistAvailability.user", null, "exception.dentistLockCalendarService.validateDentistAvailability.log", new Object[]{idDentist, "DentistCalendarLockService", "validateDentistAvailability"}, LogLevel.ERROR);
            }
        }

    }



    private List<DayOfWeek> convertToDayOfWeek(List<DayName> days){
        return days.stream()
                .map(DayName::toDayOfWeek)
                .toList();
    }
}
