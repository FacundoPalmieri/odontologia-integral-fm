package com.odontologiaintegralfm.infrastructure.systemparameter.repository;

import com.odontologiaintegralfm.infrastructure.systemparameter.enums.SystemParameterKey;
import com.odontologiaintegralfm.infrastructure.systemparameter.model.SystemParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ISystemParameterRepository extends JpaRepository<SystemParameter, Long> {

    @Query("""
    SELECT sp.value
    FROM SystemParameter sp
    WHERE sp.keyName = :keyName

    """)
    String findValueByKeyName(@Param("keyName") SystemParameterKey keyName);
}
