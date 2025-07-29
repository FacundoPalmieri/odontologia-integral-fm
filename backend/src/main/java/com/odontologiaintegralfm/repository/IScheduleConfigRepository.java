package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.ScheduleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface IScheduleConfigRepository extends JpaRepository<ScheduleConfig, Long> {
}
