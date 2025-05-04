package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface IPatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByDni(String dni);


}
