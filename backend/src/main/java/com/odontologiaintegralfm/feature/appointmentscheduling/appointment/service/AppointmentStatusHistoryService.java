package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service;


import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.AppointmentStatusHistory;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.repository.IAppointmentStatusHistoryRepository;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.List;

@Service
public class AppointmentStatusHistoryService {

    @Autowired
    private IAppointmentStatusHistoryRepository appointmentStatusHistoryRepository;


    public void save(AppointmentStatusHistory appointmentStatusHistory) {
        try{
            appointmentStatusHistoryRepository.save(appointmentStatusHistory);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentStatusHistoryService", appointmentStatusHistory.getId(), null, "save");
        }

    }


    public void saveAll(List<AppointmentStatusHistory> appointmentStatusHistory) {
        try{
            appointmentStatusHistoryRepository.saveAll(appointmentStatusHistory);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentStatusHistoryService", null, null, "saveAll");
        }
    }
}
