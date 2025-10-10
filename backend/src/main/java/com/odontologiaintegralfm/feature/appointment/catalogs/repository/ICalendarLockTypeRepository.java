package com.odontologiaintegralfm.feature.appointment.catalogs.repository;

import com.odontologiaintegralfm.feature.appointment.catalogs.model.CalendarLockType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICalendarLockTypeRepository extends JpaRepository<CalendarLockType, Long> {

    List<CalendarLockType> findAllByEnabledTrueOrderByNameAsc();
    CalendarLockType findByName(String name);

}
