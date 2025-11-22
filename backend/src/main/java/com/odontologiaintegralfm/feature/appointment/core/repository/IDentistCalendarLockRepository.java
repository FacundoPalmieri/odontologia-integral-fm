package com.odontologiaintegralfm.feature.appointment.core.repository;

import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDentistCalendarLockRepository extends JpaRepository<DentistCalendarLock, Long> {

    @Query("""
           SELECT dcl
           FROM DentistCalendarLock dcl
           WHERE dcl.dentist.id = :idDentist
           OR dcl.endDate IS null
           OR dcl.endDate > CURRENT_DATE
           """)
    List<DentistCalendarLock> findAllCurrentByDentistId(@Param("idDentist") Long dentist);

}
