package com.odontologiaintegralfm.feature.appointment.core.repository;

import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IAppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
            SELECT a
            FROM Appointment a
            WHERE a.dentist.id = :dentistId
            AND a.date > :date
            """)
    List<Appointment> findFutureAppointmentsByDentist(@Param("dentistId") Long dentistId, @Param("date") LocalDateTime date);

}
