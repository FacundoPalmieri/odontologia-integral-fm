package com.odontologiaintegralfm.feature.consultation.core.payment.service;

import com.odontologiaintegralfm.feature.consultation.core.payment.repository.IPaymentDetailRepository;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentDomainService {

    private final IPaymentDetailRepository paymentDetailRepository;

    PaymentDomainService(IPaymentDetailRepository paymentDetailRepository) {
        this.paymentDetailRepository = paymentDetailRepository;
    }

    public void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "exception.paymentDomainService.validateAmount.user", null,
                    "exception.paymentDomainService.validateAmount.log",
                    new Object[]{amount, "PaymentDomainService", "validateAmount"},
                    LogLevel.ERROR);
        }
    }

    public void validateDebt(BigDecimal finalAmount, BigDecimal paidSoFar, BigDecimal amount, Long prestationInstanceId) {
        BigDecimal remaining = finalAmount.subtract(paidSoFar);

        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ConflictException(
                    "exception.paymentDomainService.validateDebt.fullyPaid.user", null,
                    "exception.paymentDomainService.validateDebt.fullyPaid.log",
                    new Object[]{prestationInstanceId, "PaymentDomainService", "validateDebt"},
                    LogLevel.ERROR);
        }

        if (amount.compareTo(remaining) > 0) {
            throw new ConflictException(
                    "exception.paymentDomainService.validateDebt.exceedsDebt.user", null,
                    "exception.paymentDomainService.validateDebt.exceedsDebt.log",
                    new Object[]{remaining, amount, "PaymentDomainService", "validateDebt"},
                    LogLevel.ERROR);
        }
    }

    public void validateIdempotency(Long prestationInstanceId, BigDecimal amount) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        boolean duplicate = paymentDetailRepository
                .existsByPrestationInstanceIdAndAmountAndCreatedAtAfter(prestationInstanceId, amount, threshold);

        if (duplicate) {
            throw new ConflictException(
                    "exception.paymentDomainService.validateIdempotency.user", null,
                    "exception.paymentDomainService.validateIdempotency.log",
                    new Object[]{prestationInstanceId, amount, "PaymentDomainService", "validateIdempotency"},
                    LogLevel.ERROR);
        }
    }
}