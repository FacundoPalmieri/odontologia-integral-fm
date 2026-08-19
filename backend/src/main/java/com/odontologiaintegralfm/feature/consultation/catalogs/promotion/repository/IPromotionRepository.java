package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.repository;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface IPromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("SELECT p FROM Promotion p WHERE p.startDate <= :today AND p.endDate >= :today")
    List<Promotion> findAllActive(@Param("today") LocalDate today);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
