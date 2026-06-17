package com.odontologiaintegralfm.feature.consultation.core.payment.service;

import com.odontologiaintegralfm.feature.consultation.core.payment.dto.PaymentRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.payment.dto.PaymentResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.payment.model.Payment;
import com.odontologiaintegralfm.feature.consultation.core.payment.model.PaymentDetail;
import com.odontologiaintegralfm.feature.consultation.core.payment.repository.IPaymentDetailRepository;
import com.odontologiaintegralfm.feature.consultation.core.payment.repository.IPaymentRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestation.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestation.service.PrestationInstanceQueryService;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
import com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.enums.PaymentMethods;
import com.odontologiaintegralfm.feature.payment.core.paymentaccount.model.PaymentAccount;
import com.odontologiaintegralfm.feature.payment.core.paymentaccount.service.PaymentAccountQueryService;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class RegisterPaymentUseCase {

    private final PrestationInstanceQueryService prestationInstanceQueryService;
    private final PaymentAccountQueryService paymentAccountQueryService;
    private final PaymentDomainService paymentDomainService;
    private final IPaymentRepository paymentRepository;
    private final IPaymentDetailRepository paymentDetailRepository;

    RegisterPaymentUseCase(PrestationInstanceQueryService prestationInstanceQueryService,
                           PaymentAccountQueryService paymentAccountQueryService,
                           PaymentDomainService paymentDomainService,
                           IPaymentRepository paymentRepository,
                           IPaymentDetailRepository paymentDetailRepository) {
        this.prestationInstanceQueryService = prestationInstanceQueryService;
        this.paymentAccountQueryService = paymentAccountQueryService;
        this.paymentDomainService = paymentDomainService;
        this.paymentRepository = paymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
    }

    @LogAction(
            value = "registerPaymentUseCase.logAction",
            args = {"#result.data.prestationInstanceId", "#dto.amount", "#dto.account"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    @Transactional
    public Response<PaymentResponseDTO> execute(PaymentRequestDTO dto) {
        PrestationInstance pi = prestationInstanceQueryService.findById(dto.prestationInstanceId());
        Patient patient = pi.getConsultationInstance().getConsultation().getPatient();

        if (dto.method() == PaymentMethods.CASH && dto.account() != null) {
            throw new BadRequestException("exception.registerPaymentUseCase.cashWithAccount.user", null, "exception.registerPaymentUseCase.cashWithAccount.log", new Object[]{dto.account(), "RegisterPaymentUseCase", "execute"}, LogLevel.WARN);
        }
        if (dto.method() == PaymentMethods.TRANSFER && dto.account() == null) {
            throw new BadRequestException("exception.registerPaymentUseCase.transferWithoutAccount.user", null, "exception.registerPaymentUseCase.transferWithoutAccount.log", new Object[]{"RegisterPaymentUseCase", "execute"}, LogLevel.WARN);
        }

        PaymentAccount account = dto.method() == PaymentMethods.TRANSFER
                ? paymentAccountQueryService.findById(dto.account())
                : null;

        paymentDomainService.validateAmount(dto.amount());

        BigDecimal paidSoFar = paymentDetailRepository.sumAmountByPrestationInstanceId(pi.getId());

        paymentDomainService.validateDebt(pi.getFinalAmount(), paidSoFar, dto.amount(), pi.getId());
        paymentDomainService.validateIdempotency(pi.getId(), dto.amount());

        Payment payment = paymentRepository.save(Payment.build(patient, LocalDateTime.now(), dto.amount(), dto.method(), account));

        PaymentDetail detail = paymentDetailRepository.save(PaymentDetail.build(payment, pi, dto.amount()));

        BigDecimal remainingDebt = pi.getFinalAmount().subtract(paidSoFar).subtract(dto.amount());

        return new Response<>(true,
                "registerPaymentUseCase.execute.ok",
                new PaymentResponseDTO(detail.getId(), pi.getId(), remainingDebt));
    }
}
