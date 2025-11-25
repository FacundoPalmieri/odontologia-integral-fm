package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;


import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentStatusHistory;

import java.util.List;

public interface IAppointmentStatusHistoryService {

    void save(AppointmentStatusHistory appointmentStatusHistory);

    void saveAll(List<AppointmentStatusHistory> appointmentStatusHistory);
}
