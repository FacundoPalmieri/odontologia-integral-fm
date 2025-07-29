package com.odontologiaintegralfm.service.interfaces;


import com.odontologiaintegralfm.dto.ScheduleConfigResponseDTO;
import com.odontologiaintegralfm.model.ScheduleConfig;

import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
public interface IScheduleConfigService {
    /**
     * Obtiene listado de todas las tareas programadas con su expresión Cron correspondiente.
     * @return
     */
    List<ScheduleConfigResponseDTO> getAll();

    /**
     * Obtiene los detalles de una tarea programada por su ID
     * @param id
     * @return
     */
     ScheduleConfigResponseDTO getById(Long id);


    /**
     * Actualiza la ejecución de la tarea programada.
     * @param scheduleConfig
     * @return
     */
    ScheduleConfig update(ScheduleConfig scheduleConfig);
}
