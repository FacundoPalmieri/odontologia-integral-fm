package com.odontologiaintegralfm.feature.consultation.core.payment.service;

import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
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
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterPaymentUseCaseTest {

    @Mock private PrestationInstanceQueryService prestationInstanceQueryService;
    @Mock private PaymentDomainService paymentDomainService;
    @Mock private IPaymentRepository paymentRepository;
    @Mock private IPaymentDetailRepository paymentDetailRepository;
    @Mock private PaymentAccountQueryService paymentAccountQueryService;

    @InjectMocks private RegisterPaymentUseCase useCase;

    // ─── Happy path ───────────────────────────────────────────────────────────

    /**
     * CASO: Pago válido con monto parcial.
     * Validación: Captura con ArgumentCaptor el Payment pasado a save() y verifica amount, method y date (no null).
     *             Verifica también que paymentDetailRepository.save() fue invocado con la prestationInstance y el amount del DTO.
     */
    @Test
    void execute_validPayment_persistsPaymentAndDetail() {
        Patient patient = mock(Patient.class);
        Consultation consultation = mock(Consultation.class);
        ConsultationInstance ci = mock(ConsultationInstance.class);
        PrestationInstance pi = mock(PrestationInstance.class);
        when(pi.getId()).thenReturn(1L);
        when(pi.getFinalAmount()).thenReturn(new BigDecimal("1000"));
        when(pi.getConsultationInstance()).thenReturn(ci);
        when(ci.getConsultation()).thenReturn(consultation);
        when(consultation.getPatient()).thenReturn(patient);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(pi);
        when(paymentDetailRepository.sumAmountByPrestationInstanceId(1L)).thenReturn(new BigDecimal("400"));

        Payment savedPayment = mock(Payment.class);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentDetail savedDetail = mock(PaymentDetail.class);
        when(savedDetail.getId()).thenReturn(20L);
        when(paymentDetailRepository.save(any(PaymentDetail.class))).thenReturn(savedDetail);

        PaymentRequestDTO dto = new PaymentRequestDTO(1L, new BigDecimal("200"), PaymentMethods.CASH, null);

        useCase.execute(dto);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment captured = paymentCaptor.getValue();
        assertThat(captured.getTotalAmount()).isEqualByComparingTo(new BigDecimal("200"));
        assertThat(captured.getMethod()).isEqualTo(PaymentMethods.CASH);
        assertThat(captured.getDate()).isNotNull();

        ArgumentCaptor<PaymentDetail> detailCaptor = ArgumentCaptor.forClass(PaymentDetail.class);
        verify(paymentDetailRepository).save(detailCaptor.capture());
        PaymentDetail capturedDetail = detailCaptor.getValue();
        assertThat(capturedDetail.getPrestationInstance()).isEqualTo(pi);
        assertThat(capturedDetail.getAmount()).isEqualByComparingTo(new BigDecimal("200"));
    }

    /**
     * CASO: Pago parcial con finalAmount conocido y monto ya pagado.
     * Regla: remainingDebt = finalAmount − paidSoFar − amount (calculado en memoria).
     * Validación: El campo remainingDebt del PaymentResponseDTO retornado vale 400 (1000 − 400 − 200).
     */
    @Test
    void execute_validPayment_returnsCorrectRemainingDebt() {
        Patient patient = mock(Patient.class);
        Consultation consultation = mock(Consultation.class);
        ConsultationInstance ci = mock(ConsultationInstance.class);
        PrestationInstance pi = mock(PrestationInstance.class);
        when(pi.getId()).thenReturn(1L);
        when(pi.getFinalAmount()).thenReturn(new BigDecimal("1000"));
        when(pi.getConsultationInstance()).thenReturn(ci);
        when(ci.getConsultation()).thenReturn(consultation);
        when(consultation.getPatient()).thenReturn(patient);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(pi);
        when(paymentDetailRepository.sumAmountByPrestationInstanceId(1L)).thenReturn(new BigDecimal("400"));

        Payment savedPayment = mock(Payment.class);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentDetail savedDetail = mock(PaymentDetail.class);
        when(savedDetail.getId()).thenReturn(20L);
        when(paymentDetailRepository.save(any(PaymentDetail.class))).thenReturn(savedDetail);

        PaymentRequestDTO dto = new PaymentRequestDTO(1L, new BigDecimal("200"), PaymentMethods.CASH, null);

        Response<PaymentResponseDTO> result = useCase.execute(dto);

        assertThat(result.data().remainingDebt()).isEqualByComparingTo(new BigDecimal("400"));
    }

    // ─── Caminos de error 400 ─────────────────────────────────────────────────

    /**
     * CASO: Monto igual a cero en el DTO.
     * Regla: El monto debe ser mayor a cero.
     * Validación: Lanza BadRequestException antes de tocar los repositorios.
     */
    @Test
    void execute_zeroAmount_throwsBadRequest() {
        Patient patient = mock(Patient.class);
        Consultation consultation = mock(Consultation.class);
        ConsultationInstance ci = mock(ConsultationInstance.class);
        PrestationInstance pi = mock(PrestationInstance.class);
        when(pi.getConsultationInstance()).thenReturn(ci);
        when(ci.getConsultation()).thenReturn(consultation);
        when(consultation.getPatient()).thenReturn(patient);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(pi);

        doThrow(BadRequestException.class)
                .when(paymentDomainService).validateAmount(eq(BigDecimal.ZERO));

        PaymentRequestDTO dto = new PaymentRequestDTO(1L, BigDecimal.ZERO, PaymentMethods.CASH, null);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(BadRequestException.class);

        verify(paymentRepository, never()).save(any());
        verify(paymentDetailRepository, never()).save(any());
    }

    /**
     * CASO: Monto negativo en el DTO.
     * Regla: El monto debe ser mayor a cero.
     * Validación: Lanza BadRequestException antes de tocar los repositorios.
     */
    @Test
    void execute_negativeAmount_throwsBadRequest() {
        Patient patient = mock(Patient.class);
        Consultation consultation = mock(Consultation.class);
        ConsultationInstance ci = mock(ConsultationInstance.class);
        PrestationInstance pi = mock(PrestationInstance.class);
        when(pi.getConsultationInstance()).thenReturn(ci);
        when(ci.getConsultation()).thenReturn(consultation);
        when(consultation.getPatient()).thenReturn(patient);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(pi);

        doThrow(BadRequestException.class)
                .when(paymentDomainService).validateAmount(eq(new BigDecimal("-100")));

        PaymentRequestDTO dto = new PaymentRequestDTO(1L, new BigDecimal("-100"), PaymentMethods.CASH, null);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(BadRequestException.class);

        verify(paymentRepository, never()).save(any());
        verify(paymentDetailRepository, never()).save(any());
    }

    // ─── Caminos de error 409 ─────────────────────────────────────────────────

    /**
     * CASO: El monto enviado supera la deuda restante.
     * Regla: No se puede pagar más de lo que se debe.
     * Validación: Lanza ConflictException porque amount (300) supera la deuda restante (200 = 500 − 300).
     */
    @Test
    void execute_amountExceedsRemainingDebt_throwsConflict() {
        Patient patient = mock(Patient.class);
        Consultation consultation = mock(Consultation.class);
        ConsultationInstance ci = mock(ConsultationInstance.class);
        PrestationInstance pi = mock(PrestationInstance.class);
        when(pi.getId()).thenReturn(1L);
        when(pi.getFinalAmount()).thenReturn(new BigDecimal("500"));
        when(pi.getConsultationInstance()).thenReturn(ci);
        when(ci.getConsultation()).thenReturn(consultation);
        when(consultation.getPatient()).thenReturn(patient);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(pi);
        when(paymentDetailRepository.sumAmountByPrestationInstanceId(1L)).thenReturn(new BigDecimal("300"));

        doThrow(ConflictException.class)
                .when(paymentDomainService).validateDebt(
                        eq(new BigDecimal("500")),
                        eq(new BigDecimal("300")),
                        eq(new BigDecimal("300")),
                        eq(1L));

        PaymentRequestDTO dto = new PaymentRequestDTO(1L, new BigDecimal("300"), PaymentMethods.CASH, null);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: La prestación ya está totalmente abonada.
     * Regla: No se puede registrar un pago en una prestación sin deuda pendiente.
     * Validación: Lanza ConflictException porque paidSoFar (500) = finalAmount (500).
     */
    @Test
    void execute_fullyPaidPrestation_throwsConflict() {
        Patient patient = mock(Patient.class);
        Consultation consultation = mock(Consultation.class);
        ConsultationInstance ci = mock(ConsultationInstance.class);
        PrestationInstance pi = mock(PrestationInstance.class);
        when(pi.getId()).thenReturn(1L);
        when(pi.getFinalAmount()).thenReturn(new BigDecimal("500"));
        when(pi.getConsultationInstance()).thenReturn(ci);
        when(ci.getConsultation()).thenReturn(consultation);
        when(consultation.getPatient()).thenReturn(patient);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(pi);
        when(paymentDetailRepository.sumAmountByPrestationInstanceId(1L)).thenReturn(new BigDecimal("500"));

        doThrow(ConflictException.class)
                .when(paymentDomainService).validateDebt(
                        eq(new BigDecimal("500")),
                        eq(new BigDecimal("500")),
                        any(BigDecimal.class),
                        eq(1L));

        PaymentRequestDTO dto = new PaymentRequestDTO(1L, new BigDecimal("100"), PaymentMethods.CASH, null);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Ya existe un pago con la misma prestationInstanceId + amount en los últimos 5 minutos.
     * Regla: Idempotencia — no se registra el mismo pago dos veces en ventana corta.
     * Validación: Lanza ConflictException por pago duplicado detectado.
     */
    @Test
    void execute_duplicatePaymentWithinFiveMinutes_throwsConflict() {
        Patient patient = mock(Patient.class);
        Consultation consultation = mock(Consultation.class);
        ConsultationInstance ci = mock(ConsultationInstance.class);
        PrestationInstance pi = mock(PrestationInstance.class);
        when(pi.getId()).thenReturn(1L);
        when(pi.getFinalAmount()).thenReturn(new BigDecimal("1000"));
        when(pi.getConsultationInstance()).thenReturn(ci);
        when(ci.getConsultation()).thenReturn(consultation);
        when(consultation.getPatient()).thenReturn(patient);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(pi);
        when(paymentDetailRepository.sumAmountByPrestationInstanceId(1L)).thenReturn(new BigDecimal("200"));

        doThrow(ConflictException.class)
                .when(paymentDomainService).validateIdempotency(eq(1L), eq(new BigDecimal("300")));

        PaymentRequestDTO dto = new PaymentRequestDTO(1L, new BigDecimal("300"), PaymentMethods.CASH, null);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(ConflictException.class);
    }

    // ─── Caminos de error 404 ─────────────────────────────────────────────────

    /**
     * CASO: La PrestationInstance solicitada no existe.
     * Validación: La NotFoundException lanzada por PrestationInstanceQueryService se propaga sin ser capturada.
     */
    @Test
    void execute_prestationNotFound_throwsNotFoundException() {
        when(prestationInstanceQueryService.findById(99L))
                .thenThrow(NotFoundException.class);

        PaymentRequestDTO dto = new PaymentRequestDTO(99L, new BigDecimal("100"), PaymentMethods.CASH, null);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(NotFoundException.class);
    }

    // ─── REQ-005: validaciones method/account ────────────────────────────────

    /**
     * CASO: Pago por transferencia con cuenta bancaria válida.
     * Validación: Captura con ArgumentCaptor el Payment pasado a paymentRepository.save() y verifica
     *             que captured.getAccount() es la instancia de PaymentAccount retornada por paymentAccountQueryService,
     *             y que captured.getMethod() es TRANSFER.
     */
    @Test
    void execute_transferPayment_persistsPaymentWithAccount() {
        Patient patient = mock(Patient.class);
        Consultation consultation = mock(Consultation.class);
        ConsultationInstance ci = mock(ConsultationInstance.class);
        PrestationInstance pi = mock(PrestationInstance.class);
        when(pi.getId()).thenReturn(1L);
        when(pi.getFinalAmount()).thenReturn(new BigDecimal("1000"));
        when(pi.getConsultationInstance()).thenReturn(ci);
        when(ci.getConsultation()).thenReturn(consultation);
        when(consultation.getPatient()).thenReturn(patient);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(pi);

        PaymentAccount account = mock(PaymentAccount.class);
        when(paymentAccountQueryService.findById(10L)).thenReturn(account);

        when(paymentDetailRepository.sumAmountByPrestationInstanceId(1L)).thenReturn(new BigDecimal("400"));

        Payment savedPayment = mock(Payment.class);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        PaymentDetail savedDetail = mock(PaymentDetail.class);
        when(savedDetail.getId()).thenReturn(20L);
        when(paymentDetailRepository.save(any(PaymentDetail.class))).thenReturn(savedDetail);

        PaymentRequestDTO dto = new PaymentRequestDTO(1L, new BigDecimal("200"), PaymentMethods.TRANSFER, 10L);

        useCase.execute(dto);

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        Payment captured = captor.getValue();
        assertThat(captured.getAccount()).isSameAs(account);
        assertThat(captured.getMethod()).isEqualTo(PaymentMethods.TRANSFER);
    }

    /**
     * CASO: Pago en efectivo con cuenta bancaria enviada en el DTO.
     * Regla: CASH no acepta cuenta bancaria.
     * Validación: Lanza BadRequestException antes de consultar paymentAccountQueryService y antes de tocar repositorios.
     */
    @Test
    void execute_cashWithAccount_throwsBadRequest() {
        Patient patient = mock(Patient.class);
        Consultation consultation = mock(Consultation.class);
        ConsultationInstance ci = mock(ConsultationInstance.class);
        PrestationInstance pi = mock(PrestationInstance.class);
        when(pi.getConsultationInstance()).thenReturn(ci);
        when(ci.getConsultation()).thenReturn(consultation);
        when(consultation.getPatient()).thenReturn(patient);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(pi);

        PaymentRequestDTO dto = new PaymentRequestDTO(1L, new BigDecimal("200"), PaymentMethods.CASH, 10L);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(BadRequestException.class);

        verify(paymentAccountQueryService, never()).findById(any());
        verify(paymentRepository, never()).save(any());
        verify(paymentDetailRepository, never()).save(any());
    }

    /**
     * CASO: Pago por transferencia sin cuenta bancaria en el DTO.
     * Regla: TRANSFER requiere cuenta bancaria.
     * Validación: Lanza BadRequestException antes de consultar paymentAccountQueryService y antes de tocar repositorios.
     */
    @Test
    void execute_transferWithoutAccount_throwsBadRequest() {
        Patient patient = mock(Patient.class);
        Consultation consultation = mock(Consultation.class);
        ConsultationInstance ci = mock(ConsultationInstance.class);
        PrestationInstance pi = mock(PrestationInstance.class);
        when(pi.getConsultationInstance()).thenReturn(ci);
        when(ci.getConsultation()).thenReturn(consultation);
        when(consultation.getPatient()).thenReturn(patient);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(pi);

        PaymentRequestDTO dto = new PaymentRequestDTO(1L, new BigDecimal("200"), PaymentMethods.TRANSFER, null);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(BadRequestException.class);

        verify(paymentAccountQueryService, never()).findById(any());
        verify(paymentRepository, never()).save(any());
        verify(paymentDetailRepository, never()).save(any());
    }

    /**
     * CASO: Pago por transferencia con ID de cuenta deshabilitada o inexistente.
     * Validación: La NotFoundException lanzada por paymentAccountQueryService.findById() se propaga sin ser
     *             capturada por el use case; paymentRepository.save() nunca es invocado.
     */
    @Test
    void execute_accountDisabled_throwsNotFoundException() {
        Patient patient = mock(Patient.class);
        Consultation consultation = mock(Consultation.class);
        ConsultationInstance ci = mock(ConsultationInstance.class);
        PrestationInstance pi = mock(PrestationInstance.class);
        when(pi.getConsultationInstance()).thenReturn(ci);
        when(ci.getConsultation()).thenReturn(consultation);
        when(consultation.getPatient()).thenReturn(patient);
        when(prestationInstanceQueryService.findById(1L)).thenReturn(pi);
        when(paymentAccountQueryService.findById(99L)).thenThrow(NotFoundException.class);

        PaymentRequestDTO dto = new PaymentRequestDTO(1L, new BigDecimal("200"), PaymentMethods.TRANSFER, 99L);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(NotFoundException.class);

        verify(paymentRepository, never()).save(any());
    }
}
