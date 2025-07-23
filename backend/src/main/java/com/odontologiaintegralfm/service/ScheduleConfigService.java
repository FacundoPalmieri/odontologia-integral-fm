package com.odontologiaintegralfm.service;


import com.odontologiaintegralfm.exception.DataBaseException;
import lombok.extern.slf4j.Slf4j;
import com.odontologiaintegralfm.model.ScheduleConfig;
import com.odontologiaintegralfm.repository.IScheduleConfigRepository;
import com.odontologiaintegralfm.service.interfaces.IScheduleConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import static com.mysql.cj.conf.PropertyKey.logger;

/**
 * @author [Facundo Palmieri]
 */
@Service
@Slf4j
public class ScheduleConfigService implements IScheduleConfigService {
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
     *     <li><b>Día de la semana</b> (0-6 o SUN-SAT)</li>
     * </ul>
     * Por ejemplo: {@code "0 0 21 1 * *"} ejecuta la tarea el día 1 de cada mes a las 21:00 hs.
     *
     * @return {@link String} que contiene la expresión cron actual configurada.
     */
    @Override
    public String getSchedule() {
        try {
            return scheduleConfigRepository.findFirstByOrderByIdAsc()
                    .map(ScheduleConfig::getCronExpression)
                    .orElseGet(() -> {
                        log.warn("⚠️ No se encontró configuración CRON en BD. Usando valor por defecto: 0 0 21 1 * *");
                        return "0 0 21 1 * *"; // valor por defecto
                    });
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ScheduleConfig", 1L, "", "getSchedule");
        }
    }

    /**
     * Actualiza la ejecución de la tarea programada.
     *
     * @param cronExpression
     * @return
     */
    @Override
    public int updateSchedule(String cronExpression) {
        try{
            return scheduleConfigRepository.update(cronExpression);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ScheduleConfig", 1L, "", "updateSchedule");
        }
    }
}
