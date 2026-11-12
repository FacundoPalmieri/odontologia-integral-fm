package com.odontologiaintegralfm.feature.consultation.prestation.repository;


import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationTypePrice;
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

    /**
     * Precios vigentes a {@code date} de todas las prestaciones habilitadas (REQ-039, D-04, T-06). Sin {@code JOIN FETCH}
     * de {@code allowedScopes}: una prestación sin scopes también sale. El {@code endDate} no se incluye: es el día en que empieza el precio siguiente. Si una prestación trae dos filas, el use case lo rechaza (D-11).
     */
    @Query("""
            SELECT ptp
            FROM PrestationTypePrice ptp
            JOIN FETCH ptp.prestationType pt
            WHERE ptp.startDate <= :date
            AND (ptp.endDate IS NULL OR ptp.endDate > :date)
            AND ptp.enabled = true
            AND pt.enabled = true
            """)
    List<PrestationTypePrice> findAllPricesAtDate(@Param("date") LocalDate date);

}
