package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.enums.SystemParameterKey;
import com.odontologiaintegralfm.model.SystemParameter;
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
    Long findValueByKeyName(@Param("keyName") SystemParameterKey keyName);
}
