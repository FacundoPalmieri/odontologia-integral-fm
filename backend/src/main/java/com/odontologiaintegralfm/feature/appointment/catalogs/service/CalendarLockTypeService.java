package com.odontologiaintegralfm.feature.appointment.catalogs.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.CalendarLockModeResponseDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.CalendarLockTypeCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.CalendarLockTypeResponseDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.CalendarLockTypeUpdateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.CalendarLockMode;
import com.odontologiaintegralfm.feature.appointment.catalogs.model.CalendarLockType;
import com.odontologiaintegralfm.feature.appointment.catalogs.repository.ICalendarLockTypeRepository;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.shared.dto.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CalendarLockTypeService implements ICalendarLockTypeService {

    @Autowired
    private ICalendarLockTypeRepository calendarLockTypeRepository;


    @Qualifier("messageSource")
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private AuthenticatedUserService authenticatedUserService;


    /**
     * Obtener todos los tipos de bloqueos de agenda.
     */
    @Override
    public Response<List<CalendarLockTypeResponseDTO>> getAll() {
        try {
            List<CalendarLockType> calendarLockTypes = calendarLockTypeRepository.findAllByEnabledTrueOrderByNameAsc();


            List<CalendarLockTypeResponseDTO> calendarLockTypeResponseDTO = calendarLockTypes.stream()
                    .sorted(Comparator.comparing(CalendarLockType::getId))
                    .map(CalendarLockTypeResponseDTO::from)
                    .toList();

            return new Response<>(true, null, calendarLockTypeResponseDTO);

        } catch (CannotCreateTransactionException | DataAccessException e) {
            throw new DataBaseException(e, "CalendarLockTypeService", null, null, "getAll");
        }
    }



    /**
     * Obtiene un tipo de bloqueo por su ID.
     */
    @Override
    public Response<CalendarLockTypeResponseDTO> getById(Long id) {
        try{

            CalendarLockType calendarLockType = calendarLockTypeRepository.findById(id)
                    .orElseThrow(()-> new NotFoundException("exception.calendarLockType.notFound.user", null,"exception.calendarLockType.notFound.log",new Object[]{id,"CalendarLockTypeService","getById"}, LogLevel.ERROR));


            CalendarLockTypeResponseDTO calendarLockTypeResponseDTO = CalendarLockTypeResponseDTO.from(calendarLockType);
            return new Response<>(true, null, calendarLockTypeResponseDTO);

        }catch (CannotCreateTransactionException | DataAccessException e) {
            throw new DataBaseException(e, "CalendarLockTypeService", id, null, "getById");
        }

    }

    /**
     * Obtiene los diferentes modos para un tipo de bloqueo.
     */
    @Override
    public Response<Set<CalendarLockModeResponseDTO>> getModes() {
      Set <CalendarLockModeResponseDTO> calendarLockModeResponseDTO = CalendarLockMode
              .getAll()
              .stream()
              .map(CalendarLockModeResponseDTO::build)
              .collect(Collectors.toSet());

      return new Response<>(true, null, calendarLockModeResponseDTO);
    }

    /**
     * Obtiene un tipo de bloqueo por su ID.
     * Método interno de validación.
     */
    @Override
    public CalendarLockType getByIdInternal(Long id) {
        try{
            return calendarLockTypeRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("exception.calendarLockType.notFound.user", null, "exception.calendarLockType.notFound.log", new Object[]{id, "DentistCalendarLockService", "create"}, LogLevel.ERROR));
        }catch (CannotCreateTransactionException | DataAccessException e) {
            throw new DataBaseException(e, "CalendarLockTypeService", id, null, "getByIdInternal");
        }
    }


    /**
     * Crear un nuevo tipo de bloqueo de agenda.
     */
    @Override
    @LogAction(
            value = "calendarLockTypeService.logAction.create",
            args = {"#result.data.id", "#result.data.name", "#result.data.enabled"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<CalendarLockTypeResponseDTO> create(CalendarLockTypeCreateRequestDTO calendarLockTypeCreateRequestDTO) {
        try {

            CalendarLockType existing = calendarLockTypeRepository.findByName(calendarLockTypeCreateRequestDTO.name());

            if (existing != null) {
                throw new ConflictException("exception.calendarLockType.duplicate.user", null, "exception.calendarLockType.duplicate.log", new Object[]{calendarLockTypeCreateRequestDTO.name(), "CalendarLockTypeService", "create"}, LogLevel.ERROR);
            }


            CalendarLockType calendarLockType = new CalendarLockType();
            calendarLockType.setName(calendarLockTypeCreateRequestDTO.name());
            calendarLockType.setModes(calendarLockTypeCreateRequestDTO.mode());
            calendarLockType.setCreatedAt(LocalDateTime.now());
            calendarLockType.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
            calendarLockType.setEnabled(true);

            CalendarLockType calendarLockTypeSaved = calendarLockTypeRepository.save(calendarLockType);

            CalendarLockTypeResponseDTO calendarLockTypeResponseDTO = CalendarLockTypeResponseDTO.from(calendarLockTypeSaved);

            return new Response<>(
                    true,
                    messageSource.getMessage("calendarLockTypeService.create.ok.user", null, LocaleContextHolder.getLocale()),
                    calendarLockTypeResponseDTO
            );
        } catch (CannotCreateTransactionException | DataAccessException e) {
            throw new DataBaseException(e, "CalendarLockTypeService", null, calendarLockTypeCreateRequestDTO.name(), "create");
        }

    }

    /**
     * Actualizar un tipo de bloqueo de agenda.
     */
    @Override
    @LogAction(
            value = "calendarLockTypeService.logAction.update",
            args = {"#result.data.id", "#result.data.name", "#result.data.enabled"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<CalendarLockTypeResponseDTO> update(Long id , CalendarLockTypeUpdateRequestDTO calendarLockTypeUpdateRequestDTO) {
        try{

            //Validar que exista el tipo de feriado
            CalendarLockType calendarLockType = calendarLockTypeRepository.findById(id)
                    .orElseThrow(()-> new NotFoundException("exception.calendarLockType.notFound.user", null,"exception.calendarLockType.notFound.log",new Object[]{id,"CalendarLockTypeService","getById"}, LogLevel.ERROR));


            //Actualiza campos en el objeto recuperado desde BD.
            calendarLockType.setName(calendarLockTypeUpdateRequestDTO.name());
            calendarLockType.setEnabled(calendarLockTypeUpdateRequestDTO.enabled());
            calendarLockType.setUpdatedAt(LocalDateTime.now());
            calendarLockType.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());

            //Persiste.
            CalendarLockType calendarLockTypeSaved = calendarLockTypeRepository.save(calendarLockType);

            //Mapea respuesta.

            CalendarLockTypeResponseDTO calendarLockTypeResponseDTO = CalendarLockTypeResponseDTO.from(calendarLockTypeSaved);

            return new Response<>(
                    true,
                    messageSource.getMessage("calendarLockTypeService.update.ok.user", null, LocaleContextHolder.getLocale()),
                    calendarLockTypeResponseDTO
            );


        }catch (CannotCreateTransactionException | DataAccessException e) {
            throw new DataBaseException(e, "CalendarLockTypeService", null, calendarLockTypeUpdateRequestDTO.name(), "create");
        }
    }
}
