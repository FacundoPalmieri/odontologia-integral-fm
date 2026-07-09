package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.repository;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("SELECT p FROM Promotion p WHERE p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findAllActive(@Param("today") LocalDate today);

    boolean existsByName(String name);

    /**
     * Busca una promoción por id sin aplicar el filtro {@code @Where(enabled = true)} de la entidad.
     * Necesario para poder encontrar promociones deshabilitadas y reactivarlas (ver ADR-0020).
     */
    @Query(value = "SELECT * FROM promotions WHERE id = :id", nativeQuery = true)
    Optional<Promotion> findByIdNative(@Param("id") Long id);
}
