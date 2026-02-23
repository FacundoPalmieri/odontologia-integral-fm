package com.odontologiaintegralfm.feature.appointmentscheduling.locktype.repository;

import com.odontologiaintegralfm.feature.appointmentscheduling.locktype.model.CalendarLockType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICalendarLockTypeRepository extends JpaRepository<CalendarLockType, Long> {

    List<CalendarLockType> findAllByEnabledTrueOrderByNameAsc();
    CalendarLockType findByName(String name);

}
