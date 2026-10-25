package com.odontologiaintegralfm.feature.consultation.prestation.repository;

import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPrestationTypeRepository extends JpaRepository<PrestationType, Long> {

    /**
     * Ids de los {@code Treatment} relacionados con alguna prestación habilitada de arreglo de prótesis
     * ({@code requiresOriginTreatment = true}), sin repetir (D-02, D-07).
     */
    @Query("""
     SELECT DISTINCT tr.id
     FROM PrestationType pt
     JOIN pt.treatments tr
     WHERE pt.requiresOriginTreatment = true
     AND pt.enabled = true
     """)
    List<Long> findTreatmentIdsLinkedToOriginRepairs();
}