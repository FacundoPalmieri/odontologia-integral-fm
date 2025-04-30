package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.TelephoneType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ITelephoneTypeRepository extends JpaRepository<TelephoneType, Long> {

    Optional<TelephoneType> findByIdAndEnabledTrue(Long id);
}
