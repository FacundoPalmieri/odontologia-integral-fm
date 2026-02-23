package com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.repository;


import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.model.DentistHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDentistHolidayRepository extends JpaRepository<DentistHoliday, Long> {

    @Query("""
            SELECT dh
            FROM DentistHoliday dh
            JOIN dh.holiday h
            WHERE dh.dentist.id = :idDentist
            AND h.year = :year
            """)
    List<DentistHoliday> findAllByDentistId(@Param("idDentist") Long idDentist,
                                            @Param("year") Integer year);




   Optional<DentistHoliday> findByDentistIdAndHolidayId(Long dentistId, Long holidayId);


}
