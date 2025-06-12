package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface IPatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByAffiliateNumberAndEnabledTrue(String affiliateNumber);

    Page <Patient> findAllByEnabledTrue(Pageable pageable);

    Optional<Patient> findById(Long id);

}
