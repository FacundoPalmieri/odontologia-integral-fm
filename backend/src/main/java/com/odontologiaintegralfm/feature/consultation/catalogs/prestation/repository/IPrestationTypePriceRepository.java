package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.repository;


import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationTypePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IPrestationTypePriceRepository extends JpaRepository<PrestationTypePrice, Long> {

    @Query("""
            SELECT ptp
            FROM PrestationTypePrice ptp
            WHERE ptp.prestationType.id = :prestationTypeId
            AND ptp.startDate <= CURRENT_DATE
            AND ptp.endDate IS NULL
            AND ptp.enabled = true
            """)
    Optional<PrestationTypePrice> currentPrice(@Param("prestationTypeId") Long prestationTypeId);

    @Query("""
            SELECT DISTINCT ptp
            FROM PrestationTypePrice ptp
            JOIN FETCH ptp.prestationType pt
            JOIN FETCH pt.allowedScopes
            WHERE ptp.startDate <= :today
            AND ptp.endDate IS NULL
            AND ptp.enabled = true
            AND pt.enabled = true
            """)
    List<PrestationTypePrice> findAllActive(@Param("today") LocalDate today);

}
