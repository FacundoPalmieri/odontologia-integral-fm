package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service;


import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.AppointmentStatusHistory;

import java.util.List;

public interface IAppointmentStatusHistoryService {

    void save(AppointmentStatusHistory appointmentStatusHistory);

    void saveAll(List<AppointmentStatusHistory> appointmentStatusHistory);
}
