package com.odontologiaintegralfm.feature.dentist.catalogs.repository;

import com.odontologiaintegralfm.feature.dentist.catalogs.model.DentistSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDentistSpecialtyRepository extends JpaRepository<DentistSpecialty, Long> {
    Optional<DentistSpecialty> findByIdAndEnabledTrue(Long id);

    List<DentistSpecialty> findAllByEnabledTrue();
}
