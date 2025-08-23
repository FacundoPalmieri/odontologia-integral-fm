package com.odontologiaintegralfm.infrastructure.logging.repository;

import com.odontologiaintegralfm.infrastructure.logging.model.SystemLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface ISystemLogRepository extends JpaRepository<SystemLog, Long> {

    Page<SystemLog> findAll(Pageable pageable);





    @Modifying
    @Query("""
    DELETE 
    FROM SystemLog sl
    WHERE sl.timestamp < :deadline
    """)
    int deleteBeforeDeadline(@Param("deadline") LocalDateTime deadline);
}
