package com.odontologiaintegralfm.feature.consultation.core.consultation.service;

import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationCorrectionRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationStatusType;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.ConsultationEvent;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationRepository;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateConsultationCorrectionUseCaseTest {

    @Mock private IConsultationRepository consultationRepository;
    @Mock private MessageSource messageSource;
    @Mock private ConsultationEventService consultationEventService;
    @Mock private ChangeConsultationStatusUseCase changeConsultationStatusUseCase;

    @InjectMocks private UpdateConsultationCorrectionUseCase useCase;

    private final ConsultationCorrectionRequestDTO correction = new ConsultationCorrectionRequestDTO("Se inició por error");

    @Test
    void execute_whenConsultationNotFound_throwsNotFoundException() {
        when(consultationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(1L, correction))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void execute_whenStatusIsFinished_throwsConflictException() {
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.FINISHED);
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

        assertThatThrownBy(() -> useCase.execute(1L, correction))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void execute_whenStatusIsWaitingRoom_throwsConflictException() {
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.WAITING_ROOM);
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

        assertThatThrownBy(() -> useCase.execute(1L, correction))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void execute_whenStatusIsInConsultation_revertsToPreviousStatus() {
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

        ConsultationResponseDTO dto = new ConsultationResponseDTO(1L, "Paciente", "Dentista", "Sala de Espera");
        when(changeConsultationStatusUseCase.execute(consultation, ConsultationStatusType.WAITING_ROOM)).thenReturn(dto);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("ok");

        Response<ConsultationResponseDTO> result = useCase.execute(1L, correction);

        verify(changeConsultationStatusUseCase).execute(consultation, ConsultationStatusType.WAITING_ROOM);
        assertThat(result.success()).isTrue();
    }

    @Test
    void execute_onSuccess_createsConsultationCorrectedEvent() {
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

        ConsultationResponseDTO dto = new ConsultationResponseDTO(1L, "Paciente", "Dentista", "Sala de Espera");
        when(changeConsultationStatusUseCase.execute(any(), any())).thenReturn(dto);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("ok");

        useCase.execute(1L, correction);

        verify(consultationEventService).create(any(ConsultationEvent.class));
    }
}
