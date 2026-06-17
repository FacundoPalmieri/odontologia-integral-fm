package com.odontologiaintegralfm.feature.consultation.core.payment.dto;

import com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.enums.PaymentMethods;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentRequestDTO(

        @NotNull(message = "paymentRequestDTO.prestationInstanceId.empty")
        Long prestationInstanceId,

        @NotNull(message = "paymentRequestDTO.amount.empty")
        @Positive(message = "paymentRequestDTO.amount.notPositive")
        BigDecimal amount,

        @NotNull(message = "paymentRequestDTO.method.empty")
        PaymentMethods method,

        Long account
) {
}
