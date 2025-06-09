package com.odontologiaintegralfm.service.interfaces;

/**
 * @author [Facundo Palmieri]
 */
public interface IScheduleConfigService {
    /**
     * Obtiene la configuración la tarea programada.
     * @return
     */
    String getSchedule();


    /**
     * Actualiza la ejecución de la tarea programada.
     * @param cronExpression
     * @return
     */
    int updateSchedule(String cronExpression);
}
