package com.odontologiaintegralfm.service;


import com.odontologiaintegralfm.dto.ScheduleConfigRequestDTO;
import com.odontologiaintegralfm.dto.ScheduleConfigResponseDTO;
import com.odontologiaintegralfm.exception.DataBaseException;
import lombok.extern.slf4j.Slf4j;
import com.odontologiaintegralfm.model.ScheduleConfig;
import com.odontologiaintegralfm.repository.IScheduleConfigRepository;
import com.odontologiaintegralfm.service.interfaces.IScheduleConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Servicio que se encarga de las configuraciones de las tareas programadas. Métodos como getById, update, etc..
 */
@Service
@Slf4j
public class ScheduledConfigService implements IScheduleConfigService {
    @Autowired
    private IScheduleConfigRepository scheduleConfigRepository;


    /**
     * Obtiene la regla que define cuándo se ejecutará la tarea programada.
     * <p>
     * La expresión se representa en el siguiente formato cron de 6 campos:
     * <ul>
     *     <li><b>Segundo</b> (0-59)</li>
     *     <li><b>Minuto</b> (0-59)</li>
     *     <li><b>Hora</b> (0-23)</li>
     *     <li><b>Día del mes</b> (1-31)</li>
     *     <li><b>Mes</b> (1-12 o ENE-DIC)</li>
     *     <li><b>Día de la semana</b> (0-6 o DOM-SAB)</li>
     * </ul>
     * Por ejemplo: {@code "0 0 21 1 * *"} ejecuta la tarea el día 1 de cada mes a las 21:00 hs.
     *
     * @return {@link String} que contiene la expresión cron actual configurada.
     */
    @Override
    public List<ScheduleConfigResponseDTO> getAll() {
        try {
            List<ScheduleConfig> entities = scheduleConfigRepository.findAll();

            if (entities.isEmpty()) {
                log.warn("⚠️ No se encontró configuración CRON en BD. Usando valor por defecto: 0 0 21 1 * *");
                return List.of(new ScheduleConfigResponseDTO(null, null, null, "0 0 21 1 * *"));
            }

            return entities.stream()
                    .map(e -> new ScheduleConfigResponseDTO(e.getId(), e.getName(), e.getLabel(), e.getCronExpression()))
                    .toList();
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ScheduleConfig", null, null, "getAll");
        }
    }


    /**
     * Obtiene los detalles de una tarea programada por su ID
     *
     * @param id
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public ScheduleConfigResponseDTO getById(Long id) {
        try {

            Optional<ScheduleConfig> entity = scheduleConfigRepository.findById(id);

            if (entity.isPresent()) {
                return new ScheduleConfigResponseDTO(entity.get().getId(), entity.get().getName(), entity.get().getLabel(), entity.get().getCronExpression());
            } else {
                log.warn("⚠️ No se encontró configuración CRON en BD para la tarea ID " + id + ". Usando valor por defecto: 0 0 21 1 * *");
                return new ScheduleConfigResponseDTO(null, null, null, "0 0 21 1 * *");
            }
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ScheduleConfig", id, "", "getById");
        }
    }



    /**
     * Actualiza la ejecución de la tarea programada.
     *
     * @param scheduleConfig
     * @return
     */
    @Override
    @Transactional
    public ScheduleConfig update(ScheduleConfig scheduleConfig) {
        try{
            return scheduleConfigRepository.save(scheduleConfig);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ScheduleConfig", 1L, "", "update");
        }
    }
}
