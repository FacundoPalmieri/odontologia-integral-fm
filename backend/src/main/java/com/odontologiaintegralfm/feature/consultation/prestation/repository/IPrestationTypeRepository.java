package com.odontologiaintegralfm.feature.consultation.prestation.repository;

import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
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

    /**
     * Rama 1 del árbol clínico (REQ-039, D-01, D-03): un par (id de tratamiento, prestación) por cada tratamiento pedido,
     * con las prestaciones habilitadas y creadas hasta {@code date} incluida (se compara el día de {@code createdAt}).
     */
    @Query("""
     SELECT new com.odontologiaintegralfm.feature.consultation.prestation.repository.TreatmentPrestationTypeLink(tr.id, pt)
     FROM PrestationType pt
     JOIN pt.treatments tr
     WHERE tr.id IN :treatmentIds
     AND pt.enabled = true
     AND CAST(pt.createdAt AS LocalDate) <= :date
     """)
    List<TreatmentPrestationTypeLink> findTreatmentLinks(@Param("treatmentIds") Collection<Long> treatmentIds,
                                                         @Param("date") LocalDate date);

    /**
     * Rama 2 del árbol clínico (REQ-039, D-01, D-02, T-05): prestaciones habilitadas, sin ninguna fila en la tabla
     * intermedia con tratamientos, con la especialidad pedida entre las suyas y creadas hasta {@code date} incluida.
     */
    @Query("""
     SELECT pt
     FROM PrestationType pt
     WHERE pt.enabled = true
     AND pt.treatments IS EMPTY
     AND CAST(pt.createdAt AS LocalDate) <= :date
     AND EXISTS (SELECT 1 FROM pt.dentistSpecialties ds WHERE ds.id = :specialtyId)
     """)
    List<PrestationType> findWithoutTreatmentBySpecialty(@Param("specialtyId") Long specialtyId,
                                                         @Param("date") LocalDate date);

    /** Carga los {@code allowedScopes} de un lote de prestaciones; una prestación sin scopes sale igual (REQ-039, T-02). */
    @Query("""
     SELECT DISTINCT pt
     FROM PrestationType pt
     LEFT JOIN FETCH pt.allowedScopes
     WHERE pt.id IN :ids
     """)
    List<PrestationType> findAllWithAllowedScopesByIdIn(@Param("ids") Collection<Long> ids);
}
