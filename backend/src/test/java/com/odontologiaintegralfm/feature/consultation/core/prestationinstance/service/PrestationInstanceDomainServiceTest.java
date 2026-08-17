package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.DiscountType;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.enums.ToothFace;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationStepService;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.model.Odontogram;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationStepAdvancementRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationStepInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class PrestationInstanceDomainServiceTest {

    @Mock private PrestationStepService prestationStepService;
    @Mock private IPrestationInstanceRepository prestationInstanceRepository;
    @Mock private PrestationStepInstanceQueryService prestationStepInstanceQueryService;

    @InjectMocks private PrestationInstanceDomainService service;

    // ─── validateLocation ─────────────────────────────────────────────────────

    /**
     * CASO: DTO sin odontograma y sin scope.
     * Regla: La prestación debe tener exactamente una de las dos ubicaciones.
     * Validación: Lanza BadRequestException por falta total de ubicación.
     */
    @Test
    void validateLocation_whenOdontogramAndScopeNull_throwsBadRequestException() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                null, null, null, null, null,
                null, null, null);

        assertThatThrownBy(() -> service.validateLocation(dto))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: DTO con odontograma Y scope simultáneamente.
     * Regla: Odontograma y scope son mutuamente excluyentes.
     * Validación: Lanza BadRequestException por ambigüedad de ubicación.
     */
    @Test
    void validateLocation_whenOdontogramAndScopeBothPresent_throwsBadRequestException() {
        OdontogramRequestDTO odontogram = new OdontogramRequestDTO(Tooth.T11, ToothFace.TOP, 1L, 1L);
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, odontogram,
                PrestationScopeType.TOOTH, Tooth.T11, null, null, null,
                null, null, null);

        assertThatThrownBy(() -> service.validateLocation(dto))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: DTO con odontograma y sin scope.
     * Regla: Caso válido — la ubicación se resuelve por odontograma.
     * Validación: No lanza excepción.
     */
    @Test
    void validateLocation_whenOdontogramPresentAndScopeNull_doesNotThrow() {
        OdontogramRequestDTO odontogram = new OdontogramRequestDTO(Tooth.T11, ToothFace.TOP, 1L, 1L);
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, odontogram,
                null, null, null, null, null,
                null, null, null);

        assertThatCode(() -> service.validateLocation(dto))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Scope TOOTH sin campo tooth.
     * Regla: Scope TOOTH requiere tooth.
     * Validación: Lanza BadRequestException por dato faltante en scope.
     */
    @Test
    void validateLocation_whenScopeToothWithoutTooth_throwsBadRequestException() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                PrestationScopeType.TOOTH, null, null, null, null,
                null, null, null);

        assertThatThrownBy(() -> service.validateLocation(dto))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: Scope TOOTH con campo tooth.
     * Validación: No lanza excepción.
     */
    @Test
    void validateLocation_whenScopeToothWithTooth_doesNotThrow() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                PrestationScopeType.TOOTH, Tooth.T11, null, null, null,
                null, null, null);

        assertThatCode(() -> service.validateLocation(dto))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Scope TOOTH_FACE sin campo toothFace.
     * Regla: Scope TOOTH_FACE requiere tooth y toothFace.
     * Validación: Lanza BadRequestException por dato faltante en scope.
     */
    @Test
    void validateLocation_whenScopeToothFaceWithoutToothFace_throwsBadRequestException() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                PrestationScopeType.TOOTH_FACE, Tooth.T11, null, null, null,
                null, null, null);

        assertThatThrownBy(() -> service.validateLocation(dto))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: Scope TOOTH_FACE sin campo tooth.
     * Regla: Scope TOOTH_FACE requiere tooth y toothFace.
     * Validación: Lanza BadRequestException por dato faltante en scope.
     */
    @Test
    void validateLocation_whenScopeToothFaceWithoutTooth_throwsBadRequestException() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                PrestationScopeType.TOOTH_FACE, null, ToothFace.TOP, null, null,
                null, null, null);

        assertThatThrownBy(() -> service.validateLocation(dto))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: Scope QUADRANT sin campo quadrant.
     * Regla: Scope QUADRANT requiere quadrant.
     * Validación: Lanza BadRequestException por dato faltante en scope.
     */
    @Test
    void validateLocation_whenScopeQuadrantWithoutQuadrant_throwsBadRequestException() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                PrestationScopeType.QUADRANT, null, null, null, null,
                null, null, null);

        assertThatThrownBy(() -> service.validateLocation(dto))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: Scope QUADRANT con campo quadrant.
     * Validación: No lanza excepción.
     */
    @Test
    void validateLocation_whenScopeQuadrantWithQuadrant_doesNotThrow() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                PrestationScopeType.QUADRANT, null, null, Quadrant.UPPER_RIGHT, null,
                null, null, null);

        assertThatCode(() -> service.validateLocation(dto))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Scope MAXILLARY sin campo maxillary.
     * Regla: Scope MAXILLARY requiere maxillary.
     * Validación: Lanza BadRequestException por dato faltante en scope.
     */
    @Test
    void validateLocation_whenScopeMaxillaryWithoutMaxillary_throwsBadRequestException() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                PrestationScopeType.MAXILLARY, null, null, null, null,
                null, null, null);

        assertThatThrownBy(() -> service.validateLocation(dto))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: Scope MAXILLARY con campo maxillary.
     * Validación: No lanza excepción.
     */
    @Test
    void validateLocation_whenScopeMaxillaryWithMaxillary_doesNotThrow() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                PrestationScopeType.MAXILLARY, null, null, null, Maxillary.UPPER,
                null, null, null);

        assertThatCode(() -> service.validateLocation(dto))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Scope FULL_MOUTH (default) con algún campo de ubicación específica colado.
     * Regla: FULL_MOUTH no admite tooth, toothFace, quadrant ni maxillary.
     * Validación: Lanza BadRequestException por campos incompatibles con FULL_MOUTH.
     */
    @Test
    void validateLocation_whenScopeFullMouthWithLocationFields_throwsBadRequestException() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                PrestationScopeType.FULL_MOUTH, Tooth.T11, null, null, null,
                null, null, null);

        assertThatThrownBy(() -> service.validateLocation(dto))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: Scope FULL_MOUTH sin campos de ubicación específica.
     * Validación: No lanza excepción.
     */
    @Test
    void validateLocation_whenScopeFullMouthWithoutLocationFields_doesNotThrow() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                PrestationScopeType.FULL_MOUTH, null, null, null, null,
                null, null, null);

        assertThatCode(() -> service.validateLocation(dto))
                .doesNotThrowAnyException();
    }

    // ─── validateDiscount ─────────────────────────────────────────────────────

    /**
     * CASO: La prestación trae promotionId y discountValue simultáneamente.
     * Regla: Promoción y descuento manual son mutuamente excluyentes.
     * Validación: Lanza BadRequestException por coexistencia inválida.
     */
    @Test
    void validateDiscount_whenPromotionAndManualDiscountCoexist_throwsBadRequestException() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                null, null, null, null, null,
                5L, DiscountType.FIXED, new BigDecimal("100"));

        assertThatThrownBy(() -> service.validateDiscount(dto))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: Se envía discountType sin discountValue.
     * Regla: Tipo y valor del descuento manual deben venir juntos o ninguno.
     * Validación: Lanza BadRequestException por descuento incompleto.
     */
    @Test
    void validateDiscount_whenDiscountTypeWithoutValue_throwsBadRequestException() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                null, null, null, null, null,
                null, DiscountType.FIXED, null);

        assertThatThrownBy(() -> service.validateDiscount(dto))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: Se envía discountValue sin discountType.
     * Regla: Tipo y valor del descuento manual deben venir juntos o ninguno.
     * Validación: Lanza BadRequestException por descuento incompleto.
     */
    @Test
    void validateDiscount_whenDiscountValueWithoutType_throwsBadRequestException() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                null, null, null, null, null,
                null, null, new BigDecimal("100"));

        assertThatThrownBy(() -> service.validateDiscount(dto))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: Descuento manual completo (type + value) sin promoción.
     * Validación: No lanza excepción.
     */
    @Test
    void validateDiscount_whenManualDiscountCompleteWithoutPromotion_doesNotThrow() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                null, null, null, null, null,
                null, DiscountType.PERCENTAGE, new BigDecimal("10"));

        assertThatCode(() -> service.validateDiscount(dto))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Solo promoción, sin descuento manual.
     * Validación: No lanza excepción.
     */
    @Test
    void validateDiscount_whenOnlyPromotion_doesNotThrow() {
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, null,
                null, null, null, null, null,
                5L, null, null);

        assertThatCode(() -> service.validateDiscount(dto))
                .doesNotThrowAnyException();
    }

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

    /**
     * CASO: No existe ninguna PrestationInstance IN_PROGRESS para el paciente.
     * Validación: No lanza excepción.
     */
    @Test
    void validatePrestationInstancePending_whenNoInProgressInstances_doesNotThrow() {
        when(prestationInstanceRepository.findPrestationInstanceByPatientAndStatus(5L, PrestationInstanceStatus.IN_PROGRESS))
                .thenReturn(List.of());

        OdontogramRequestDTO odontogramDTO = new OdontogramRequestDTO(Tooth.T11, ToothFace.TOP, 1L, 1L);
        PrestationInstanceRequestDTO dto = new PrestationInstanceRequestDTO(
                10L, null, null, odontogramDTO,
                null, null, null, null, null, null, null, null);

        assertThatCode(() -> service.validatePrestationInstancePending(5L, dto))
                .doesNotThrowAnyException();
    }

    // ─── validateStepAdvancement ─────────────────────────────────────────────

    /**
     * CASO: La prestación pertenece a un paciente distinto al que viene en la request.
     * Validación: Lanza ConflictException (409 — ADR-0026: depende de mirar el estado persistido
     * de la prestación, el mismo input sería válido con otro paciente dueño).
     */
    @Test
    void validateStepAdvancement_whenPatientNotOwner_throwsConflictException() {
        PrestationInstance prestation = mock(PrestationInstance.class, RETURNS_DEEP_STUBS);
        when(prestation.getConsultationInstance().getConsultation().getPatient().getId()).thenReturn(999L);
        when(prestation.getId()).thenReturn(100L);

        PrestationStepAdvancementRequestDTO dto = new PrestationStepAdvancementRequestDTO(
                100L, 200L, PrestationStepStatus.FINISHED);

        assertThatThrownBy(() -> service.validateStepAdvancement(5L, prestation, dto))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Se intenta avanzar un step en una prestación que NO está IN_PROGRESS (ej: COMPLETED).
     * Validación: Lanza ConflictException (409 — ADR-0026: depende de mirar el estado persistido).
     */
    @Test
    void validateStepAdvancement_whenPrestationNotInProgress_throwsConflictException() {
        PrestationInstance prestation = mock(PrestationInstance.class, RETURNS_DEEP_STUBS);
        when(prestation.getConsultationInstance().getConsultation().getPatient().getId()).thenReturn(5L);
        when(prestation.getStatus()).thenReturn(PrestationInstanceStatus.FINISHED);
        when(prestation.getId()).thenReturn(100L);

        PrestationStepAdvancementRequestDTO dto = new PrestationStepAdvancementRequestDTO(
                100L, 200L, PrestationStepStatus.FINISHED);

        assertThatThrownBy(() -> service.validateStepAdvancement(5L, prestation, dto))
                .isInstanceOf(ConflictException.class);
    }

    // ─── buildStepAdvancements ────────────────────────────────────

    /**
     * CASO: El step ya existe con estado IN_PROGRESS y se intenta registrar nuevamente como IN_PROGRESS.
     * Regla: Un step IN_PROGRESS solo puede avanzar a COMPLETED.
     * Validación: Lanza ConflictException por transición de estado inválida.
     */
    @Test
    void buildStepAdvancements_whenInProgressStepNotAdvancedToCompleted_throwsConflictException() {
        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getId()).thenReturn(1L);

        PrestationStepInstance existingInstance = mock(PrestationStepInstance.class);
        when(existingInstance.getStep()).thenReturn(step1);
        when(existingInstance.getStatus()).thenReturn(PrestationStepStatus.IN_PROGRESS);

        // Se intenta registrar el mismo step con IN_PROGRESS en lugar de COMPLETED
        PrestationStepAdvancementRequestDTO dto = new PrestationStepAdvancementRequestDTO(
                100L, 1L, PrestationStepStatus.IN_PROGRESS);

        assertThatThrownBy(() -> service.buildStepAdvancements(
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
    void buildStepAdvancements_whenPreviousStepNotResolved_throwsBadRequestException() {
        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getId()).thenReturn(1L);
        when(step1.getPosition()).thenReturn(1);

        PrestationStep step2 = mock(PrestationStep.class);
        when(step2.getId()).thenReturn(2L);
        when(step2.getPosition()).thenReturn(2);

        // Se envía directamente el step 2 sin que step 1 esté resuelto
        PrestationStepAdvancementRequestDTO dto = new PrestationStepAdvancementRequestDTO(
                100L, 2L, PrestationStepStatus.FINISHED);

        assertThatThrownBy(() -> service.buildStepAdvancements(
                List.of(step1, step2),
                List.of(),
                List.of(dto),
                mock(ConsultationInstance.class),
                mock(PrestationInstance.class)))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: Se intenta omitir un step que está marcado como obligatorio.
     * Regla: Un step required no puede omitirse.
     * Validación: Lanza BadRequestException por intento de omitir un step requerido.
     */
    @Test
    void buildStepAdvancements_whenRequiredStepMarkedAsOmitted_throwsBadRequestException() {
        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getId()).thenReturn(1L);
        when(step1.isRequired()).thenReturn(true);

        PrestationStepAdvancementRequestDTO dto = new PrestationStepAdvancementRequestDTO(
                100L, 1L, PrestationStepStatus.OMITTED);

        assertThatThrownBy(() -> service.buildStepAdvancements(
                List.of(step1),
                List.of(),
                List.of(dto),
                mock(ConsultationInstance.class),
                mock(PrestationInstance.class)))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: El step ya está COMPLETED en base y se intenta volver a registrar.
     * Regla: Un step en estado final no admite nuevos avances.
     * Validación: Lanza ConflictException por re-registro de step final.
     */
    @Test
    void buildStepAdvancements_whenStepAlreadyCompleted_throwsConflictException() {
        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getId()).thenReturn(1L);

        PrestationStepInstance existingInstance = mock(PrestationStepInstance.class);
        when(existingInstance.getStep()).thenReturn(step1);
        when(existingInstance.getStatus()).thenReturn(PrestationStepStatus.FINISHED);

        PrestationStepAdvancementRequestDTO dto = new PrestationStepAdvancementRequestDTO(
                100L, 1L, PrestationStepStatus.FINISHED);

        assertThatThrownBy(() -> service.buildStepAdvancements(
                List.of(step1),
                List.of(existingInstance),
                List.of(dto),
                mock(ConsultationInstance.class),
                mock(PrestationInstance.class)))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: El step ya está OMITTED en base y se intenta volver a registrar.
     * Regla: Un step en estado final no admite nuevos avances.
     * Validación: Lanza ConflictException por re-registro de step final.
     */
    @Test
    void buildStepAdvancements_whenStepAlreadyOmitted_throwsConflictException() {
        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getId()).thenReturn(1L);

        PrestationStepInstance existingInstance = mock(PrestationStepInstance.class);
        when(existingInstance.getStep()).thenReturn(step1);
        when(existingInstance.getStatus()).thenReturn(PrestationStepStatus.OMITTED);

        PrestationStepAdvancementRequestDTO dto = new PrestationStepAdvancementRequestDTO(
                100L, 1L, PrestationStepStatus.FINISHED);

        assertThatThrownBy(() -> service.buildStepAdvancements(
                List.of(step1),
                List.of(existingInstance),
                List.of(dto),
                mock(ConsultationInstance.class),
                mock(PrestationInstance.class)))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Steps 1 y 2 enviados juntos, en orden válido, sin estados previos.
     * Validación: No lanza excepción y construye una instancia por cada step recibido.
     */
    @Test
    void buildStepAdvancements_whenStepsValidAndOrdered_buildsInstancesForEach() {
        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getId()).thenReturn(1L);
        when(step1.getPosition()).thenReturn(1);

        PrestationStep step2 = mock(PrestationStep.class);
        when(step2.getId()).thenReturn(2L);
        when(step2.getPosition()).thenReturn(2);

        PrestationStepAdvancementRequestDTO dtoStep1 = new PrestationStepAdvancementRequestDTO(
                100L, 1L, PrestationStepStatus.FINISHED);
        PrestationStepAdvancementRequestDTO dtoStep2 = new PrestationStepAdvancementRequestDTO(
                100L, 2L, PrestationStepStatus.FINISHED);

        List<PrestationStepInstance> result = service.buildStepAdvancements(
                List.of(step1, step2),
                List.of(),
                List.of(dtoStep1, dtoStep2),
                mock(ConsultationInstance.class),
                mock(PrestationInstance.class));

        assertThat(result).hasSize(2);
    }

    // ─── shouldComplete ──────────────────────────────────────────────────────

    /**
     * CASO: Todos los steps del catálogo tienen una instancia en estado final (COMPLETED u OMITTED).
     * Validación: Retorna true — la prestación puede darse por completada.
     */
    @Test
    void shouldComplete_whenAllStepsResolved_returnsTrue() {
        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getId()).thenReturn(1L);
        PrestationStep step2 = mock(PrestationStep.class);
        when(step2.getId()).thenReturn(2L);

        PrestationStepInstance inst1 = mock(PrestationStepInstance.class);
        when(inst1.getStep()).thenReturn(step1);
        when(inst1.getStatus()).thenReturn(PrestationStepStatus.FINISHED);

        PrestationStepInstance inst2 = mock(PrestationStepInstance.class);
        when(inst2.getStep()).thenReturn(step2);
        when(inst2.getStatus()).thenReturn(PrestationStepStatus.OMITTED);

        boolean result = service.shouldComplete(List.of(step1, step2), List.of(inst1, inst2));

        assertThat(result).isTrue();
    }

    /**
     * CASO: Un step del catálogo no tiene instancia registrada.
     * Validación: Retorna false — la prestación todavía tiene trabajo pendiente.
     */
    @Test
    void shouldComplete_whenStepWithoutInstance_returnsFalse() {
        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getId()).thenReturn(1L);
        PrestationStep step2 = mock(PrestationStep.class);
        when(step2.getId()).thenReturn(2L);

        PrestationStepInstance inst1 = mock(PrestationStepInstance.class);
        when(inst1.getStep()).thenReturn(step1);
        when(inst1.getStatus()).thenReturn(PrestationStepStatus.FINISHED);

        boolean result = service.shouldComplete(List.of(step1, step2), List.of(inst1));

        assertThat(result).isFalse();
    }

    /**
     * CASO: Un step del catálogo tiene una instancia en estado IN_PROGRESS (no final).
     * Validación: Retorna false — la prestación no puede completarse con steps en progreso.
     */
    @Test
    void shouldComplete_whenStepInProgress_returnsFalse() {
        PrestationStep step1 = mock(PrestationStep.class);
        when(step1.getId()).thenReturn(1L);

        PrestationStepInstance inst1 = mock(PrestationStepInstance.class);
        when(inst1.getStep()).thenReturn(step1);
        when(inst1.getStatus()).thenReturn(PrestationStepStatus.IN_PROGRESS);

        boolean result = service.shouldComplete(List.of(step1), List.of(inst1));

        assertThat(result).isFalse();
    }

    // ─── validatePrestationUnique ────────────────────────────────────────────

    /**
     * CASO: PrestationType.isUnique() == true, newCount > 1.
     * Validación: Lanza ConflictException.
     */
    @Test
    void validatePrestationUnique_whenUniqueWithMultipleNew_throwsConflictException() {
        PrestationType prestationType = mock(PrestationType.class);
        when(prestationType.isUnique()).thenReturn(true);
        when(prestationType.getName()).thenReturn("Radiografía");

        assertThatThrownBy(() -> service.validatePrestationUnique(prestationType, 2, true))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: PrestationType.isUnique() == true, newCount == 1, stepAdvancementsEmpty == true.
     * Validación: No lanza excepción.
     */
    @Test
    void validatePrestationUnique_whenUniqueAlone_doesNotThrow() {
        PrestationType prestationType = mock(PrestationType.class);
        when(prestationType.isUnique()).thenReturn(true);

        assertThatCode(() -> service.validatePrestationUnique(prestationType, 1, true))
                .doesNotThrowAnyException();
    }

    // ─── matchOdontogram ──────────────────────────────────────────────────────

    /**
     * CASO: Lista de Odontogram guardados con uno cuyo tooth+toothFace coincide con el DTO.
     * Validación: Retorna el Odontogram correspondiente, sin lanzar ninguna excepción (el método ya no lanza, solo matchea).
     */
    @Test
    void matchOdontogram_whenMatchExists_returnsOdontogram() {
        Odontogram matching = mock(Odontogram.class);
        when(matching.getTooth()).thenReturn(Tooth.T11);
        when(matching.getToothFace()).thenReturn(ToothFace.TOP);

        Odontogram other = mock(Odontogram.class);
        when(other.getTooth()).thenReturn(Tooth.T21);

        OdontogramRequestDTO dto = new OdontogramRequestDTO(Tooth.T11, ToothFace.TOP, 1L, 1L);

        Odontogram result = service.matchOdontogram(List.of(other, matching), dto);

        assertThat(result).isEqualTo(matching);
    }

    /**
     * CASO: Ninguno de los Odontogram guardados coincide.
     * Validación: Retorna null — la decisión de qué excepción lanzar queda en el caller.
     */
    @Test
    void matchOdontogram_whenNoMatch_returnsNull() {
        Odontogram other = mock(Odontogram.class);
        when(other.getTooth()).thenReturn(Tooth.T21);

        OdontogramRequestDTO dto = new OdontogramRequestDTO(Tooth.T11, ToothFace.TOP, 1L, 1L);

        Odontogram result = service.matchOdontogram(List.of(other), dto);

        assertThat(result).isNull();
    }

    // ─── isStepEditable (ADR-0029) ────────────────────────────────────────────

    /**
     * CASO: step.getConsultationInstance().getCreatedAt() + correctionHours es posterior a now().
     * Validación: Retorna true.
     */
    @Test
    void isStepEditable_whenWithinWindow_returnsTrue() {
        ConsultationInstance originConsultationInstance = mock(ConsultationInstance.class);
        when(originConsultationInstance.getCreatedAt()).thenReturn(java.time.LocalDateTime.now().minusHours(1));

        PrestationStepInstance step = mock(PrestationStepInstance.class);
        when(step.getConsultationInstance()).thenReturn(originConsultationInstance);

        boolean result = service.isStepEditable(step, 24L);

        assertThat(result).isTrue();
    }

    /**
     * CASO: step.getConsultationInstance().getCreatedAt() + correctionHours es anterior a now().
     * Validación: Retorna false — el caller decide si eso implica 409 según haya cambio o no.
     */
    @Test
    void isStepEditable_whenOutsideWindow_returnsFalse() {
        ConsultationInstance originConsultationInstance = mock(ConsultationInstance.class);
        when(originConsultationInstance.getCreatedAt()).thenReturn(java.time.LocalDateTime.now().minusHours(48));

        PrestationStepInstance step = mock(PrestationStepInstance.class);
        when(step.getConsultationInstance()).thenReturn(originConsultationInstance);

        boolean result = service.isStepEditable(step, 24L);

        assertThat(result).isFalse();
    }

    // ─── isPrestationInstanceEditable (regla #13, hueco post-diseño) ──────────

    /**
     * CASO: prestationInstance.getCreatedAt() + correctionHours es posterior a now().
     * Validación: Retorna true.
     */
    @Test
    void isPrestationInstanceEditable_whenWithinWindow_returnsTrue() {
        PrestationInstance prestationInstance = mock(PrestationInstance.class);
        when(prestationInstance.getCreatedAt()).thenReturn(java.time.LocalDateTime.now().minusHours(1));

        boolean result = service.isPrestationInstanceEditable(prestationInstance, 24L);

        assertThat(result).isTrue();
    }

    /**
     * CASO: prestationInstance.getCreatedAt() + correctionHours es anterior a now().
     * Validación: Retorna false.
     */
    @Test
    void isPrestationInstanceEditable_whenOutsideWindow_returnsFalse() {
        PrestationInstance prestationInstance = mock(PrestationInstance.class);
        when(prestationInstance.getCreatedAt()).thenReturn(java.time.LocalDateTime.now().minusHours(48));

        boolean result = service.isPrestationInstanceEditable(prestationInstance, 24L);

        assertThat(result).isFalse();
    }

    // ─── validateLocationNotOverridable (regla #17/#27) ──────────────────────

    /**
     * CASO: persisted.getOdontogram() != null y el DTO trae scope/tooth con valor.
     * Regla: Una PrestationInstance con Odontogram asociado no puede recibir campos de ubicación propios
     * — la ubicación se corrige editando el Odontogram, no estos campos.
     * Validación: Lanza ConflictException, 409.
     */
    @Test
    void validateLocationNotOverridable_whenHasOdontogramAndLocationFieldPresent_throwsConflictException() {
        Odontogram linkedOdontogram = mock(Odontogram.class);
        PrestationInstance persisted = mock(PrestationInstance.class);
        when(persisted.getId()).thenReturn(200L);
        when(persisted.getOdontogram()).thenReturn(linkedOdontogram);

        PrestationInstanceUpdateRequestDTO dto = new PrestationInstanceUpdateRequestDTO(
                200L, PrestationScopeType.TOOTH, Tooth.T11, null, null, null, true);

        assertThatThrownBy(() -> service.validateLocationNotOverridable(persisted, dto))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: persisted.getOdontogram() == null, type.isRequiresLocation() == true, y los 5 campos de
     * ubicación del DTO llegan todos null.
     * Regla: Por ADR-0024 el front manda el objeto completo — todo-null es la ubicación real declarada,
     * no ausencia de intención; si la prestación requiere ubicación y no tiene Odontogram, es un estado
     * inválido.
     * Validación: Lanza ConflictException, 409.
     */
    @Test
    void validateLocationNotOverridable_whenNoOdontogramAndRequiresLocationAndAllFieldsNull_throwsConflictException() {
        PrestationType type = mock(PrestationType.class);
        when(type.isRequiresLocation()).thenReturn(true);

        PrestationInstance persisted = mock(PrestationInstance.class);
        when(persisted.getId()).thenReturn(200L);
        when(persisted.getOdontogram()).thenReturn(null);
        when(persisted.getType()).thenReturn(type);

        PrestationInstanceUpdateRequestDTO dto = new PrestationInstanceUpdateRequestDTO(
                200L, null, null, null, null, null, true);

        assertThatThrownBy(() -> service.validateLocationNotOverridable(persisted, dto))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: persisted.getOdontogram() == null, type.isRequiresLocation() == false, y los 5 campos de
     * ubicación del DTO llegan todos null.
     * Validación: No lanza excepción — sin Odontogram y sin requerimiento de ubicación, todo-null es
     * comportamiento normal.
     */
    @Test
    void validateLocationNotOverridable_whenNoOdontogramAndDoesNotRequireLocation_noException() {
        PrestationType type = mock(PrestationType.class);
        when(type.isRequiresLocation()).thenReturn(false);

        PrestationInstance persisted = mock(PrestationInstance.class);
        when(persisted.getOdontogram()).thenReturn(null);
        when(persisted.getType()).thenReturn(type);

        PrestationInstanceUpdateRequestDTO dto = new PrestationInstanceUpdateRequestDTO(
                200L, null, null, null, null, null, true);

        assertThatCode(() -> service.validateLocationNotOverridable(persisted, dto))
                .doesNotThrowAnyException();
    }

    // ─── validateScopeAllowedForType (regla #29) ──────────────────────────────

    /**
     * CASO: El scope recibido está incluido en type.getAllowedScopes().
     * Validación: No lanza excepción.
     */
    @Test
    void validateScopeAllowedForType_whenScopeInAllowedScopes_noException() {
        PrestationType type = mock(PrestationType.class);
        when(type.getAllowedScopes()).thenReturn(Set.of(PrestationScopeType.TOOTH, PrestationScopeType.TOOTH_FACE));

        assertThatCode(() -> service.validateScopeAllowedForType(type, PrestationScopeType.TOOTH))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: El scope recibido NO está incluido en type.getAllowedScopes().
     * Validación: Lanza ConflictException, 409.
     */
    @Test
    void validateScopeAllowedForType_whenScopeNotInAllowedScopes_throwsConflictException() {
        PrestationType type = mock(PrestationType.class);
        when(type.getId()).thenReturn(10L);
        when(type.getAllowedScopes()).thenReturn(Set.of(PrestationScopeType.TOOTH));

        assertThatThrownBy(() -> service.validateScopeAllowedForType(type, PrestationScopeType.QUADRANT))
                .isInstanceOf(ConflictException.class);
    }

    // ─── applyStepAdvancement (edición in-place) ───────────

    /**
     * CASO: existingStep real (no mock opaco) con status=IN_PROGRESS, id=300; dto con status=COMPLETED
     * para el mismo prestationStepId.
     * Regla: el avance de un step ya existente debe editar el registro persistido in-place (ADR-0008),
     * nunca construir uno nuevo — ese camino (PrestationStepInstance.build) es exclusivo de altas.
     * Validación: El mismo objeto existingStep queda con getStatus() == COMPLETED; el método no retorna
     * ni construye ningún PrestationStepInstance nuevo — existingStep.getId() no cambia.
     */
    @Test
    void applyStepAdvancement_whenInProgressAdvancesToCompleted_updatesSameInstanceInPlace() {
        PrestationStep catalogStep = mock(PrestationStep.class);

        PrestationStepInstance existingStep = PrestationStepInstance.build(
                mock(PrestationInstance.class), mock(ConsultationInstance.class), catalogStep, PrestationStepStatus.IN_PROGRESS);
        existingStep.setId(300L);

        PrestationStepAdvancementRequestDTO dto = new PrestationStepAdvancementRequestDTO(100L, 1L, PrestationStepStatus.FINISHED);

        service.applyStepAdvancement(existingStep, dto);

        assertThat(existingStep.getId()).isEqualTo(300L);
        assertThat(existingStep.getStatus()).isEqualTo(PrestationStepStatus.FINISHED);
    }
}