package com.odontologiaintegralfm.feature.appointment.core.repository;

import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IDentistCalendarLockRepository extends JpaRepository<DentistCalendarLock, Long> {

    @Query("""

           SELECT dcl
           FROM DentistCalendarLock dcl
           WHERE dcl.dentist.id = :dentistId
           AND (dcl.endDate IS null OR dcl.endDate > CURRENT_DATE)
           """)
    List<DentistCalendarLock> findAllCurrentByDentistId(@Param("dentistId") Long dentist);


    /**
     * Obtiene bloqueos dentro de un rango de fechas, se usa para vista diaria
     * @param dentistId : id dentista
     * @param date : fecha del día a evaluar
     */
    @Query("""
           SELECT dcl
           FROM DentistCalendarLock dcl
           WHERE dcl.dentist.id = :dentistId
           AND dcl.startDate <= :date
           AND dcl.endDate >= :date
           """)
    List<DentistCalendarLock> findByDentistIdAndDate(@Param("dentistId") Long dentistId,
                                                     @Param("date") LocalDate date);


    /**
     * Obtiene bloqueos dentro de un rango de fechas, se usa para vista semanal y mensual
     * @param dentistId : id dentista
     * @param startDate : fecha inicio
     * @param endDate : fecha fin
     */
    @Query("""
       SELECT dcl
       FROM DentistCalendarLock dcl
       WHERE dcl.dentist.id = :dentistId
       AND dcl.startDate <= :endDate
       AND dcl.endDate >= :startDate
       """)
    List<DentistCalendarLock> findByDentistIdAndDateRange(
            @Param("dentistId") Long dentistId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


}