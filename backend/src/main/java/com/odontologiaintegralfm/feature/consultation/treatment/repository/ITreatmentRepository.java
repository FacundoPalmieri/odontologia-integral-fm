package com.odontologiaintegralfm.feature.consultation.treatment.repository;

import com.odontologiaintegralfm.feature.consultation.treatment.model.Treatment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ITreatmentRepository extends JpaRepository<Treatment, Long> {

    @EntityGraph(attributePaths = "conditions")
    Page<Treatment> findAllByEnabledTrue(Pageable pageable);

    List<Treatment> findAll();

    /**
     * Tratamientos habilitados que tienen la especialidad pedida entre las suyas, con sus {@code conditions} cargadas
     * para que el mapper no dispare una consulta por tratamiento (REQ-039, D-02, D-12, T-02).
     */
    @Query("""
     SELECT DISTINCT t
     FROM Treatment t
     LEFT JOIN FETCH t.conditions
     WHERE t.enabled = true
     AND EXISTS (SELECT 1 FROM t.dentistSpecialties ds WHERE ds.id = :specialtyId)
     """)
    List<Treatment> findAllBySpecialtyWithConditions(@Param("specialtyId") Long specialtyId);

}
