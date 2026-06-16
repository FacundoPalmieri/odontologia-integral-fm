package com.odontologiaintegralfm.feature.consultation.core.payment.dto;

import java.math.BigDecimal;

public record PaymentResponseDTO(
        Long paymentDetailId,
        Long prestationInstanceId,
        BigDecimal remainingDebt
) {
}
