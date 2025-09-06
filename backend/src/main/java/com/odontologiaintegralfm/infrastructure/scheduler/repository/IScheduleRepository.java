package com.odontologiaintegralfm.infrastructure.scheduler.repository;

import com.odontologiaintegralfm.shared.enums.ScheduledTaskKey;
import com.odontologiaintegralfm.infrastructure.scheduler.model.ScheduleTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface IScheduleRepository extends JpaRepository<ScheduleTask, Long> {

    @Query("""
     SELECT s.cronExpression
     FROM ScheduleTask s
     WHERE s.keyName = :keyName
     """)
    String findCronExpressionByKeyName(@Param("keyName") ScheduledTaskKey keyName);

}
