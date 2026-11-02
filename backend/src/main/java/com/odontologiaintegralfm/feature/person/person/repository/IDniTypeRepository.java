package com.odontologiaintegralfm.feature.person.person.repository;

import com.odontologiaintegralfm.feature.person.person.model.DniType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface IDniTypeRepository extends JpaRepository<DniType, Long> {

    Set<DniType> findAllByEnabledTrue();

    Optional<DniType> findByIdAndEnabledTrue(Long id);
}
