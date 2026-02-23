package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.repository;


import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.AppointmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAppointmentStatusHistoryRepository extends JpaRepository<AppointmentStatusHistory, Long> {
}
