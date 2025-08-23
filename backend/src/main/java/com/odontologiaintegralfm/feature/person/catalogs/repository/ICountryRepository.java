package com.odontologiaintegralfm.feature.person.catalogs.repository;

import com.odontologiaintegralfm.feature.person.catalogs.model.Country;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface ICountryRepository extends JpaRepository<Country, Long> {

    List<Country> findAllByEnabledTrue();

    Optional<Country> findByIdAndEnabledTrue(Long id);

}
