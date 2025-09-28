package com.odontologiaintegralfm.feature.appointment.core.repository;

import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface IAppointmentConflictRepository extends JpaRepository<AppointmentConflict, Long> {

    @Query("""
            SELECT ac
            FROM AppointmentConflict ac
            JOIN ac.appointment ap
            WHERE ac.appointment.dentist.id = :idDentist
            AND ap.date > :date
            """)
    List<AppointmentConflict> findAllByIdDentist(@Param("idDentist") Long idDentist,
                                                 @Param("date") LocalDateTime date
    );


    @Query("""
            SELECT ac
            FROM AppointmentConflict ac
            JOIN ac.appointment ap
            WHERE ac.appointment.dentist.id = :idDentist
            AND ac.resolved = false
            """)
    List<AppointmentConflict> findByIdDentistAndResolvedFalse(@Param("idDentist") Long idDentist);
}
