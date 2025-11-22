package com.odontologiaintegralfm.feature.appointment.core.repository;


import com.odontologiaintegralfm.feature.appointment.core.model.DentistCalendarLockDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IDentistCalendarLockDetailRepository extends JpaRepository<DentistCalendarLockDetail, Long> {

    List<DentistCalendarLockDetail> findByDentistCalendarLockId(Long dentistCalendarId);
}
