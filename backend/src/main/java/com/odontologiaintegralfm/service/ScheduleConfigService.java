package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.ScheduleConfig;
import com.odontologiaintegralfm.repository.IScheduleConfigRepository;
import com.odontologiaintegralfm.service.interfaces.IScheduleConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

/**
 * @author [Facundo Palmieri]
 */
@Service
public class ScheduleConfigService implements IScheduleConfigService {
    @Autowired
    private IScheduleConfigRepository scheduleConfigRepository;


    /**
     * Obtiene la configuración la tarea programada.
     *
     * @return
     */
    @Override
    public String getSchedule() {
        try{
            ScheduleConfig scheduleConfig = scheduleConfigRepository.findFirstByOrderByIdAsc()
                    .orElseThrow(() -> new NotFoundException("exception.scheduleNotFound.user",null,"exception.scheduleNotFound.log",new Object[]{"Token Config Service","getSchedule"}, LogLevel.ERROR));

            return scheduleConfig.getCronExpression();
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
