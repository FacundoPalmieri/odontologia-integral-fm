package com.odontologiaintegralfm.feature.consultation.catalogs.repository;

import com.odontologiaintegralfm.feature.consultation.catalogs.model.Treatment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ITreatmentRepository extends JpaRepository<Treatment, Long> {

    @EntityGraph(attributePaths = "condition")
    Page<Treatment> findAllByEnabledTrue(Pageable pageable);

}
