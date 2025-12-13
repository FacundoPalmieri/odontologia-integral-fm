package com.odontologiaintegralfm.feature.appointment.catalogs.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedSystemService;
import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.HolidayCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.HolidayResponseDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.HolidayUpdateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.repository.IHolidayRepository;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.HolidayType;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.Holiday;
import com.odontologiaintegralfm.infrastructure.externalapi.client.ArgentinaDatosClient;
import com.odontologiaintegralfm.infrastructure.externalapi.dto.HolidayApiResponseDTO;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import org.springframework.context.MessageSource;
import com.odontologiaintegralfm.infrastructure.scheduler.dto.internal.SchedulerResultDTO;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.shared.dto.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author [Facundo Palmieri]
 */
@Service
public class HolidayService implements IHolidayService {

    @Autowired
    private ArgentinaDatosClient argentinaDatosClient;

    @Autowired
    private IHolidayRepository holidayRepository;

    @Autowired
    private AuthenticatedSystemService authenticatedSystemService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private MessageSource messageSource;


    /**
     * Método para obtener la lista páginada de todos los feriados.
     * @param year      : Año a consultar.
     * @return
     */
    @Override
    public Response<List<HolidayResponseDTO>> getAll(int year) {
        try{

            List<Holiday> holidays = holidayRepository.findAllByYear(year);

            List<HolidayResponseDTO> holidayResponseDTOS = holidays.stream()
                    .map(holiday -> new HolidayResponseDTO(
                            holiday.getId(),
                            holiday.getDate(),
                            holiday.getType().getLabel(),
                            holiday.getName()
                    ))
                    .toList();

            return new Response<>(true, null, holidayResponseDTOS);
        }catch(CannotCreateTransactionException | DataAccessException e ) {
            throw new DataBaseException(e, "HolidayService",null,null, "getAll");
        }
    }

    /**
     * Método interno de la aplicación para valida la existencia de un feriado.
     *
     * @param id : id del feriado.
     */
    @Override
    public Holiday getByIdInternal(Long id) {
        try{
            return holidayRepository.findById(id)
                    .orElseThrow(()-> new NotFoundException("exception.holidayNotFound.user",null,"exception.holidayNotFound.log", new Object[]{id,"HolidayService","getByIdInternal"},LogLevel.ERROR));
        }catch(CannotCreateTransactionException | DataAccessException e ) {
            throw new DataBaseException(e, "HolidayService",id,null, "getByIdInternal");
        }
    }


    /**
     * Método para crear un feriado.
     * @param holidayCreateRequestDTO
     * @return
     */
    @LogAction(
            value = "holidayService.logAction.createHoliday",
            args = {"#result.data.id", "#result.data.date", "#result.data.type", "#result.data.name"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    @Override
    public Response<HolidayResponseDTO> create(HolidayCreateRequestDTO holidayCreateRequestDTO) {

        //Validar que el feriado a actualizar no sea anterior al día actual.
        validateNotBeforeDate(null, holidayCreateRequestDTO.name(), holidayCreateRequestDTO.date());

        // Valído que no exista un feriado para esa fecha
        validateHolidayCreate(holidayCreateRequestDTO.date());


        // Mapeo el DTO a Holiday
        Holiday holiday = new Holiday();
        holiday.setDate(holidayCreateRequestDTO.date());
        holiday.setType(HolidayType.valueOf(holidayCreateRequestDTO.type().toUpperCase()));
        holiday.setName(holidayCreateRequestDTO.name());
        holiday.setYear(holidayCreateRequestDTO.date().getYear());
        holiday.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        holiday.setCreatedAt(LocalDateTime.now());
        holiday.setEnabled(true);

        // Guardo
        holiday =  holidayRepository.save(holiday);

        // Mapeo al DTO
        HolidayResponseDTO holidayResponseDTO = new HolidayResponseDTO(
                holiday.getId(),
                holiday.getDate(),
                holiday.getType().getLabel(),
                holiday.getName()
        );

        //Obtengo mensaje
        String  messageUser = messageSource.getMessage("holidayService.create.user.ok",null, LocaleContextHolder.getLocale());

        //Elaboro respuesta
        return new Response<>(true, messageUser, holidayResponseDTO);

    }


    /**
     * Método para actualiza datos de un feriado.
     *
     * @param holidayUpdateRequestDTO : DTO con el feriado a actualizar.
     */
    @LogAction(
            value = "holidayService.logAction.updateHoliday",
            args = {"#result.data.id", "#result.data.date", "#result.data.type", "#result.data.name"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO

    )
    @Override
    public Response<HolidayResponseDTO> update(HolidayUpdateRequestDTO holidayUpdateRequestDTO) {

        //Validar que el feriado a actualizar no sea anterior al día actual.
        validateNotBeforeDate(holidayUpdateRequestDTO.id(), holidayUpdateRequestDTO.name(), holidayUpdateRequestDTO.date());

        // Valído que no exista un feriado para esa fecha
        validateHolidayUpdate(holidayUpdateRequestDTO.date());

        //Buscar el feriado en la base.
         Holiday holiday = holidayRepository.findById(holidayUpdateRequestDTO.id())
                .orElseThrow(() -> new NotFoundException("exception.holidayNotFound.user", null, "exception.holidayNotFound.log", new Object[]{holidayUpdateRequestDTO.id(), "Holiday Service", "Update"}, LogLevel.WARN));



        //Actualizar los datos de la entidad con los del DTO.
        holiday.setDate(holidayUpdateRequestDTO.date());
        holiday.setType(HolidayType.valueOf(holidayUpdateRequestDTO.type().toUpperCase()));
        holiday.setName(holidayUpdateRequestDTO.name());
        holiday.setYear(holidayUpdateRequestDTO.date().getYear());
        holiday.setUpdatedAt(LocalDateTime.now());
        holiday.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());

        // Persistir
        Holiday holidaySaved = holidayRepository.save(holiday);

        //Construye DTO respuesta
        HolidayResponseDTO holidayResponseDTO = new HolidayResponseDTO(
                holidaySaved.getId(),
                holidaySaved.getDate(),
                holidaySaved.getType().getLabel(),
                holidaySaved.getName()
        );

        //Construyo mensaje
        String messageUser = messageSource.getMessage("holidayService.update.user.ok",null, LocaleContextHolder.getLocale());

        //Devuelvo respuesta.
        return new Response<>(true, messageUser, holidayResponseDTO);
    }





    /**
     * Método para validar si existe un feriado en una fecha determinada.
     */
    public Optional<Holiday> getByDate(LocalDate date){
        return holidayRepository.findByDate(date);
    }





    /**
     * Método privado que valida si antes de crear un nuevo feriado, no existe otro para esa misma fecha.
     * @param date
     */
    private void validateHolidayCreate(LocalDate date) {
        Optional<Holiday> holidayOptional = holidayRepository.findByDate(date);
        if (holidayOptional.isPresent()) {
            throw new ConflictException(
                    "exception.validateHolidayCreate.user",
                    new Object[]{holidayOptional.get().getDate(), holidayOptional.get().getType(), holidayOptional.get().getName()},
                    "exception.validateHolidayCreate.log",
                    new Object[]{holidayOptional.get().getId(), holidayOptional.get().getDate(), holidayOptional.get().getType(), holidayOptional.get().getName(), "HolidayService", "validateHolidayCreate"},
                    LogLevel.WARN);
        }

    }




    /**
     * Método privado que valida si antes de actualizar un nuevo feriado, no existe otro para esa misma fecha.
     * @param date
     */
    private void validateHolidayUpdate(LocalDate date) {
        Optional<Holiday> holidayOptional = holidayRepository.findByDate(date);
        if (holidayOptional.isPresent()) {
            throw new ConflictException(
                    "exception.validateHolidayUpdate.user",
                    new Object[]{holidayOptional.get().getDate(), holidayOptional.get().getType(), holidayOptional.get().getName()},
                    "exception.validateHolidayUpdate.log",
                    new Object[]{holidayOptional.get().getId(), holidayOptional.get().getDate(), holidayOptional.get().getType(), holidayOptional.get().getName(), "HolidayService", "validateHolidayUpdate"}
                    , LogLevel.WARN
            );
        }
    }


    private void validateNotBeforeDate(Long id, String name, LocalDate date){
        if(date.isBefore(LocalDate.now())){
            throw new ConflictException(
                    "exception.holidayBefore.user",
                    null,
                    "exception.holidayBefore.log",
                    new Object[]{id, name, "Holiday Service", "validateNotBeforeDate"}
                    , LogLevel.WARN
            );
        }
    }





    /**
     * Método que consume ArgentinaDatosClient.
     * Mapea la respuesta recibida de la API a la entidad y persiste en BD.
     *
     * @param year año a consultar
     * @return
     */

    @LogAction(
            value = "holidayService.logAction.loadHoliday",
            args = {"#result.durationSeconds", "#result.message", "#result.countInit"},
            type = LogType.SCHEDULED,
            level = LogLevel.INFO
    )
    @Override
    @Transactional
    public SchedulerResultDTO loadHolidays(int year) {
        AtomicInteger count = new AtomicInteger(0);
        long start;
        long end;
        double durationSeconds;

        //Consulta si ya se realizó la carga de feriados.
        int loadedHolidays = holidayRepository.countByYear(year);

        if(loadedHolidays > 0){
            return new SchedulerResultDTO(
                    0,
                    messageSource.getMessage("holidayService.loadHoliday.cancel.log", null, LocaleContextHolder.getLocale()),
                    loadedHolidays,
                    0);
        }

        //Inicia tarea programada
        start = System.currentTimeMillis();

        List<HolidayApiResponseDTO> dto = argentinaDatosClient.fetch(year);

        List<Holiday> holidays = dto.stream()
                .map(d -> {
                    Holiday holiday = new Holiday();
                    holiday.setDate(LocalDate.parse(d.fecha()));
                    holiday.setType(HolidayType.fromLabel(d.tipo()));
                    holiday.setName(d.nombre());
                    holiday.setYear(Year.now().getValue() + 1);
                    holiday.setEnabled(true);
                    holiday.setCreatedBy(authenticatedSystemService.getAuthenticatedUserSystem());
                    holiday.setCreatedAt(LocalDateTime.now());

                    count.incrementAndGet();

                    return holiday;
                })
                .toList();

        // Persiste los feriados.
        holidayRepository.saveAll(holidays);

        //Finaliza tarea programada
        end = System.currentTimeMillis();

        //Convierte milisegundos a segundos.
        durationSeconds = (end - start) / 1000.0;

        SchedulerResultDTO schedulerResultDTO = new SchedulerResultDTO(
                durationSeconds,
                messageSource.getMessage("holidayService.loadHoliday.ok.log", null, LocaleContextHolder.getLocale()),
                count.intValue(),
                0);
        return schedulerResultDTO;

    }
}

