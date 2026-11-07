package com.odontologiaintegralfm.feature.consultation.prestation.repository;

import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface IPrestationStepRepository extends JpaRepository<PrestationStep, Long> {

    List<PrestationStep> findByPrestationId(Long presstationId);

    /** Catálogo de steps de varios tipos de prestación en una consulta, con el {@code Step} y el tipo ya cargados (REQ-027, T-04). */
    @Query("""
     SELECT ps
     FROM PrestationStep ps
     JOIN FETCH ps.step
     JOIN FETCH ps.prestation
     WHERE ps.prestation.id IN :typeIds
     """)
    List<PrestationStep> findByPrestationIdInFetchStep(@Param("typeIds") Collection<Long> typeIds);
}