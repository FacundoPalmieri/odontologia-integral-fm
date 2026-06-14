package com.odontologiaintegralfm.feature.consultation.core.consultation.service;

import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationStatusType;
import com.odontologiaintegralfm.feature.consultation.core.consultation.mapper.ConsultationMapper;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.ConsultationStatusHistory;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationRepository;
import com.odontologiaintegralfm.infrastructure.websocket.enums.WebSocketEventType;
import com.odontologiaintegralfm.infrastructure.websocket.service.WebSocketEventPublisher;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChangeConsultationStatusUseCaseTest {

    @Mock private IConsultationRepository consultationRepository;
    @Mock private ConsultationStatusHistoryService consultationStatusHistoryService;
    @Mock private WebSocketEventPublisher webSocketEventPublisher;
    @Mock private ConsultationMapper consultationMapper;

    @InjectMocks private ChangeConsultationStatusUseCase useCase;

    /**
     * CASO: Cambio de estado válido desde WAITING_ROOM a IN_CONSULTATION.
     * Validación: Aplica setStatus con el nuevo estado y persiste la consulta.
     */
    @Test
    void execute_whenValidStatusChange_setsNewStatusAndSaves() {
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        when(consultationRepository.save(consultation)).thenReturn(consultation);
        when(consultationMapper.toDTO(consultation)).thenReturn(
                new ConsultationResponseDTO(1L, null, null, null, "Paciente", "Dentista", "En Atención", null));

        useCase.execute(consultation, ConsultationStatusType.IN_CONSULTATION);

        verify(consultation).setStatus(ConsultationStatusType.IN_CONSULTATION);
        verify(consultationRepository).save(consultation);
    }

    /**
     * CASO: Cambio de estado válido — debe registrar el evento en el historial.
     * Validación: Se invoca consultationStatusHistoryService.create con un ConsultationStatusHistory construido a partir de la consulta y el nuevo estado.
     */
    @Test
    void execute_whenValidStatusChange_createsStatusHistory() {
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        when(consultationRepository.save(consultation)).thenReturn(consultation);
        when(consultationMapper.toDTO(consultation)).thenReturn(
                new ConsultationResponseDTO(1L, null, null, null, "Paciente", "Dentista", "En Atención", null));

        useCase.execute(consultation, ConsultationStatusType.IN_CONSULTATION);

        verify(consultationStatusHistoryService).create(any(ConsultationStatusHistory.class));
    }

    /**
     * CASO: Cambio de estado a IN_CONSULTATION — debe publicar evento WebSocket ATTENTION_STARTED.
     * Regla: El evento publicado corresponde al webSocketEvent() del nuevo estado.
     * Validación: Se invoca el publisher con WebSocketEventType.ATTENTION_STARTED y el DTO mapeado.
     */
    @Test
    void execute_whenStatusIsInConsultation_publishesAttentionStartedEvent() {
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        when(consultationRepository.save(consultation)).thenReturn(consultation);
        ConsultationResponseDTO dto =
                new ConsultationResponseDTO(1L, null, null, null, "Paciente", "Dentista", "En Atención", null);
        when(consultationMapper.toDTO(consultation)).thenReturn(dto);

        useCase.execute(consultation, ConsultationStatusType.IN_CONSULTATION);

        verify(webSocketEventPublisher).publish(eq(WebSocketEventType.ATTENTION_STARTED), eq(dto));
    }

    /**
     * CASO: Cambio de estado a WAITING_ROOM — debe publicar PATIENT_RECEIVED.
     * Validación: Se invoca el publisher con WebSocketEventType.PATIENT_RECEIVED.
     */
    @Test
    void execute_whenStatusIsWaitingRoom_publishesPatientReceivedEvent() {
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.WAITING_ROOM);
        when(consultationRepository.save(consultation)).thenReturn(consultation);
        ConsultationResponseDTO dto =
                new ConsultationResponseDTO(1L, null, null, null, "Paciente", "Dentista", "Sala de Espera", null);
        when(consultationMapper.toDTO(consultation)).thenReturn(dto);

        useCase.execute(consultation, ConsultationStatusType.WAITING_ROOM);

        verify(webSocketEventPublisher).publish(eq(WebSocketEventType.PATIENT_RECEIVED), eq(dto));
    }

    /**
     * CASO: Cambio de estado a FINISHED — debe publicar ATTENTION_FINISHED.
     * Validación: Se invoca el publisher con WebSocketEventType.ATTENTION_FINISHED.
     */
    @Test
    void execute_whenStatusIsFinished_publishesAttentionFinishedEvent() {
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.FINISHED);
        when(consultationRepository.save(consultation)).thenReturn(consultation);
        ConsultationResponseDTO dto =
                new ConsultationResponseDTO(1L, null, null, null, "Paciente", "Dentista", "Finalizada", null);
        when(consultationMapper.toDTO(consultation)).thenReturn(dto);

        useCase.execute(consultation, ConsultationStatusType.FINISHED);

        verify(webSocketEventPublisher).publish(eq(WebSocketEventType.ATTENTION_FINISHED), eq(dto));
    }

    /**
     * CASO: Falla la persistencia de la consulta por error de acceso a base de datos.
     * Validación: Lanza DataBaseException — no se invoca historial ni se publica WebSocket.
     */
    @Test
    void execute_whenRepositoryThrowsDataAccessException_throwsDataBaseException() {
        Consultation consultation = mock(Consultation.class);
        when(consultationRepository.save(consultation))
                .thenThrow(new DataAccessResourceFailureException("db down"));

        assertThatThrownBy(() -> useCase.execute(consultation, ConsultationStatusType.IN_CONSULTATION))
                .isInstanceOf(DataBaseException.class);
    }

    /**
     * CASO: Cambio de estado exitoso — debe retornar el DTO mapeado de la consulta persistida.
     * Validación: El DTO devuelto coincide con el que produjo el mapper.
     */
    @Test
    void execute_whenStatusChanged_returnsMappedDTO() {
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        when(consultationRepository.save(consultation)).thenReturn(consultation);
        ConsultationResponseDTO dto =
                new ConsultationResponseDTO(1L, null, null, null, "Paciente", "Dentista", "En Atención", null);
        when(consultationMapper.toDTO(consultation)).thenReturn(dto);

        ConsultationResponseDTO result = useCase.execute(consultation, ConsultationStatusType.IN_CONSULTATION);

        assertThat(result).isEqualTo(dto);
    }

    /**
     * CASO: El historial recibe la consulta persistida con el nuevo estado.
     * Validación: El ConsultationStatusHistory creado se construye contra la consulta retornada por save().
     */
    @Test
    void execute_whenStatusChanged_createsHistoryWithSavedConsultation() {
        Consultation consultation = mock(Consultation.class);
        Consultation saved = mock(Consultation.class);
        when(saved.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        when(consultationRepository.save(consultation)).thenReturn(saved);
        when(consultationMapper.toDTO(saved)).thenReturn(
                new ConsultationResponseDTO(1L, null, null, null, "Paciente", "Dentista", "En Atención", null));

        useCase.execute(consultation, ConsultationStatusType.IN_CONSULTATION);

        ArgumentCaptor<ConsultationStatusHistory> captor = ArgumentCaptor.forClass(ConsultationStatusHistory.class);
        verify(consultationStatusHistoryService).create(captor.capture());
        assertThat(captor.getValue()).isNotNull();
    }
}