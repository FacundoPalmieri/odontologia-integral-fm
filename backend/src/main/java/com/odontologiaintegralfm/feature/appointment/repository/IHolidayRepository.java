package com.odontologiaintegralfm.feature.appointment.repository;

import com.odontologiaintegralfm.feature.appointment.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface IHolidayRepository extends JpaRepository<Holiday, Long> {
}
