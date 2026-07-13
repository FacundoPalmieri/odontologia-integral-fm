package com.odontologiaintegralfm.feature.consultation.core.consultation.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationEventType;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationStatusType;
import com.odontologiaintegralfm.feature.consultation.core.consultation.mapper.ConsultationMapper;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.ConsultationEvent;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationRepository;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.infrastructure.websocket.enums.WebSocketEventType;
import com.odontologiaintegralfm.infrastructure.websocket.service.IWebSocketEventPublisher;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class DesactivateConsultationUseCaseTest {

    @Mock private IConsultationRepository consultationRepository;
    @Mock private ConsultationMapper consultationMapper;
    @Mock private AuthenticatedUserService authenticatedUserService;
    @Mock private MessageSource messageSource;
    @Mock private ConsultationEventService consultationEventService;
    @Mock private IWebSocketEventPublisher webSocketEventPublisher;

    @InjectMocks private DesactivateConsultationUseCase useCase;

    @Test
    void execute_whenConsultationNotFound_throwsNotFoundException() {
        when(consultationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(1L, "observacion"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void execute_whenStatusIsNotWaitingRoom_throwsConflictException() {
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

        assertThatThrownBy(() -> useCase.execute(1L, "observacion"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void execute_whenStatusIsWaitingRoom_disablesAndSavesConsultation() {
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.WAITING_ROOM);
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

        UserSec user = mock(UserSec.class);
        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(user);
        when(consultationMapper.toDTO(consultation)).thenReturn(
                new ConsultationResponseDTO(1L, null, null, null, "Paciente", null, "Dentista", "Sala de Espera", null));
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("ok");

        useCase.execute(1L, "observacion");

        verify(consultation).disable(user);
        verify(consultationRepository).save(consultation);
    }

    @Test
    void execute_whenStatusIsWaitingRoom_createsConsultationCanceledEventWithObservation() {
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.WAITING_ROOM);
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(mock(UserSec.class));
        when(consultationMapper.toDTO(consultation)).thenReturn(
                new ConsultationResponseDTO(1L, null, null, null, "Paciente",  null,"Dentista", "Sala de Espera", null));
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("ok");

        useCase.execute(1L, "Paciente no se presentó");

        ArgumentCaptor<ConsultationEvent> captor = ArgumentCaptor.forClass(ConsultationEvent.class);
        verify(consultationEventService).create(captor.capture());
        ConsultationEvent published = captor.getValue();
        assertThat(published.getEventType()).isEqualTo(ConsultationEventType.CONSULTATION_CANCELED);
        assertThat(published.getObservation()).isEqualTo("Paciente no se presentó");
        assertThat(published.getConsultation()).isSameAs(consultation);
    }

    @Test
    void execute_whenStatusIsWaitingRoom_publishesConsultationRemovedEvent() {
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.WAITING_ROOM);
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

        when(authenticatedUserService.getAuthenticatedUser()).thenReturn(mock(UserSec.class));
        ConsultationResponseDTO dto = new ConsultationResponseDTO(1L, null, null, null, "Paciente", null,"Dentista", "Sala de Espera", null);
        when(consultationMapper.toDTO(consultation)).thenReturn(dto);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("ok");

        useCase.execute(1L, "observacion");

        verify(webSocketEventPublisher).publish(eq(WebSocketEventType.CONSULTATION_REMOVED), eq(dto));
    }
}
