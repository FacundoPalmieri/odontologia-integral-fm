package com.odontologiaintegralfm.feature.consultation.core.consultation.service;

import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationStatusType;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallPatientUseCaseTest {

    // Mocks de dependencias externas (no se ejecuta lógica real)
    @Mock private IConsultationRepository consultationRepository;
    @Mock private ChangeConsultationStatusUseCase changeConsultationStatusUseCase;
    @Mock private MessageSource messageSource;

    // Clase bajo test con los mocks inyectados
    @InjectMocks private CallPatientUseCase useCase;


    /**
     * CASO: La consulta no existe en el repositorio
     *
     * Escenario:
     * - El repositorio devuelve Optional.empty()
     *
     * Validación:
     * - Se debe lanzar NotFoundException
     * - No se debe intentar cambiar el estado
     */
    @Test
    void execute_whenConsultationNotFound_throwsNotFoundException() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(NotFoundException.class);

        // Verificamos que no haya efectos secundarios
        verify(changeConsultationStatusUseCase, never()).execute(any(), any());
    }


    /**
     * CASO: La consulta ya está FINALIZADA
     *
     * Escenario:
     * - Existe la consulta
     * - Su estado es FINISHED
     *
     * Validación:
     * - Se debe lanzar ConflictException
     * - No se debe cambiar el estado
     */
    @Test
    void execute_whenStatusIsFinished_throwsConflictException() {
        // Arrange
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.FINISHED);
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(ConflictException.class);

        // Verificamos que no haya efectos secundarios
        verify(changeConsultationStatusUseCase, never()).execute(any(), any());
    }


    /**
     * CASO: La consulta ya está EN CONSULTA
     *
     * Escenario:
     * - Existe la consulta
     * - Su estado es IN_CONSULTATION
     *
     * Validación:
     * - Se debe lanzar ConflictException
     * - No se debe cambiar el estado
     */
    @Test
    void execute_whenStatusIsInConsultation_throwsConflictException() {
        // Arrange
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(ConflictException.class);

        // Verificamos que no haya efectos secundarios
        verify(changeConsultationStatusUseCase, never()).execute(any(), any());
    }


    /**
     * CASO: La consulta está en SALA DE ESPERA (flujo válido)
     *
     * Escenario:
     * - Existe la consulta
     * - Su estado es WAITING_ROOM
     * - Se puede avanzar a IN_CONSULTATION
     *
     * Validación:
     * - Se debe invocar el caso de uso de cambio de estado
     * - Se debe devolver una respuesta exitosa
     * - El DTO devuelto debe coincidir con el esperado
     */
    @Test
    void execute_whenStatusIsWaitingRoom_changesStatusToInConsultation() {
        // Arrange
        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.WAITING_ROOM);
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));

        // DTO esperado luego del cambio de estado
        ConsultationResponseDTO dto =
                new ConsultationResponseDTO(1L, null, null, null, "Paciente", "Dentista", "En Atención", null);

        // Simulación del cambio de estado
        when(changeConsultationStatusUseCase.execute(
                consultation, ConsultationStatusType.IN_CONSULTATION)
        ).thenReturn(dto);

        // Simulación del messageSource (i18n)
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("ok");

        // Act
        Response<ConsultationResponseDTO> result = useCase.execute(1L);

        // Assert: se invoca correctamente el cambio de estado
        verify(changeConsultationStatusUseCase)
                .execute(consultation, ConsultationStatusType.IN_CONSULTATION);

        // Assert: respuesta correcta
        assertThat(result.success()).isTrue();
        assertThat(result.data()).isEqualTo(dto);
    }
}