package com.odontologiaintegralfm.repository;

import com.odontologiaintegralfm.model.Locality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author [Facundo Palmieri]
 */
@Repository
public interface ILocalityRepository extends JpaRepository<Locality, Long> {

    List<Locality> findAllByProvinceIdAndEnabledTrue(Long provinceId);

    Optional<Locality> findByIdAndEnabledTrue(Long id);
}
