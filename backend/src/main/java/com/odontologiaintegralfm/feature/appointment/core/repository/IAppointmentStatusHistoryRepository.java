package com.odontologiaintegralfm.feature.appointment.core.repository;


import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAppointmentStatusHistoryRepository extends JpaRepository<AppointmentStatusHistory, Long> {
}
