package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.DentistSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IDentistSpecialtyRepository extends JpaRepository<DentistSpecialty, Long> {
    Optional<DentistSpecialty> findByIdAndEnabledTrue(Long id);
}
