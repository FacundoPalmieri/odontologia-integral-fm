package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.Holiday;
import com.odontologiaintegralfm.feature.appointment.catalogs.service.HolidayService;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistAvailability;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistHoliday;
import com.odontologiaintegralfm.feature.appointment.core.repository.IDentistHolidayRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IDentistAvailabilityService;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IDentistHolidayService;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.interfaces.IDentistService;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.feature.user.service.UserService;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.context.MessageSource;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.dto.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Slf4j
@Service
public class DentistHolidayService implements IDentistHolidayService {

    @Autowired
    private IDentistHolidayRepository dentistHolidayRepository;

    @Autowired
    private IDentistService dentistService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private HolidayService holidayService;

    @Autowired
    private UserService userService;

    @Autowired
    private IDentistAvailabilityService dentistAvailabilityService;

    /**
     * Crea una relación entre un dentista y un feriado específico.
     *
     * <p>Este método realiza las siguientes acciones:</p>
     * <ol>
     *     <li>Valida que el usuario exista.</li>
     *     <li>Obtiene el dentista asociado al usuario.</li>
     *     <li>Obtiene el feriado por su ID.</li>
     *     <li>Valida que no exista una relación previa entre el dentista y el feriado.</li>
     *     <li>Verifica que el feriado sea posterior a la fecha actual.</li>
     *     <li>Valida que la hora de inicio sea anterior a la hora de fin.</li>
     *     <li>Valida que la duración del bloqueo cubra al menos la duración mínima de un turno del dentista.</li>
     *     <li>Persiste la nueva relación {@link DentistHoliday} en la base de datos.</li>
     *     <li>Construye y retorna un {@link DentistHolidayResponseDTO} con los datos de la relación creada.</li>
     * </ol>
     *
     * @param id ID del usuario asociado al dentista. No puede ser {@code null}.
     * @param dentistHolidayRequestCreateDTO DTO que contiene los datos del feriado y las horas de inicio y fin.
     * @return {@link Response} con un {@link DentistHolidayResponseDTO} que contiene:
     * <ul>
     *     <li>ID de la relación creada.</li>
     *     <li>ID del dentista.</li>
     *     <li>ID del feriado y detalles como nombre y fecha.</li>
     *     <li>Horas de inicio y fin del feriado para ese dentista.</li>
     *     <li>Indicador de habilitación.</li>
     * </ul>
     */
    @Override
    @Transactional
    public Response<DentistHolidayResponseDTO> create(Long id, DentistHolidayRequestCreateDTO dentistHolidayRequestCreateDTO) {
        try {
            //Obtiene el usuario.
            UserSec userSec = userService.getByIdInternal(id);

            //Obtiene el dentista
            Dentist dentists = dentistService.getById(userSec.getPerson().getId())
                    .orElseThrow(()-> new NotFoundException("exception.dentistNotFound.user", null,"exception.dentistNotFound.log",new Object[]{userSec.getPerson().getId(),"DentistHolidayService","create"},LogLevel.ERROR));


            //Obtiene el feriado.
            Holiday holiday = holidayService.getByIdInternal(dentistHolidayRequestCreateDTO.idHoliday());

            //Valida que no exista una relación previa.
            Optional <DentistHoliday> dentistHolidays = dentistHolidayRepository.findByDentistIdAndHolidayId(id,holiday.getId());
            if(dentistHolidays.isPresent()) {
                throw new ConflictException("exception.dentistHolidayService.create.user",null,"exception.dentistHolidayService.create.log",new Object[]{id,holiday.getId(),"Dentist Holiday Service","create"},LogLevel.ERROR);
            }

            //Valída que el feriado no sea anterior a la fecha actual.
            if(!holiday.getDate().isAfter(LocalDate.now())){
                throw new ConflictException("exception.dentistHolidayService.create.validateHolidayBeforeNow.user",null,"exception.dentistHolidayService.create.validateHolidayBeforeNow.log", new Object[]{id,holiday.getId(),holiday.getName(),holiday.getDate(),"Dentist Holiday Service","create"},LogLevel.ERROR);
            }

            //Valída que la hora de inicio sea anterior a la de fin.
            if(!dentistHolidayRequestCreateDTO.startTime().isBefore(dentistHolidayRequestCreateDTO.endTime())){
                throw new ConflictException("exception.dentistHolidayService.create.validateStartTimeBeforeEndTime.user",null,"exception.dentistHolidayService.create.validateStartTimeBeforeEndTime.log", new Object[]{id,holiday.getId(),holiday.getName(),holiday.getDate(),dentistHolidayRequestCreateDTO.startTime(),dentistHolidayRequestCreateDTO.endTime(),"Dentist Holiday Service","create"},LogLevel.ERROR);
            }


            //Valída que la fecha del feriado coincida con alguna jornada laboral.(Debe si o si coincidir, el feriado solo se opta por trabajarlo o no, si está dentro de una jornada)
            DentistAvailability dentistAvailability = dentistAvailabilityService.getDentistAvailabilityByDate(dentists.getId(), holiday.getDate());


            //Valída que la hora de inicio y fin cubra al menos la parametrización de la duración de un turno.
            if(! dentistAvailabilityService.validateDurationLessThanAppointmentDuration(dentistHolidayRequestCreateDTO.startTime(),dentistHolidayRequestCreateDTO.endTime(), dentistHolidayRequestCreateDTO.appointmentDuration())){
                throw new ConflictException("exception.dentistHolidayService.create.validateDurationLessThanAppointmentDuration.user",null,"exception.dentistHolidayService.create.validateDurationLessThanAppointmentDuration.log", new Object[]{dentistHolidayRequestCreateDTO.startTime(),dentistHolidayRequestCreateDTO.endTime(), dentistHolidayRequestCreateDTO.appointmentDuration(),"DentistAvailabilityService","entityFromDto"},LogLevel.ERROR);
            }


            //Construye objeto a persistir.
            DentistHoliday dentistHoliday = DentistHoliday.build(dentists,holiday,dentistHolidayRequestCreateDTO);

            //Persiste.
            DentistHoliday dentistHolidaySaved = dentistHolidayRepository.save(dentistHoliday);

            return new Response<>(
                    true,
                    messageSource.getMessage("dentistHolidayService.create.user.ok",null,LocaleContextHolder.getLocale()),
                    DentistHolidayResponseDTO.build(dentistHolidaySaved)
            );

        }catch (CannotCreateTransactionException | DataAccessException e) {
            throw new DataBaseException(e, "DentistHolidayService", id, null, "create");
        }
    }





    /**
     * Método para la actualización de la relación de un dentista con feriados.
     */
    @Override
    public Response<DentistHolidayResponseDTO> update(DentistHolidayRequestUpdateDTO dentistHolidayRequestUpdateDTO) {

        try {
            //Recupera el objeto anterior.
            DentistHoliday dentistHoliday = dentistHolidayRepository.findById(dentistHolidayRequestUpdateDTO.idDentistHoliday())
                    .orElseThrow(()-> new BadRequestException("exception.dentistHolidayService.update.user",null,"exception.dentistHolidayService.update.log",new Object[]{dentistHolidayRequestUpdateDTO.idDentistHoliday(),"Dentist CalendarHoliday Service","update"},LogLevel.ERROR));



            //Actualiza datos.
            dentistHoliday.setStartTime(dentistHolidayRequestUpdateDTO.startTime());
            dentistHoliday.setEndTime(dentistHolidayRequestUpdateDTO.endTime());
            dentistHoliday.setEnabled(dentistHolidayRequestUpdateDTO.enabled());
            dentistHoliday.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
            dentistHoliday.setUpdatedAt(LocalDateTime.now());

            DentistHoliday dentistHolidaySaved = dentistHolidayRepository.save(dentistHoliday);


            return new Response<>(
                    true,
                    messageSource.getMessage("dentistHolidayService.update.user.ok", null, LocaleContextHolder.getLocale()),
                    DentistHolidayResponseDTO.build(dentistHolidaySaved));


        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistHolidayService", dentistHolidayRequestUpdateDTO.idDentistHoliday(), null, "update");
        }
    }

    /**
     * Obtiene las relaciones entre dentista y feriados.
     *
     * @param idDentist : id dentista
     * @param year      : año a consultar
     * @return : Lista
     */
    @Override
    public List<DentistHoliday> getAll(Long idDentist, int year) {
        try{
            return dentistHolidayRepository.findAllByDentistId(idDentist, year);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistHolidayService", idDentist, null, "get");
        }

    }

    /**
     * Obtiene la relación entre dentista y feriado.
     *
     * @param idDentist : id dentista
     * @param idHoliday : id feriado.
     */
    @Override
    public Optional<DentistHoliday> getByDentistIdAndHolidayId(Long idDentist, Long idHoliday) {
        try{
            return dentistHolidayRepository.findByDentistIdAndHolidayId(idDentist, idHoliday);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistHolidayService", idDentist, null, "getByDentistIdAndHolidayId");
        }
    }




    /**
     * Valída si existe relación entre feriado y dentista.
     * Si existe, no realiza acción.
     * Si no existe, arroja exceptión.
     * @param idDentist : Id dentista
     * @param date : Fecha
     */
    @Override
    public void validateDentistIdAndDate(Long idDentist, LocalDate date) {

        //Se valida que el turno no sea un feriado.
        Optional<Holiday> holiday = holidayService.getByDate(date);

        // Si es feriado, se valida que el dentista lo trabaje.
        if (holiday.isPresent()) {
            getByDentistIdAndHolidayId(idDentist,holiday.get().getId())
                    .orElseThrow(() -> new ConflictException("exception.dentistHolidayService.validateHoliday.user", null, "exception.dentistHolidayService.validateHoliday.log",new Object[]{idDentist ,holiday.get().getId(), "dentistHolidayService", "validateHoliday"}, LogLevel.ERROR));
        }
    }


}
