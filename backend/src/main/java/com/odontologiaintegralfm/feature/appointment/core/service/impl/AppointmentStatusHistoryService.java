package com.odontologiaintegralfm.feature.appointment.core.service.impl;


import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentStatusHistory;
import com.odontologiaintegralfm.feature.appointment.core.repository.IAppointmentStatusHistoryRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentStatusHistoryService;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

@Service
public class AppointmentStatusHistoryService implements IAppointmentStatusHistoryService {

    @Autowired
    private IAppointmentStatusHistoryRepository appointmentStatusHistoryRepository;


    @Override
    public void save(AppointmentStatusHistory appointmentStatusHistory) {
        try{
            appointmentStatusHistoryRepository.save(appointmentStatusHistory);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AppointmentStatusHistoryService", null, null, "create");
        }

    }
}
