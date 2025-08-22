package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface IProvinceRepository extends JpaRepository<Province, Long> {

    List<Province> findAllByCountryIdAndEnabledTrue(Long countryId);

    Optional<Province> findByIdAndEnabledTrue(Long Id);
}
