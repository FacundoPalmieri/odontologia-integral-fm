package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.ScheduleConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface IScheduleConfigRepository extends JpaRepository<ScheduleConfig, Long> {

    Optional<ScheduleConfig> findFirstByOrderByIdAsc();


    @Transactional
    @Modifying
    @Query("""
           UPDATE ScheduleConfig sc
           SET sc.cronExpression = :newValor
           """)
    int update(@Param("newValor") String newValor);

}
