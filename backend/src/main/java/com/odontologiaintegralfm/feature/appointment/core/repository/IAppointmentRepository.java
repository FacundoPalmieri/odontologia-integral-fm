package com.odontologiaintegralfm.feature.appointment.core.repository;

import com.odontologiaintegralfm.feature.appointment.catalogs.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IAppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("""
            SELECT a
            FROM Appointment a
            WHERE a.dentist.id = :dentistId
            AND a.date > :date
            AND a.status = :status
            """)
    List<Appointment> findFutureAppointmentsReservedByDentist(@Param("dentistId") Long dentistId,
                                                              @Param("date") LocalDateTime date,
                                                              @Param("status") AppointmentStatus status
    );


    Optional<Appointment> findByDentistIdAndDate(Long dentistId, LocalDateTime dateTime);

}
