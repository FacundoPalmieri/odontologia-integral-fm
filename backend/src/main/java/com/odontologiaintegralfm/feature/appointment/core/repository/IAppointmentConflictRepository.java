package com.odontologiaintegralfm.feature.appointment.core.repository;

import com.odontologiaintegralfm.feature.appointment.core.enums.OriginConflict;
import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface IAppointmentConflictRepository extends JpaRepository<AppointmentConflict, Long> {

    @Query("""
     SELECT ac
            FROM AppointmentConflict ac
            JOIN ac.appointment ap
            WHERE ac.appointment.dentist.id = :idDentist
            AND ac.originConflict = :originConflict
            AND ap.date > :date
    """)
    List<AppointmentConflict> findAllByDentistAndOriginConflict(@Param("idDentist")Long dentistId,
                                                                @Param("originConflict") OriginConflict originConflict,
                                                                @Param("date") LocalDateTime date);



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



    @Query("""
            SELECT ac
            FROM AppointmentConflict ac
            JOIN ac.appointment ap
            WHERE ac.appointment.dentist.id = :idDentist
            AND ac.idOriginConflict = :idOriginConflict
            AND ac.resolved = false
            """)
   List<AppointmentConflict> findAllByDentistIdAndIdOriginConflict(Long idDentist,Long idOriginConflict);


    @Modifying
    @Query("""
            UPDATE AppointmentConflict ac
            SET ac.resolved  = true,
                ac.updatedAt = :updatedAt,
                ac.updatedBy = :updatedBy
            WHERE ac.id in :ids
            
            """)
    void resolved(@Param("ids") List<Long> ids,
                  @Param("updatedAt") LocalDateTime updatedAt,
                  @Param("updatedBy")UserSec updatedBy);

}
