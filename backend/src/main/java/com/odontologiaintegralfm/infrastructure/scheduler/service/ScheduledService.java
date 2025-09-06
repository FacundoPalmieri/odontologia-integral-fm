package com.odontologiaintegralfm.infrastructure.scheduler.service;


import com.odontologiaintegralfm.infrastructure.scheduler.dto.ScheduleResponseDTO;
import com.odontologiaintegralfm.infrastructure.logging.dto.SystemLogResponseDTO;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.enums.ScheduledTaskKey;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.infrastructure.scheduler.model.ScheduleTask;
import com.odontologiaintegralfm.infrastructure.message.service.interfaces.IMessageService;
import com.odontologiaintegralfm.infrastructure.logging.service.SystemLogService;
import lombok.extern.slf4j.Slf4j;
import com.odontologiaintegralfm.infrastructure.scheduler.repository.IScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
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
public class ScheduledService implements IScheduleService {
    @Autowired
    private IScheduleRepository scheduleRepository;

    @Autowired
    private IMessageService messageService;
    @Autowired
    private SystemLogService systemLogService;



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
    public List<ScheduleResponseDTO> getAll() {
        try {
            List<ScheduleTask> entities = scheduleRepository.findAll();

            if (entities.isEmpty()) {
                log.warn("⚠️ No se encontró configuración CRON en BD. Usando valor por defecto: 0 0 21 1 * *");
                return List.of(new ScheduleResponseDTO(null, null, "0 0 21 1 * *"));
            }

            return entities.stream()
                    .map(e -> new ScheduleResponseDTO(e.getId(), e.getLabel(), e.getCronExpression()))
                    .toList();
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ScheduleTask", null, null, "getAll");
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
    public ScheduleResponseDTO getById(Long id) {
        try {

            Optional<ScheduleTask> entity = scheduleRepository.findById(id);

            if (entity.isPresent()) {
                return new ScheduleResponseDTO(entity.get().getId(), entity.get().getLabel(), entity.get().getCronExpression());
            } else {
                log.warn("⚠️ No se encontró configuración CRON en BD para la tarea ID " + id + ". Usando valor por defecto: 0 0 21 1 * *");
                return new ScheduleResponseDTO(null, null, "0 0 21 1 * *");
            }
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ScheduleTask", id, "", "getById");
        }
    }

    /**
     * Obtiene el cron por el keyName
     * Se utiliza para método interno de SchedulerInitializerConfig
     *
     * @param keyName
     * @return expresión Cron
     */
    @Override
    public String getByKeyName(ScheduledTaskKey keyName) {
        try{
            //Obtiene la expresión Cron desde BD.
            String cron = scheduleRepository.findCronExpressionByKeyName(keyName);

            //Si no se obtiene la expresión Cron desde la base, se devuelve una por defecto y se loguea.
            if(cron == null || cron.isBlank()){
                cron = "0 0 21 1 * *";

                String message = messageService.getMessage("scheduleService.getByKeyName.error",new Object[]{keyName, cron}, LocaleContextHolder.getLocale());

                SystemLogResponseDTO dto = new SystemLogResponseDTO(
                        LogLevel.WARN,
                        LogType.SCHEDULED,
                        null,
                        message,
                        "LogActionAspect",
                        null,
                        null,
                        null
                );
                systemLogService.save(dto);


            }else{
                //En caso de obtener la configuración solo se loguea como INFO.
                String message = messageService.getMessage("scheduleService.getByKeyName.ok",new Object[]{keyName,cron}, LocaleContextHolder.getLocale());

                SystemLogResponseDTO dto = new SystemLogResponseDTO(
                        LogLevel.INFO,
                        LogType.SCHEDULED,
                        null,
                        message,
                        "LogActionAspect",
                        null,
                        null,
                        null
                );
                systemLogService.save(dto);
            }
            return cron;

        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ScheduleTask", null, keyName.toString(), "getByKeyName");
        }
    }


    /**
     * Actualiza la ejecución de la tarea programada.
     *
     * @param scheduleTask
     * @return
     */
    @Override
    @Transactional
    public ScheduleTask update(ScheduleTask scheduleTask) {
        try{
            return scheduleRepository.save(scheduleTask);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ScheduleTask", scheduleTask.getId(), scheduleTask.getKeyName().toString(), "update");
        }
    }
}
