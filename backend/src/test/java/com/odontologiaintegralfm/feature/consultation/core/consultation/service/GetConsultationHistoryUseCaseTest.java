package com.odontologiaintegralfm.feature.consultation.core.consultation.service;

import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationHistoryResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationStatusType;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationRepository;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.repository.IConsultationInstanceRepository;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.patient.core.service.interfaces.IPatientService;
import com.odontologiaintegralfm.feature.person.core.model.Person;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class GetConsultationHistoryUseCaseTest {

    @Mock private IPatientService patientService;
    @Mock private IConsultationRepository consultationRepository;
    @Mock private IConsultationInstanceRepository consultationInstanceRepository;

    @InjectMocks private GetConsultationHistoryUseCase useCase;

    /**
     * Stub neutro de los colaboradores que ConsultationHistoryResponseDTO.build() navega siempre
     * (appointment.getDate(), dentist.getPerson().getFullName(), status) — irrelevantes para las
     * assertions de estos casos, pero necesarios para que build() no haga NPE.
     */
    private void stubBuildDependencies(Consultation consultation) {
        Appointment appointment = mock(Appointment.class);
        Dentist dentist = mock(Dentist.class);
        Person person = mock(Person.class);
        when(appointment.getDate()).thenReturn(LocalDateTime.now());
        when(person.getFullName()).thenReturn("Nombre Apellido");
        when(dentist.getPerson()).thenReturn(person);
        when(consultation.getAppointment()).thenReturn(appointment);
        when(consultation.getDentist()).thenReturn(dentist);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.WAITING_ROOM);
    }

    /**
     * CASO: El paciente tiene múltiples consultas.
     * Regla: el repo ya devuelve las consultas ordenadas DESC por id; el use case no reordena.
     * Validación: el resultado tiene exactamente 3 elementos y respeta la secuencia devuelta por el repo.
     */
    @Test
    void getHistory_multipleConsultations_orderedByIdDesc() {
        Consultation c30 = mock(Consultation.class);
        Consultation c20 = mock(Consultation.class);
        Consultation c10 = mock(Consultation.class);
        when(c30.getId()).thenReturn(30L);
        when(c20.getId()).thenReturn(20L);
        when(c10.getId()).thenReturn(10L);
        stubBuildDependencies(c30);
        stubBuildDependencies(c20);
        stubBuildDependencies(c10);

        when(patientService.findById(1L)).thenReturn(mock(com.odontologiaintegralfm.feature.patient.core.model.Patient.class));
        when(consultationRepository.findByPatientIdOrderByIdDesc(1L)).thenReturn(List.of(c30, c20, c10));
        when(consultationInstanceRepository.findByConsultationPatientId(1L)).thenReturn(List.of());

        Response<List<ConsultationHistoryResponseDTO>> result = useCase.execute(1L);

        assertThat(result.data()).hasSize(3);
        assertThat(result.data().get(0).consultationId()).isEqualTo(30L);
        assertThat(result.data().get(1).consultationId()).isEqualTo(20L);
        assertThat(result.data().get(2).consultationId()).isEqualTo(10L);
    }

    /**
     * CASO: Una consulta del historial no tiene ConsultationInstance creada todavía.
     * Regla: consultationInstanceId viaja en null cuando no hay instance asociada.
     * Validación: el DTO tiene consultationInstanceId() == null y no lanza excepción.
     */
    @Test
    void getHistory_consultationWithoutInstance_instanceIdNull() {
        Consultation c1 = mock(Consultation.class);
        when(c1.getId()).thenReturn(1L);
        stubBuildDependencies(c1);

        when(patientService.findById(1L)).thenReturn(mock(com.odontologiaintegralfm.feature.patient.core.model.Patient.class));
        when(consultationRepository.findByPatientIdOrderByIdDesc(1L)).thenReturn(List.of(c1));
        when(consultationInstanceRepository.findByConsultationPatientId(1L)).thenReturn(List.of());

        Response<List<ConsultationHistoryResponseDTO>> result = useCase.execute(1L);

        assertThat(result.data().get(0).consultationInstanceId()).isNull();
    }

    /**
     * CASO: Dos consultas, solo una tiene ConsultationInstance asociada.
     * Regla: el mapeo consultationId -> instanceId no debe cruzarse entre consultas.
     * Validación: el DTO de la consulta id=2 tiene consultationInstanceId()==99; el de id=1 tiene null.
     */
    @Test
    void getHistory_consultationWithInstance_mapsInstanceIdCorrectly() {
        Consultation c1 = mock(Consultation.class);
        Consultation c2 = mock(Consultation.class);
        when(c1.getId()).thenReturn(1L);
        when(c2.getId()).thenReturn(2L);
        stubBuildDependencies(c1);
        stubBuildDependencies(c2);

        ConsultationInstance instance99 = mock(ConsultationInstance.class);
        Consultation consultationOfInstance = mock(Consultation.class);
        when(consultationOfInstance.getId()).thenReturn(2L);
        when(instance99.getId()).thenReturn(99L);
        when(instance99.getConsultation()).thenReturn(consultationOfInstance);

        when(patientService.findById(1L)).thenReturn(mock(com.odontologiaintegralfm.feature.patient.core.model.Patient.class));
        when(consultationRepository.findByPatientIdOrderByIdDesc(1L)).thenReturn(List.of(c1, c2));
        when(consultationInstanceRepository.findByConsultationPatientId(1L)).thenReturn(List.of(instance99));

        Response<List<ConsultationHistoryResponseDTO>> result = useCase.execute(1L);

        ConsultationHistoryResponseDTO dtoC1 = result.data().stream()
                .filter(dto -> dto.consultationId().equals(1L)).findFirst().orElseThrow();
        ConsultationHistoryResponseDTO dtoC2 = result.data().stream()
                .filter(dto -> dto.consultationId().equals(2L)).findFirst().orElseThrow();

        assertThat(dtoC2.consultationInstanceId()).isEqualTo(99L);
        assertThat(dtoC1.consultationInstanceId()).isNull();
    }

    /**
     * CASO: Se solicita el historial de un paciente inexistente.
     * Regla: la existencia del paciente se valida antes de consultar consultas/instances.
     * Validación: se propaga NotFoundException y nunca se invocan los repos de consultas/instances.
     */
    @Test
    void getHistory_patientDoesNotExist_throwsNotFoundException() {
        when(patientService.findById(99L)).thenThrow(NotFoundException.class);

        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(NotFoundException.class);

        verify(consultationRepository, never()).findByPatientIdOrderByIdDesc(org.mockito.ArgumentMatchers.anyLong());
        verify(consultationInstanceRepository, never()).findByConsultationPatientId(org.mockito.ArgumentMatchers.anyLong());
    }

    /**
     * CASO: El paciente existe pero no tiene consultas registradas.
     * Regla: la ausencia de consultas es un resultado válido, no un error.
     * Validación: Response.data() es una lista vacía (no null), Response.success()==true, sin excepción.
     */
    @Test
    void getHistory_patientWithNoConsultations_returnsEmptyList() {
        when(patientService.findById(1L)).thenReturn(mock(com.odontologiaintegralfm.feature.patient.core.model.Patient.class));
        when(consultationRepository.findByPatientIdOrderByIdDesc(1L)).thenReturn(List.of());
        when(consultationInstanceRepository.findByConsultationPatientId(1L)).thenReturn(List.of());

        Response<List<ConsultationHistoryResponseDTO>> result = useCase.execute(1L);

        assertThat(result.data()).isNotNull();
        assertThat(result.data()).isEmpty();
        assertThat(result.success()).isTrue();
    }
}
