package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.Patient;
import com.odontologiaintegralfm.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface IPersonRepository extends JpaRepository<Person, Long> {
    @Query(""" 
           SELECT p FROM Person p 
           WHERE p.dniType.Id = :dniTypeId 
           AND p.dni = :dni""")
    Optional<Person> findByDniTypeIdAndDni (@Param("dniTypeId") Long dniTypeId, @Param("dni") String dni);
}
