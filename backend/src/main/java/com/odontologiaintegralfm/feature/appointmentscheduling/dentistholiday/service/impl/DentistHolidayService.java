package com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.service.IDentistHolidayService;
import com.odontologiaintegralfm.feature.appointmentscheduling.holiday.model.Holiday;
import com.odontologiaintegralfm.feature.appointmentscheduling.holiday.service.HolidayService;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.dto.DentistHolidayRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.dto.DentistHolidayRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.dto.DentistHolidayResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.model.DentistHoliday;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.repository.IDentistHolidayRepository;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.util.CalendarUtils;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
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
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private HolidayService holidayService;


    /**
     * Crea un registro de feriado para un dentista específico. Realiza múltiples validaciones antes de persistir:
     * <ul>
     *     <li>No debe existir una relación previa entre dentista y feriado.</li>
     *     <li>El feriado no puede ser anterior a la fecha actual.</li>
     *     <li>La hora de inicio debe ser anterior a la de fin.</li>
     *     <li>La duración entre inicio y fin debe cubrir al menos la duración mínima de un turno.</li>
     * </ul>
     * Además, registra auditoría con usuario creador y fecha de creación.
     *
     * @param dentist el dentista al que se asignará el feriado.
     * @param holiday el feriado que se asignará al dentista.
     * @param dto     DTO con los horarios y duración del turno.
     * @return Response<DentistHolidayResponseDTO> con los datos del feriado asignado al dentista.
     * @throws ConflictException     si ya existe la relación, si la fecha es anterior a hoy,
     *                               si el horario de inicio es posterior al de fin, o si la duración
     *                               no cumple con la duración mínima de un turno.
     * @throws DataBaseException     si ocurre un error al persistir los datos en la base.
     */
    @Transactional
    public Response<DentistHolidayResponseDTO> create(Dentist dentist, Holiday holiday, DentistHolidayRequestCreateDTO dto ) {
        try {

            //Valida que no exista una relación previa.
            Optional <DentistHoliday> dentistHolidays = dentistHolidayRepository.findByDentistIdAndHolidayId(dentist.getId(),holiday.getId());
            if(dentistHolidays.isPresent()) {
                throw new ConflictException("exception.dentistHolidayService.create.user",null,"exception.dentistHolidayService.create.log",new Object[]{dentist.getId(),holiday.getId(),"Dentist Holiday Service","create"},LogLevel.ERROR);
            }

            //Valída que el feriado no sea anterior a la fecha actual.
            if(!holiday.getDate().isAfter(LocalDate.now())){
                throw new ConflictException("exception.dentistHolidayService.create.validateHolidayBeforeNow.user",null,"exception.dentistHolidayService.create.validateHolidayBeforeNow.log", new Object[]{dentist.getId(),holiday.getId(),holiday.getName(),holiday.getDate(),"Dentist Holiday Service","create"},LogLevel.ERROR);
            }

            //Valída que la hora de inicio sea anterior a la de fin.
            if(!dto.startTime().isBefore(dto.endTime())){
                throw new ConflictException("exception.dentistHolidayService.create.validateStartTimeBeforeEndTime.user",null,"exception.dentistHolidayService.create.validateStartTimeBeforeEndTime.log", new Object[]{dentist.getId(),holiday.getId(),holiday.getName(),holiday.getDate(),dto.startTime(),dto.endTime(),"Dentist Holiday Service","create"},LogLevel.ERROR);
            }

            //Valída que la hora de inicio y fin cubra al menos la parametrización de la duración de un turno.
            if(! CalendarUtils.validateDurationLessThanAppointmentDuration(dto.startTime(),dto.endTime(), dto.appointmentDuration())){
                throw new ConflictException("exception.dentistHolidayService.create.validateDurationLessThanAppointmentDuration.user",null,"exception.dentistHolidayService.create.validateDurationLessThanAppointmentDuration.log", new Object[]{dto.startTime(),dto.endTime(), dto.appointmentDuration(),"DentistAvailabilityService","entityFromDto"},LogLevel.ERROR);
            }


            //Construye objeto a persistir.
            DentistHoliday dentistHoliday = DentistHoliday.build(dentist,holiday,dto);

            //Auditoría.
            dentistHoliday.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
            dentistHoliday.setCreatedAt(LocalDateTime.now());
            dentistHoliday.setEnabled(true);

            //Persiste.
            DentistHoliday dentistHolidaySaved = dentistHolidayRepository.save(dentistHoliday);

            return new Response<>(
                    true,
                    messageSource.getMessage("dentistHolidayService.create.user.ok",null,LocaleContextHolder.getLocale()),
                    DentistHolidayResponseDTO.build(dentistHolidaySaved)
            );

        }catch (CannotCreateTransactionException | DataAccessException e) {
            throw new DataBaseException(e, "DentistHolidayService", dentist.getId(), "<--- Dentist ID", "create");
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


    /**
     * Verifica si existe relación entre un Holiday y dentista. Caso afirmativo, deshabilita la relación.
     */
    @Override
    public void VerifyAndDisabled(Long idHoliday, Long idDentist) {
        Optional<DentistHoliday> dentistHoliday = dentistHolidayRepository.findByDentistIdAndHolidayId(idDentist, idHoliday);
        if (dentistHoliday.isPresent()) {
            DentistHoliday dh = dentistHoliday.get();

            dh.setEnabled(false);
            dh.setDisabledAt(LocalDateTime.now());
            dh.setDisabledBy(authenticatedUserService.getAuthenticatedUser());
        }
    }
}
