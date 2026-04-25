package com.odontologiaintegralfm.feature.consultation.core.consultation.service;

import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service.IAppointmentService;
import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationStatusType;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateConsultationUseCaseTest {

    @Mock private IAppointmentService appointmentService;
    @Mock private ChangeConsultationStatusUseCase changeStatusUseCase;
    @Mock private MessageSource messageSource;

    @InjectMocks private CreateConsultationUseCase useCase;

    @Test
    void execute_whenAppointmentDateIsNotToday_throwsConflictException() {
        Appointment appointment = mock(Appointment.class);
        when(appointment.getDate()).thenReturn(LocalDateTime.now().minusDays(1));
        when(appointmentService.getById(1L)).thenReturn(appointment);

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(ConflictException.class);
    }



    @Test
    void execute_whenAppointmentDateIsToday_createsConsultationInWaitingRoom() {
        Appointment appointment = mock(Appointment.class);
        when(appointment.getDate()).thenReturn(LocalDateTime.now());
        when(appointmentService.getById(1L)).thenReturn(appointment);

        ConsultationResponseDTO dto = new ConsultationResponseDTO(1L, "Paciente", "Dentista", "Sala de Espera");
        when(changeStatusUseCase.execute(any(), eq(ConsultationStatusType.WAITING_ROOM))).thenReturn(dto);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Consulta creada");

        Response<ConsultationResponseDTO> result = useCase.execute(1L);

        verify(changeStatusUseCase).execute(any(), eq(ConsultationStatusType.WAITING_ROOM));
        assertThat(result.success()).isTrue();
        assertThat(result.data()).isEqualTo(dto);
    }
}
