package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.ToothFace;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationStepService;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationTypePriceService;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationTypeService;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service.PromotionService;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.model.TreatmentCondition;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.service.TreatmentConditionService;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.service.TreatmentService;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationStatusType;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationRepository;
import com.odontologiaintegralfm.feature.consultation.core.consultation.service.ConsultationQueryService;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto.ConsultationInstanceRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto.ConsultationInstanceResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.repository.IConsultationInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.mapper.OdontogramMapper;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.model.Odontogram;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.repository.IOdontogramRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestation.dto.PrestationInstanceRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestation.dto.PrestationStepAdvancementRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestation.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestation.mapper.PrestationInstanceMapper;
import com.odontologiaintegralfm.feature.consultation.core.prestation.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestation.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestation.repository.IPrestationStepInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestation.service.PrestationInstanceDomainService;
import com.odontologiaintegralfm.feature.consultation.core.prestation.service.PrestationInstanceQueryService;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateConsultationInstanceUseCaseTest {

    @Mock private ConsultationQueryService consultationQueryService;
    @Mock private IConsultationInstanceRepository consultationInstanceRepository;
    @Mock private IOdontogramRepository odontogramRepository;
    @Mock private IPrestationInstanceRepository prestationInstanceRepository;
    @Mock private PrestationInstanceQueryService prestationInstanceQueryService;
    @Mock private PrestationInstanceDomainService prestationInstanceDomainService;
    @Mock private PrestationTypeService prestationTypeService;
    @Mock private PrestationTypePriceService prestationTypePriceService;
    @Mock private PrestationStepService prestationStepService;
    @Mock private IPrestationStepInstanceRepository prestationStepInstanceRepository;
    @Mock private TreatmentService treatmentService;
    @Mock private TreatmentConditionService treatmentConditionService;
    @Mock private PromotionService promotionService;
    @Mock private OdontogramMapper odontogramMapper;
    @Mock private PrestationInstanceMapper prestationInstanceMapper;
    @Mock private MessageSource messageSource;

    @InjectMocks private CreateConsultationInstanceUseCase useCase;

    // ─── Status validation ────────────────────────────────────────────────────

    /**
     * CASO: La consulta no está en estado IN_CONSULTATION (ej: WAITING_ROOM).
     * Validación: Lanza ConflictException — no se puede crear instancia sin atención activa.
     */
    @Test
    void execute_whenConsultationStatusIsNotInConsultation_throwsConflictException() {
        ConsultationInstanceRequestDTO dto = new ConsultationInstanceRequestDTO(
                1L, null, List.of(), List.of(), List.of());

        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.WAITING_ROOM);
        when(consultationQueryService.findById(1L)).thenReturn(consultation);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(ConflictException.class);
    }

    // ─── ConsultationInstance duplicate ──────────────────────────────────────

    /**
     * CASO: Ya existe una instancia de consulta para la misma consulta.
     * Escenario: El repository retorna una instancia previa no nula.
     * Validación: Lanza ConflictException y no persiste una segunda instancia.
     */
    @Test
    void execute_whenExistingConsultationInstance_throwsConflictException() {
        ConsultationInstanceRequestDTO dto = new ConsultationInstanceRequestDTO(
                1L, null, List.of(), List.of(), List.of());

        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        ConsultationInstance existing = mock(ConsultationInstance.class);
        when(existing.getId()).thenReturn(99L);

        when(consultationQueryService.findById(1L)).thenReturn(consultation);
        when(consultationInstanceRepository.findByConsultationId(1L)).thenReturn(existing);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(ConflictException.class);

        verify(consultationInstanceRepository, never()).save(any());
    }

    // ─── PrestationType isUnique ──────────────────────────────────────────────

    /**
     * CASO: Se envía una prestación marcada como "única" junto con otra en la misma request.
     * Escenario: prestationType.isUnique() = true con 2 prestaciones nuevas.
     * Validación: Lanza ConflictException por violación de unicidad de la prestación.
     */
    @Test
    void execute_whenUniquePrestationTypeWithMultiplePrestations_throwsConflictException() {
        PrestationInstanceRequestDTO p1 = new PrestationInstanceRequestDTO(
                10L, null, null, null, null, null, null, null, null, null, null, null);
        PrestationInstanceRequestDTO p2 = new PrestationInstanceRequestDTO(
                10L, null, null, null, null, null, null, null, null, null, null, null);
        ConsultationInstanceRequestDTO dto = new ConsultationInstanceRequestDTO(
                1L, null, List.of(), List.of(p1, p2), List.of());

        Consultation consultation = mock(Consultation.class);
        Patient patient = mock(Patient.class);

        when(patient.getId()).thenReturn(1L);

        when(consultation.getPatient()).thenReturn(patient);

        when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        PrestationType prestationType = mock(PrestationType.class);
        when(prestationType.isUnique()).thenReturn(true);
        when(prestationType.getName()).thenReturn("Radiografía");

        when(consultationQueryService.findById(1L)).thenReturn(consultation);
        when(consultationInstanceRepository.findByConsultationId(1L)).thenReturn(null);
        when(consultationInstanceRepository.save(any())).thenReturn(mock(ConsultationInstance.class));
        when(odontogramRepository.saveAll(any())).thenReturn(List.of());
        when(prestationTypeService.findById(10L)).thenReturn(prestationType);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(ConflictException.class);
    }

    // ─── Odontogram reference mismatch ───────────────────────────────────────

    /**
     * CASO: La prestación referencia una ubicación en el odontograma que no fue registrada en la visita.
     * Escenario: Se registró T11/TOP en el odontograma, pero la prestación solicita ubicación en T21/BOTTOM.
     * Validación: Lanza BadRequestException al no encontrar el odontograma correspondiente.
     */
    @Test
    void execute_whenPrestationOdontogramNotMatchingSaved_throwsBadRequestException() {
        OdontogramRequestDTO toothRegistered = new OdontogramRequestDTO(Tooth.T11, ToothFace.TOP, 1L, 1L);
        OdontogramRequestDTO differentTooth = new OdontogramRequestDTO(Tooth.T21, ToothFace.BOTTOM, 1L, 1L);

        PrestationInstanceRequestDTO prestacionDTO = new PrestationInstanceRequestDTO(
                10L, null, null, differentTooth, null, null, null, null, null, null, null, null);
        ConsultationInstanceRequestDTO dto = new ConsultationInstanceRequestDTO(
                1L, null, List.of(toothRegistered), List.of(prestacionDTO), List.of());

        Consultation consultation = mock(Consultation.class);
        Patient patient = mock(Patient.class);

        when(patient.getId()).thenReturn(1L);

        when(consultation.getPatient()).thenReturn(patient);

        when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        PrestationType prestationType = mock(PrestationType.class);
        when(prestationType.isUnique()).thenReturn(false);

        Odontogram savedOdontogram = mock(Odontogram.class);
        when(savedOdontogram.getTooth()).thenReturn(Tooth.T11);

        when(consultationQueryService.findById(1L)).thenReturn(consultation);
        when(consultationInstanceRepository.findByConsultationId(1L)).thenReturn(null);
        when(consultationInstanceRepository.save(any())).thenReturn(mock(ConsultationInstance.class));
        when(treatmentService.findById(1L)).thenReturn(mock(Treatment.class));
        when(treatmentConditionService.findById(1L)).thenReturn(mock(TreatmentCondition.class));
        when(odontogramRepository.saveAll(any())).thenReturn(List.of(savedOdontogram));
        when(prestationTypeService.findById(10L)).thenReturn(prestationType);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(BadRequestException.class);
    }

    // ─── Step advancements ────────────────────────────────────────────────────

    /**
     * CASO: Se incluye un step con estado CANCELLED en stepAdvancements[].
     * Regla: CANCELLED no es un estado válido en el flujo de creación de instancia.
     * Validación: Lanza ConflictException y no persiste ningún step.
     */
    @Test
    void execute_whenStepAdvancementStatusIsCancelled_throwsConflictException() {
        PrestationStepAdvancementRequestDTO cancelledStep = new PrestationStepAdvancementRequestDTO(
                100L, 200L, PrestationStepStatus.CANCELLED);
        ConsultationInstanceRequestDTO dto = new ConsultationInstanceRequestDTO(
                1L, null, List.of(), List.of(), List.of(cancelledStep));

        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        when(consultationQueryService.findById(1L)).thenReturn(consultation);
        when(consultationInstanceRepository.findByConsultationId(1L)).thenReturn(null);
        when(consultationInstanceRepository.save(any())).thenReturn(mock(ConsultationInstance.class));
        when(odontogramRepository.saveAll(any())).thenReturn(List.of());
        when(prestationInstanceRepository.saveAll(any())).thenReturn(List.of());

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(ConflictException.class);

        verify(prestationStepInstanceRepository, never()).saveAll(any());
    }

    /**
     * CASO: Los steps del catálogo quedan todos completados/omitidos tras el avance.
     * Escenario: shouldComplete retorna true para la prestación instanciada.
     * Validación: La prestación transiciona a COMPLETED y se persiste en base.
     */
    @Test
    void execute_whenShouldComplete_completesAndSavesPrestationInstance() {
        PrestationStepAdvancementRequestDTO advancement = new PrestationStepAdvancementRequestDTO(
                100L, 200L, PrestationStepStatus.COMPLETED);
        ConsultationInstanceRequestDTO dto = new ConsultationInstanceRequestDTO(
                1L, null, List.of(), List.of(), List.of(advancement));

        Consultation consultation = mock(Consultation.class);

        Patient patient = mock(Patient.class);

        when(patient.getId()).thenReturn(1L);

        when(consultation.getPatient()).thenReturn(patient);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        PrestationInstance prestationInstance = mock(PrestationInstance.class);
        PrestationType prestationType = mock(PrestationType.class);
        when(prestationInstance.getType()).thenReturn(prestationType);
        when(prestationType.getId()).thenReturn(10L);

        when(consultationQueryService.findById(1L)).thenReturn(consultation);
        when(consultationInstanceRepository.findByConsultationId(1L)).thenReturn(null);
        when(consultationInstanceRepository.save(any())).thenReturn(mock(ConsultationInstance.class));
        when(odontogramRepository.saveAll(any())).thenReturn(List.of());
        when(prestationInstanceRepository.saveAll(any())).thenReturn(List.of());
        when(prestationInstanceQueryService.findById(100L)).thenReturn(prestationInstance);
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(List.of());
        when(prestationStepInstanceRepository.findByPrestationInstance(prestationInstance)).thenReturn(List.of());
        when(prestationInstanceDomainService.validateAndBuildStepAdvancements(any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(prestationInstanceDomainService.shouldComplete(any(), any())).thenReturn(true);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("ok");

        useCase.execute(dto);

        verify(prestationInstance).complete();
        verify(prestationInstanceRepository).save(prestationInstance);
    }

    /**
     * CASO: Los steps del catálogo tienen pasos pendientes tras el avance.
     * Escenario: shouldComplete retorna false para la prestación instanciada.
     * Validación: Los steps nuevos se guardan pero la prestación NO transiciona a COMPLETED.
     */
    @Test
    void execute_whenShouldNotComplete_savesStepsWithoutCompletingPrestation() {
        PrestationStepAdvancementRequestDTO advancement = new PrestationStepAdvancementRequestDTO(
                100L, 200L, PrestationStepStatus.IN_PROGRESS);
        ConsultationInstanceRequestDTO dto = new ConsultationInstanceRequestDTO(
                1L, null, List.of(), List.of(), List.of(advancement));

        Consultation consultation = mock(Consultation.class);

        Patient patient = mock(Patient.class);

        when(patient.getId()).thenReturn(1L);

        when(consultation.getPatient()).thenReturn(patient);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        PrestationInstance prestationInstance = mock(PrestationInstance.class);
        PrestationType prestationType = mock(PrestationType.class);
        when(prestationInstance.getType()).thenReturn(prestationType);
        when(prestationType.getId()).thenReturn(10L);

        when(consultationQueryService.findById(1L)).thenReturn(consultation);
        when(consultationInstanceRepository.findByConsultationId(1L)).thenReturn(null);
        when(consultationInstanceRepository.save(any())).thenReturn(mock(ConsultationInstance.class));
        when(odontogramRepository.saveAll(any())).thenReturn(List.of());
        when(prestationInstanceRepository.saveAll(any())).thenReturn(List.of());
        when(prestationInstanceQueryService.findById(100L)).thenReturn(prestationInstance);
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(List.of());
        when(prestationStepInstanceRepository.findByPrestationInstance(prestationInstance)).thenReturn(List.of());
        when(prestationInstanceDomainService.validateAndBuildStepAdvancements(any(), any(), any(), any(), any()))
                .thenReturn(List.of());
        when(prestationInstanceDomainService.shouldComplete(any(), any())).thenReturn(false);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("ok");

        useCase.execute(dto);

        verify(prestationInstance, never()).complete();
        verify(prestationInstanceRepository, never()).save(prestationInstance);
        verify(prestationStepInstanceRepository).saveAll(any());
    }

    // ─── Happy path ───────────────────────────────────────────────────────────

    /**
     * CASO: Request mínima válida sin prestaciones nuevas ni avances de steps.
     * Escenario: Solo se registra la instancia de consulta con odontograma vacío.
     * Validación: Retorna Response con success=true y persiste la instancia y el odontograma.
     */
    @Test
    void execute_whenValidMinimalRequest_returnsSuccessResponse() {
        ConsultationInstanceRequestDTO dto = new ConsultationInstanceRequestDTO(
                1L, "Sin observaciones", List.of(), List.of(), List.of());

        Consultation consultation = mock(Consultation.class);
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);

        when(consultationQueryService.findById(1L)).thenReturn(consultation);
        when(consultationInstanceRepository.findByConsultationId(1L)).thenReturn(null);
        when(consultationInstanceRepository.save(any())).thenReturn(mock(ConsultationInstance.class));
        when(odontogramRepository.saveAll(any())).thenReturn(List.of());
        when(prestationInstanceRepository.saveAll(any())).thenReturn(List.of());
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Consulta registrada");

        Response<ConsultationInstanceResponseDTO> result = useCase.execute(dto);

        assertThat(result.success()).isTrue();
        assertThat(result.data().odontogram()).isEmpty();
        assertThat(result.data().prestationInstance()).isEmpty();
        verify(consultationInstanceRepository).save(any());
        verify(odontogramRepository).saveAll(any());
    }
}
