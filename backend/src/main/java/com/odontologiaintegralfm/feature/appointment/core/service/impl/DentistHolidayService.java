package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedSystemService;
import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.Holiday;
import com.odontologiaintegralfm.feature.appointment.catalogs.repository.IHolidayRepository;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayListRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayListResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistHoliday;
import com.odontologiaintegralfm.feature.appointment.core.repository.IDentistHolidayRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IDentistHolidayService;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.interfaces.IDentistService;
import com.odontologiaintegralfm.feature.user.model.UserSec;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private IHolidayRepository holidayRepository;
    @Autowired
    private AuthenticatedSystemService authenticatedSystemService;

    /**
     * Método que crea las relaciones entre dentistas y feriados.
     * El mismo se ejecuta dentro de la tarea programada anual de carga de feriados.
     *
     * @param holidayList
     */
    @Override
    public void create(int year,List<Holiday> holidayList) {
        try {
            //Obtiene la lista de dentista.
            List<Dentist> dentists = dentistService.getAllInternal();

            //Crea el DTO con la relación.
            List<DentistHolidayListRequestDTO> dentistHolidayListRequestDTOS = holidayList.stream().map(holiday -> new DentistHolidayListRequestDTO(
                            holiday.getId(),
                            LocalTime.of(0, 0),
                            LocalTime.of(23, 59)
                    ))
                    .toList();

            DentistHolidayRequestDTO dentistHolidayRequestDTO = new DentistHolidayRequestDTO(
                    year,
                    dentistHolidayListRequestDTOS
            );

            List<DentistHoliday> dentistHolidays = new ArrayList<>();
            dentists.forEach(dentist -> {
             dentistHolidays.addAll(buildHolidayDentist(dentist, holidayList, dentistHolidayRequestDTO, authenticatedSystemService.getAuthenticatedUserSystem()));
            });

            dentistHolidayRepository.saveAll(dentistHolidays);

        } catch (Exception e) {

            throw new ConflictException(null, null, "exception.dentistHolidayService.create.log", null,LogLevel.ERROR);

        }
    }

    /**
     * Método para la creación de la relación de un dentista con feriados.
     */
    @Override
    public Response<DentistHolidayResponseDTO> update(Long idDentist, DentistHolidayRequestDTO dentistHolidayRequestDTO) {

        try {

            //Validar el dentista.
            Dentist dentist = dentistService.getById(idDentist)
                    .orElseThrow(() -> new ConflictException("exception.dentistNotFound.user", null, "exception.dentistNotFound.log", new Object[]{idDentist, "Dentist Holiday Service", "update"}, LogLevel.ERROR));


            //Validar que existan los feriados. Retorna la misma lista de la request pero con la entidad completa.
            List<Holiday> holidays = this.validateHolidaysExist(dentistHolidayRequestDTO.holiday(), dentistHolidayRequestDTO.year());


            //Obtener relaciones previas si existen.
            List<DentistHoliday> dentistHoliday = dentistHolidayRepository.findAllByDentistId(idDentist, dentistHolidayRequestDTO.year());

            //Reemplazar la lista
            dentistHolidayRepository.deleteAll(dentistHoliday);

            //Armar objetos DentistHoliday para persistir
            List<DentistHoliday> dentistHolidays = buildHolidayDentist(dentist, holidays, dentistHolidayRequestDTO,authenticatedUserService.getAuthenticatedUser());

            //Agrega las nuevas relaciones
            dentistHoliday.addAll(dentistHolidays);

            List<DentistHoliday> dentistHolidaySaved = dentistHolidayRepository.saveAll(dentistHoliday);


            //Mapear DTO respuesta.
            //Lista interna del DTO principal de respuesta con los feriados
            List<DentistHolidayListResponseDTO> holidayListResponseDTOS = dentistHolidaySaved
                    .stream()
                    .map(dhs-> new DentistHolidayListResponseDTO(
                            dhs.getId(),
                            dhs.getHoliday().getId(),
                            dhs.getStartTime(),
                            dhs.getEndTime()
                    ))
                    .toList();


            //Dto respuesta
            DentistHolidayResponseDTO dentistHolidayResponseDTO = new DentistHolidayResponseDTO(
                    dentist.getId(),
                    holidayListResponseDTOS
            );

            String messageUser = messageSource.getMessage("dentistHolidayService.update.user.ok", null, LocaleContextHolder.getLocale());

            return new Response<>(true,messageUser, dentistHolidayResponseDTO);


        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistHolidayService", idDentist, null, "update");
        }
    }

    /**
     * Método para obtener la relación entre un dentista y los feriados.
     *
     * @param idDentist : Id Dentista
     * @param year      : Año consultado
     */
    @Override
    public Response<DentistHolidayResponseDTO> get(Long idDentist, Integer year) {
        try{

            List<DentistHoliday> dentistHolidays = dentistHolidayRepository.findAllByDentistId(idDentist, year);

            List<DentistHolidayListResponseDTO> dentistHolidayList = dentistHolidays
                    .stream()
                    .map(dentistHoliday -> new DentistHolidayListResponseDTO(
                            dentistHoliday.getId(),
                            dentistHoliday.getHoliday().getId(),
                            dentistHoliday.getStartTime(),
                            dentistHoliday.getEndTime()
                    ))
                    .toList();


            DentistHolidayResponseDTO responseDTO = new DentistHolidayResponseDTO(
                    idDentist,
                    dentistHolidayList
            );

            return new Response<>(true, null, responseDTO);


        }catch (CannotCreateTransactionException | DataAccessException e) {
            throw new DataBaseException(e, "DentistHolidayService", idDentist, null, "get");
        }
    }






    /**
     * Método privado para construir objeto de relación  entre dentista y feriado.
     * @param dentist : Dentista
     * @param holidays : Lista de feriados
     * @param dentistHolidayRequestDTO : Request con la relación a construir.
     * @param userSec : Usuario que crea la relación (Al llamarse desde la tarea programada o método manual puede ser un usuario autenticado o usuario de sistema. El método llamador envía el usuario correspondiente)
     */
    private List<DentistHoliday> buildHolidayDentist(Dentist dentist , List<Holiday> holidays, DentistHolidayRequestDTO dentistHolidayRequestDTO, UserSec userSec) {

        Map<Long,Holiday> holidayMap = new HashMap<>();

        holidays.forEach(holiday -> holidayMap.put(holiday.getId(), holiday));

        //Arma las nuevas entidades para persistir la relación.
       return dentistHolidayRequestDTO.holiday()
                .stream()
                .map(dentistHolidayDTO -> {
                    DentistHoliday dentistHoliday = new DentistHoliday();
                    dentistHoliday.setDentist(dentist);

                    Holiday holiday =  holidayMap.get(dentistHolidayDTO.idHoliday());
                    dentistHoliday.setHoliday(holiday);

                    dentistHoliday.setStartTime(dentistHolidayDTO.startTime());
                    dentistHoliday.setEndTime(dentistHolidayDTO.endTime());
                    dentistHoliday.setCreatedAt(LocalDateTime.now());
                    dentistHoliday.setCreatedBy(userSec);
                    dentistHoliday.setEnabled(true);
                    return dentistHoliday;
                })
                .toList();

    }



    /**
     * Método para validar si existen los feriados dentro de una lista.
     *
     * @param holidays : Lista de feriados a validar.
     */
    private List<Holiday> validateHolidaysExist(List<DentistHolidayListRequestDTO> holidays, int year) {

        //Traemos en una sola consulta todos los feriados por año.
        List<Holiday> holidaysDatabase =  holidayRepository.findAllByYear(year);

        //Hacemos un maps de ID.
        Map<Long,Holiday> holidaysMap = new HashMap<>();


        //Verificamos contra la lista.
        holidaysDatabase.forEach(holiday -> {
            holidaysMap.put(holiday.getId(), holiday);
        });

        List<Holiday> holiday = new ArrayList<>();
        holidays.forEach(h -> {
                    Holiday holidayExist = holidaysMap.get(h.idHoliday());
                    if (holidayExist == null) {
                        throw new BadRequestException("holidayService.validateHolidaysExist.user", new Object[]{h.idHoliday()}, "holidayService.validateHolidaysExist.log", new Object[]{h.idHoliday(),"Holiday Service","validateHolidaysExist"}, LogLevel.ERROR);
                    }
                    holiday.add(holidayExist);
                }
        );

        return holiday;

    }

}
