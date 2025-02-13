package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.FailedLoginAttemptsConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IFailedLoginAttemptsRepository extends JpaRepository<FailedLoginAttemptsConfig, Long> {

    @Query("SELECT f.value FROM FailedLoginAttemptsConfig f")
    Integer findFirst();

    @Modifying
    @Query("UPDATE FailedLoginAttemptsConfig f SET f.value = :value")
    void update(@Param("value") Integer value);
}


