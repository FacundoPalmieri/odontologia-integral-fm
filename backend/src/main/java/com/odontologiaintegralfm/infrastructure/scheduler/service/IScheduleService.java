package com.odontologiaintegralfm.infrastructure.scheduler.service;


import com.odontologiaintegralfm.infrastructure.scheduler.dto.ScheduleResponseDTO;
import com.odontologiaintegralfm.shared.enums.ScheduledTaskKey;
import com.odontologiaintegralfm.infrastructure.scheduler.model.ScheduleTask;

import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
public interface IScheduleService {
    /**
     * Obtiene listado de todas las tareas programadas con su expresión Cron correspondiente.
     * @return
     */
    List<ScheduleResponseDTO> getAll();

    /**
     * Obtiene los detalles de una tarea programada por su ID
     * @param id
     * @return
     */
     ScheduleResponseDTO getById(Long id);

    /**
     * Obtiene el cron por el keyName
     * Se utiliza para método interno de SchedulerInitializerConfig
     * @param keyName
     * @return expresión Cron
     */
     String getByKeyName(ScheduledTaskKey keyName);

    /**
     * Actualiza la ejecución de la tarea programada.
     * @param scheduleTask
     * @return
     */
    ScheduleTask update(ScheduleTask scheduleTask);
}
