package com.odontologiaintegralfm.feature.appointment.service;

import com.odontologiaintegralfm.feature.appointment.model.Holiday;
import com.odontologiaintegralfm.infrastructure.scheduler.dto.internal.SchedulerResultDTO;

import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
public interface IHolidayService {

    /**
     * Método que consume ArgentinaDatosClient.
     * Mapea la respuesta recibida de la API a la entidad y persiste en BD.
     * @param year año a consultar
     * @return
     */
    SchedulerResultDTO loadHolidays(int year);

}
