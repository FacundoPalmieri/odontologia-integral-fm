package com.odontologiaintegralfm.feature.dentist.core.repository;

import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface IDentistRepository extends JpaRepository<Dentist, Long> {

    Optional<Dentist> findByLicenseNumberAndEnabledTrue(String licenseNumber);

    Set<Dentist> findAllByEnabledTrue();
}
