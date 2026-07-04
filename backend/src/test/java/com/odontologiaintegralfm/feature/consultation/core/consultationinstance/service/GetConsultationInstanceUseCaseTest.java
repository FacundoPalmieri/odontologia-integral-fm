package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.authentication.enums.Role;
import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.mapper.ConsultationMapper;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto.ConsultationInstanceResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.mapper.OdontogramMapper;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.repository.IOdontogramRepository;
import com.odontologiaintegralfm.feature.consultation.core.payment.repository.IPaymentDetailRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.mapper.PrestationInstanceMapper;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.person.core.model.Person;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.exception.ForbiddenException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class GetConsultationInstanceUseCaseTest {

    @Mock private ConsultationInstanceQueryService consultationInstanceQueryService;
    @Mock private IPrestationInstanceRepository prestationInstanceRepository;
    @Mock private IPaymentDetailRepository paymentDetailRepository;
    @Mock private IOdontogramRepository odontogramRepository;
    @Mock private PrestationInstanceMapper prestationInstanceMapper;
    @Mock private OdontogramMapper odontogramMapper;
    @Mock private ConsultationMapper consultationMapper;
    @Mock private AuthenticatedUserService authenticatedUserService;
    @Mock private MessageSource messageSource;

    @InjectMocks private GetConsultationInstanceUseCase useCase;

    /**
     * CASO: La instancia tiene una PrestationInstance con pagos imputados parciales.
     * Regla: pendingAmount = finalAmount − suma(PaymentDetail.amount) para esa prestación.
     * Validación: Que el DTO devuelve pendingAmount con el cálculo correcto.
     */
    @Test
    void getConsultationInstance_withPayments_calculatesPendingAmountCorrectly() {
        ConsultationInstance instance = mock(ConsultationInstance.class);
        UserSec user = mock(UserSec.class);

        PrestationInstance prestation = mock(PrestationInstance.class);
        when(prestation.getId()).thenReturn(50L);
        when(prestation.getFinalAmount()).thenReturn(new BigDecimal("1000.00"));

        when(instance.getId()).thenReturn(1L);
        when(consultationInstanceQueryService.findById(1L)).thenReturn(instance);
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(user);
        when(user.getRolesList()).thenReturn(Set.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(1L)).thenReturn(List.of(prestation));
        when(paymentDetailRepository.sumAmountByPrestationInstanceId(50L)).thenReturn(new BigDecimal("300.00"));
        when(odontogramRepository.findByConsultationInstanceId(1L)).thenReturn(List.of());
        when(instance.getConsultation()).thenReturn(mock(Consultation.class));
        when(consultationMapper.toDTO(any(Consultation.class))).thenReturn(mock(ConsultationResponseDTO.class));
        PrestationInstanceResponseDTO baseDTO = new PrestationInstanceResponseDTO(
                50L, null, null, null, null, null, null, null, null, null, null, null,
                new BigDecimal("1000.00"), null);
        when(prestationInstanceMapper.toDTO(prestation)).thenReturn(baseDTO);

        Response<ConsultationInstanceResponseDTO> result = useCase.execute(1L);

        PrestationInstanceResponseDTO prestationDTO = result.data().prestationInstance().get(0);
        assertThat(prestationDTO.pendingAmount())
                .isEqualByComparingTo(new BigDecimal("700.00"));
        verify(paymentDetailRepository).sumAmountByPrestationInstanceId(50L);
    }

    /**
     * CASO: Se solicita una ConsultationInstance existente y habilitada.
     * Regla: enabled=true es filtrado por @Where en el repo; si existe se devuelve el DTO.
     * Validación: Que el use case retorna el DTO sin lanzar excepción.
     */
    @Test
    void getConsultationInstance_whenItExistsEnabledTrue_returnOK() {
        ConsultationInstance instance = mock(ConsultationInstance.class);
        UserSec user = mock(UserSec.class);

        when(instance.getId()).thenReturn(1L);
        when(consultationInstanceQueryService.findById(1L)).thenReturn(instance);
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(user);
        when(user.getRolesList()).thenReturn(Set.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(1L)).thenReturn(List.of());
        when(odontogramRepository.findByConsultationInstanceId(1L)).thenReturn(List.of());
        when(instance.getConsultation()).thenReturn(mock(Consultation.class));
        when(consultationMapper.toDTO(any(Consultation.class))).thenReturn(mock(ConsultationResponseDTO.class));

        Response<ConsultationInstanceResponseDTO> result = useCase.execute(1L);

        assertThat(result.success()).isTrue();
    }

    /**
     * CASO: Se solicita una ConsultationInstance deshabilitada o inexistente.
     * Regla: @Where(enabled=true) filtra las deshabilitadas; el repo devuelve Optional.empty().
     * Validación: Que se lanza NotFoundException.
     */
    @Test
    void getConsultationInstance_whenItExistsEnabledFalse_return404() {
        when(consultationInstanceQueryService.findById(99L)).thenThrow(NotFoundException.class);

        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(NotFoundException.class);
    }

    /**
     * CASO: Un odontólogo solicita ver la instancia de consulta de otro odontólogo.
     * Regla: Un odontólogo solo puede ver sus propias instancias.
     * Validación: Que se lanza ForbiddenException.
     */
    @Test
    void getConsultationInstance_fromAnotherDentist_return403() {
        ConsultationInstance instance = mock(ConsultationInstance.class);
        Consultation consultation = mock(Consultation.class);
        Dentist dentist = mock(Dentist.class);
        Person personDentist = mock(Person.class);
        UserSec user = mock(UserSec.class);
        Person personUser = mock(Person.class);
        com.odontologiaintegralfm.feature.authentication.model.Role roleDentist =
                mock(com.odontologiaintegralfm.feature.authentication.model.Role.class);

        when(personDentist.getId()).thenReturn(10L);
        when(dentist.getPerson()).thenReturn(personDentist);
        when(consultation.getDentist()).thenReturn(dentist);
        when(instance.getConsultation()).thenReturn(consultation);
        when(consultationInstanceQueryService.findById(1L)).thenReturn(instance);

        when(personUser.getId()).thenReturn(99L);
        when(user.getPerson()).thenReturn(personUser);
        when(roleDentist.getName()).thenReturn(Role.DENTIST.name());
        when(user.getRolesList()).thenReturn(Set.of(roleDentist));
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(user);

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(ForbiddenException.class);
    }

    /**
     * CASO: La instancia tiene múltiples prestaciones con distintos finalAmount.
     * Regla: totalFinalAmount = suma de todos los finalAmount de las PrestationInstance.
     * Validación: Que totalFinalAmount del response = suma exacta de los finalAmount.
     */
    @Test
    void getConsultationInstance_calculatesTotalFinalAmount_correctly() {
        ConsultationInstance instance = mock(ConsultationInstance.class);
        UserSec user = mock(UserSec.class);

        PrestationInstance p1 = mock(PrestationInstance.class);
        PrestationInstance p2 = mock(PrestationInstance.class);
        when(p1.getFinalAmount()).thenReturn(new BigDecimal("1000.00"));
        when(p2.getFinalAmount()).thenReturn(new BigDecimal("500.00"));
        when(p1.getId()).thenReturn(1L);
        when(p2.getId()).thenReturn(2L);

        when(instance.getId()).thenReturn(1L);
        when(consultationInstanceQueryService.findById(1L)).thenReturn(instance);
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(user);
        when(user.getRolesList()).thenReturn(Set.of());
        when(prestationInstanceRepository.findByConsultationInstanceId(1L)).thenReturn(List.of(p1, p2));
        when(paymentDetailRepository.sumAmountByPrestationInstanceId(1L)).thenReturn(BigDecimal.ZERO);
        when(paymentDetailRepository.sumAmountByPrestationInstanceId(2L)).thenReturn(BigDecimal.ZERO);
        when(odontogramRepository.findByConsultationInstanceId(1L)).thenReturn(List.of());
        when(instance.getConsultation()).thenReturn(mock(Consultation.class));
        when(consultationMapper.toDTO(any(Consultation.class))).thenReturn(mock(ConsultationResponseDTO.class));
        PrestationInstanceResponseDTO dto1 = new PrestationInstanceResponseDTO(
                1L, null, null, null, null, null, null, null, null, null, null, null,
                new BigDecimal("1000.00"), null);
        PrestationInstanceResponseDTO dto2 = new PrestationInstanceResponseDTO(
                2L, null, null, null, null, null, null, null, null, null, null, null,
                new BigDecimal("500.00"), null);
        when(prestationInstanceMapper.toDTO(p1)).thenReturn(dto1);
        when(prestationInstanceMapper.toDTO(p2)).thenReturn(dto2);

        Response<ConsultationInstanceResponseDTO> result = useCase.execute(1L);

        assertThat(result.data().totalFinalAmount())
                .isEqualByComparingTo(new BigDecimal("1500.00"));
    }
}