package com.odontologiaintegralfm.feature.person.catalogs.repository;

import com.odontologiaintegralfm.feature.person.catalogs.model.PhoneType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface IPhoneTypeRepository extends JpaRepository<PhoneType, Long> {

    Optional<PhoneType> findByIdAndEnabledTrue(Long id);

    Set<PhoneType> findAllByEnabledTrue();
}
