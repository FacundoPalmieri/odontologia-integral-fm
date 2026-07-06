package com.odontologiaintegralfm.feature.consultation.core.payment.repository;

import com.odontologiaintegralfm.feature.consultation.core.payment.model.PaymentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface IPaymentDetailRepository extends JpaRepository<PaymentDetail, Long> {

    @Query("""
            SELECT COALESCE(SUM(pd.amount), 0)
            FROM PaymentDetail pd
            WHERE pd.prestationInstance.id = :prestationInstanceId
    """)
    BigDecimal sumAmountByPrestationInstanceId(@Param("prestationInstanceId") Long prestationInstanceId);

    boolean existsByPrestationInstanceIdAndAmountAndCreatedAtAfter(Long prestationInstanceId, BigDecimal amount, LocalDateTime threshold);
}
