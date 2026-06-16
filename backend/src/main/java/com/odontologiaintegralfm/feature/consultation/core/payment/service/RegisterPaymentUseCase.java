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
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class RegisterPaymentUseCase {

    private final PrestationInstanceQueryService prestationInstanceQueryService;
    private final PaymentDomainService paymentDomainService;
    private final IPaymentRepository paymentRepository;
    private final IPaymentDetailRepository paymentDetailRepository;

    RegisterPaymentUseCase(PrestationInstanceQueryService prestationInstanceQueryService,
                           PaymentDomainService paymentDomainService,
                           IPaymentRepository paymentRepository,
                           IPaymentDetailRepository paymentDetailRepository) {
        this.prestationInstanceQueryService = prestationInstanceQueryService;
        this.paymentDomainService = paymentDomainService;
        this.paymentRepository = paymentRepository;
        this.paymentDetailRepository = paymentDetailRepository;
    }

    @LogAction(
            value = "registerPaymentUseCase.logAction",
            args = {"#result.data.prestationInstanceId", "#dto.amount"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    @Transactional
    public Response<PaymentResponseDTO> execute(PaymentRequestDTO dto) {
        PrestationInstance pi = prestationInstanceQueryService.findById(dto.prestationInstanceId());
        Patient patient = pi.getConsultationInstance().getConsultation().getPatient();

        paymentDomainService.validateAmount(dto.amount());

        BigDecimal paidSoFar = paymentDetailRepository.sumAmountByPrestationInstanceId(pi.getId());

        paymentDomainService.validateDebt(pi.getFinalAmount(), paidSoFar, dto.amount(), pi.getId());
        paymentDomainService.validateIdempotency(pi.getId(), dto.amount());

        Payment payment = paymentRepository.save(
                Payment.build(patient, LocalDateTime.now(), dto.amount(), dto.method()));

        PaymentDetail detail = paymentDetailRepository.save(
                PaymentDetail.build(payment, pi, dto.amount()));

        BigDecimal remainingDebt = pi.getFinalAmount().subtract(paidSoFar).subtract(dto.amount());

        return new Response<>(true,
                "registerPaymentUseCase.execute.ok",
                new PaymentResponseDTO(detail.getId(), pi.getId(), remainingDebt));
    }
}
