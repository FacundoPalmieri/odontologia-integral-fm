package com.odontologiaintegralfm.feature.consultation.core.prestation.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.ToothFace;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationStepService;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.model.Odontogram;
import com.odontologiaintegralfm.feature.consultation.core.prestation.dto.PrestationInstanceRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestation.dto.PrestationStepAdvancementRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestation.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestation.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestation.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestation.model.PrestationStepInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestation.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrestationInstanceDomainServiceTest {

    @Mock private PrestationStepService prestationStepService;
    @Mock private IPrestationInstanceRepository prestationInstanceRepository;
    @Mock private PrestationStepInstanceQueryService prestationStepInstanceQueryService;

    @InjectMocks private PrestationInstanceDomainService service;

    // ─── validatePrestationStep ───────────────────────────────────────────────

    /**
     * CASO: El step enviado no pertenece al workflow del tipo de prestación.
     * Validación: Lanza BadRequestException al no encontrar el step en el catálogo.
     */
    @Test
    void validatePrestationStep_whenStepNotBelongsToPrestation_throwsBadRequestException() {
        PrestationStep step = mock(PrestationStep.class);
        when(step.getId()).thenReturn(1L);
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(List.of(step));

        assertThatThrownBy(() -> service.validatePrestationStep(10L, 99L))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: El step enviado sí pertenece al workflow del tipo de prestación.
     * Validación: No lanza excepción.
     */
    @Test
    void validatePrestationStep_whenStepBelongsToPrestation_doesNotThrow() {
        PrestationStep step = mock(PrestationStep.class);
        when(step.getId()).thenReturn(1L);
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(List.of(step));

        assertThatCode(() -> service.validatePrestationStep(10L, 1L))
                .doesNotThrowAnyException();
    }

    // ─── validatePrestationInstancePending ───────────────────────────────────

    /**
     * CASO: Existe una PrestationInstance IN_PROGRESS con el mismo tipo y misma ubicación en odontograma.
     * Validación: Lanza ConflictException por prestación pendiente en la misma ubicación.
     */
    @Test
    void validatePrestationInstancePending_whenInProgressAtSameOdontogramLocation_throwsConflictException() {
        Odontogram existingOdontogram = mock(Odontogram.class);
        when(existingOdontogram.getTooth()).thenReturn(Tooth.T11);
        when(existingOdontogram.getToothFace()).thenReturn(ToothFace.TOP);

        PrestationInstance existing = mock(PrestationInstance.class, RETURNS_DEEP_STUBS);
        when(existing.getType().getId()).thenReturn(10L);
        when(existing.getOdontogram()).thenReturn(existingOdontogram);

        when(prestationInstanceRepository.findPrestationInstanceByPatientAndStatus(5L, PrestationInstanceStatus.IN_PROGRESS))
                .thenReturn(List.of(existing));


        OdontogramRequestDTO odontogramDTO = new OdontogramRequestDTO(Tooth.T11, ToothFace.TOP, 1L, 1L);
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, 200L, PrestationStepStatus.IN_PROGRESS, odontogramDTO,
                null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> service.validatePrestationInstancePending(5L, dto))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Existe una PrestationInstance IN_PROGRESS con el mismo tipo y misma ubicación en odontograma,
     *       pero sin ningún step activo (la prestación nació sin step inicial).
     * Regresión: antes de la corrección, sameState() retornaba false en este caso y no lanzaba excepción.
     * Validación: Lanza ConflictException por prestación pendiente en la misma ubicación, sin importar el estado de los steps.
     */
    @Test
    void validatePrestationInstancePending_whenInProgressAtSameLocationWithNoSteps_throwsConflictException() {
        Odontogram existingOdontogram = mock(Odontogram.class);
        when(existingOdontogram.getTooth()).thenReturn(Tooth.T11);
        when(existingOdontogram.getToothFace()).thenReturn(ToothFace.TOP);

        PrestationInstance existing = mock(PrestationInstance.class, RETURNS_DEEP_STUBS);
        when(existing.getType().getId()).thenReturn(10L);
        when(existing.getOdontogram()).thenReturn(existingOdontogram);

        when(prestationInstanceRepository.findPrestationInstanceByPatientAndStatus(5L, PrestationInstanceStatus.IN_PROGRESS))
                .thenReturn(List.of(existing));

        OdontogramRequestDTO odontogramDTO = new OdontogramRequestDTO(Tooth.T11, ToothFace.TOP, 1L, 1L);
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, odontogramDTO,
                null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> service.validatePrestationInstancePending(5L, dto))
                .isInstanceOf(ConflictException.class);
    }

    // ─── validateStepAdvancement ─────────────────────────────────────────────

    /**
     * CASO: Se intenta avanzar un step en una prestación que NO está IN_PROGRESS (ej: COMPLETED).
     * Validación: Lanza ConflictException porque no se puede operar sobre una prestación finalizada.
     */
    @Test
    void validateStepAdvancement_whenPrestationNotInProgress_throwsConflictException() {
        PrestationInstance prestation = mock(PrestationInstance.class, RETURNS_DEEP_STUBS);
        when(prestation.getConsultationInstance().getConsultation().getPatient().getId()).thenReturn(5L);
        when(prestation.getStatus()).thenReturn(PrestationInstanceStatus.COMPLETED);
        when(prestation.getId()).thenReturn(100L);

        PrestationStepAdvancementRequestDTO dto = new PrestationStepAdvancementRequestDTO(
                100L, 200L, PrestationStepStatus.COMPLETED);

        assertThatThrownBy(() -> service.validateStepAdvancement(5L, prestation, dto))
                .isInstanceOf(ConflictException.class);
    }

    // ─── validateAndBuildStepAdvancements ────────────────────────────────────

    /**
     * CASO: El step ya existe con estado IN_PROGRESS y se intenta registrar nuevamente como IN_PROGRESS.
     * Regla: Un step IN_PROGRESS solo puede avanzar a COMPLETED.
     * Validación: Lanza ConflictException por transición de estado inválida.
     */
    @Test
    void validateAndBuildStepAdvancements_whenInProgressStepNotAdvancedToCompleted_throwsConflictException() {
        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getId()).thenReturn(1L);

        PrestationStepInstance existingInstance = mock(PrestationStepInstance.class);
        when(existingInstance.getStep()).thenReturn(step1);
        when(existingInstance.getStatus()).thenReturn(PrestationStepStatus.IN_PROGRESS);

        // Se intenta registrar el mismo step con IN_PROGRESS en lugar de COMPLETED
        PrestationStepAdvancementRequestDTO dto = new PrestationStepAdvancementRequestDTO(
                100L, 1L, PrestationStepStatus.IN_PROGRESS);

        assertThatThrownBy(() -> service.validateAndBuildStepAdvancements(
                List.of(step1),
                List.of(existingInstance),
                List.of(dto),
                mock(ConsultationInstance.class),
                mock(PrestationInstance.class)))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Se envía un step 2 sin haber resuelto (COMPLETED u OMITTED) el step 1 previo.
     * Regla: Los steps deben avanzarse en orden; no se puede saltar un step sin resolver el anterior.
     * Validación: Lanza BadRequestException por step previo no resuelto.
     */
    @Test
    void validateAndBuildStepAdvancements_whenPreviousStepNotResolved_throwsBadRequestException() {
        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getId()).thenReturn(1L);
        when(step1.getPosition()).thenReturn(1);

        PrestationStep step2 = mock(PrestationStep.class);
        when(step2.getId()).thenReturn(2L);
        when(step2.getPosition()).thenReturn(2);

        // Se envía directamente el step 2 sin que step 1 esté resuelto
        PrestationStepAdvancementRequestDTO dto = new PrestationStepAdvancementRequestDTO(
                100L, 2L, PrestationStepStatus.COMPLETED);

        assertThatThrownBy(() -> service.validateAndBuildStepAdvancements(
                List.of(step1, step2),
                List.of(),
                List.of(dto),
                mock(ConsultationInstance.class),
                mock(PrestationInstance.class)))
                .isInstanceOf(BadRequestException.class);
    }
}
