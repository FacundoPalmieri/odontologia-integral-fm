package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedSystemService;
import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.Holiday;
import com.odontologiaintegralfm.feature.appointment.catalogs.repository.IHolidayRepository;
import com.odontologiaintegralfm.feature.appointment.catalogs.service.HolidayService;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistHoliday;
import com.odontologiaintegralfm.feature.appointment.core.repository.IDentistHolidayRepository;
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
import com.odontologiaintegralfm.shared.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
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

    /**
     * Método que crea relación entre dentista y feriado.
     */
    @Override
    public Response<DentistHolidayResponseDTO> create(Long id, DentistHolidayRequestCreateDTO dentistHolidayRequestCreateDTO) {
        try {
            //Obtiene el usuario.
            UserSec userSec = userService.getByIdInternal(id);

            //Obtiene el dentista
            Dentist dentists = dentistService.getById(userSec.getPerson().getId())
                    .orElseThrow(()-> new NotFoundException("exception.dentistNotFound.user", null,"exception.dentistNotFound.log",new Object[]{id,"DentistHolidayService","create"},LogLevel.ERROR));


            //Obtiene el feriado.
            Holiday holiday = holidayService.getByIdInternal(dentistHolidayRequestCreateDTO.idHoliday());

            //Valida que no exista una relación previa.
            Optional <DentistHoliday> dentistHolidays = dentistHolidayRepository.findByDentistIdAndHolidayId(id,holiday.getId());
            if(dentistHolidays.isPresent()) {
                throw new ConflictException("exception.dentistHolidayService.create.user",null,"exception.dentistHolidayService.create.log",new Object[]{id,holiday.getId(),"Dentist Holiday Service","create"},LogLevel.ERROR);
            }

            //Persiste la relación.
            DentistHoliday dentistHoliday = new DentistHoliday();
            dentistHoliday.setDentist(dentists);
            dentistHoliday.setHoliday(holiday);
            dentistHoliday.setStartTime(dentistHolidayRequestCreateDTO.startTime());
            dentistHoliday.setEndTime(dentistHolidayRequestCreateDTO.endTime());
            dentistHoliday.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
            dentistHoliday.setCreatedAt(LocalDateTime.now());
            dentistHoliday.setEnabled(true);

            //Persiste.
            DentistHoliday dentistHolidaySaved = dentistHolidayRepository.save(dentistHoliday);

            //Convierte al DTO.
            DentistHolidayResponseDTO dentistHolidayResponseDTO = new DentistHolidayResponseDTO(
                    dentistHolidaySaved.getId(),
                    dentistHolidaySaved.getDentist().getId(),
                    dentistHolidaySaved.getHoliday().getId(),
                    dentistHolidaySaved.getHoliday().getDate(),
                    dentistHolidaySaved.getHoliday().getName(),
                    dentistHolidaySaved.getStartTime(),
                    dentistHolidaySaved.getEndTime(),
                    dentistHolidaySaved.isEnabled()
            );

            return new Response<>(
                    true,
                    messageSource.getMessage("dentistHolidayService.create.user.ok",null,LocaleContextHolder.getLocale()),
                    dentistHolidayResponseDTO
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
                    .orElseThrow(()-> new BadRequestException("exception.dentistHolidayService.update.user",null,"exception.dentistHolidayService.update.log",new Object[]{dentistHolidayRequestUpdateDTO.idDentistHoliday(),"Dentist Holiday Service","update"},LogLevel.ERROR));



            //Actualiza datos.
            dentistHoliday.setStartTime(dentistHolidayRequestUpdateDTO.startTime());
            dentistHoliday.setEndTime(dentistHolidayRequestUpdateDTO.endTime());
            dentistHoliday.setEnabled(dentistHolidayRequestUpdateDTO.enabled());
            dentistHoliday.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
            dentistHoliday.setUpdatedAt(LocalDateTime.now());

            DentistHoliday dentistHolidaySaved = dentistHolidayRepository.save(dentistHoliday);

            //Arma respuesta
            DentistHolidayResponseDTO dentistHolidayResponseDTO = new DentistHolidayResponseDTO(
                    dentistHolidaySaved.getId(),
                    dentistHolidaySaved.getDentist().getId(),
                    dentistHolidaySaved.getHoliday().getId(),
                    dentistHoliday.getHoliday().getDate(),
                    dentistHoliday.getHoliday().getName(),
                    dentistHolidaySaved.getStartTime(),
                    dentistHolidaySaved.getEndTime(),
                    dentistHolidaySaved.isEnabled()
            );

            return new Response<>(
                    true,
                    messageSource.getMessage("dentistHolidayService.update.user.ok", null, LocaleContextHolder.getLocale()),
                    dentistHolidayResponseDTO);


        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistHolidayService", dentistHolidayRequestUpdateDTO.idDentistHoliday(), null, "update");
        }
    }









}
