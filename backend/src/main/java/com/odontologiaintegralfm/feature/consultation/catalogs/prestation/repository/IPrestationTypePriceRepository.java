package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.repository;


import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationTypePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
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


}
