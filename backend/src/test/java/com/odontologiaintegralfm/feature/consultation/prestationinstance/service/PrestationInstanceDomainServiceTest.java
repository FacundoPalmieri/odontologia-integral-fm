package com.odontologiaintegralfm.feature.consultation.prestationinstance.service;

import com.odontologiaintegralfm.feature.consultation.prestation.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.prestation.service.PrestationStepService;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.dto.StepItemDTO;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationStepInstance;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.service.PrestationInstanceDomainService;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.scope.enums.ToothFace;
import com.odontologiaintegralfm.feature.consultation.scope.model.Scope;
import com.odontologiaintegralfm.feature.consultation.scope.service.ScopeDomainService;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.dto.TreatmentInstanceRefDTO;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.model.TreatmentInstance;
import com.odontologiaintegralfm.shared.exception.AppException;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests de las reglas de negocio de PrestationInstanceDomainService (REQ-033).
 *
 * Decisiones de armado (ADR-0031):
 * - Las entidades de entrada (PrestationType, PrestationInstance, PrestationStepInstance, TreatmentInstance)
 *   se mockean con stubs lenient en los helpers: cada test solo declara el dato que activa su rama.
 * - Scope es un valor: se construye real. PrestationStep no tiene build(): se arma real con sus setters.
 * - ScopeDomainService es un @Spy real (sin colaboradores): la geometría de solapamiento, cobertura,
 *   contigüidad y expansión de dientes se ejercita de verdad en vez de stubbearla.
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class PrestationInstanceDomainServiceTest {

    @Mock private PrestationStepService prestationStepService;
    @Spy private ScopeDomainService scopeDomainService = new ScopeDomainService();

    @InjectMocks private PrestationInstanceDomainService service;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static PrestationStep step(long id, int position, boolean required) {
        PrestationStep step = new PrestationStep();
        step.setId(id);
        step.setPosition(position);
        step.setRequired(required);
        return step;
    }

    /**
     * Catálogo de n steps no requeridos (S1..Sn) con id distinto de la posición (101, 102, ... en las posiciones 1, 2, ...),
     * para que una comparación por posición en lugar de por id ponga el test en rojo.
     */
    private static List<PrestationStep> catalog(int n) {
        List<PrestationStep> steps = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            steps.add(step(100L + i, i, false));
        }
        return steps;
    }

    private static PrestationStepInstance stepInstance(Long id, PrestationStep step, PrestationStepStatus status) {
        PrestationStepInstance instance = mock(PrestationStepInstance.class);
        lenient().when(instance.getId()).thenReturn(id);
        lenient().when(instance.getStep()).thenReturn(step);
        lenient().when(instance.getStatus()).thenReturn(status);
        return instance;
    }

    private static PrestationStepInstance stepInstance(PrestationStep step, PrestationStepStatus status) {
        return stepInstance(step.getId() * 10, step, status);
    }

    private static PrestationInstance existingPrestation(PrestationInstanceStatus status) {
        PrestationInstance prestation = mock(PrestationInstance.class);
        lenient().when(prestation.getId()).thenReturn(812L);
        lenient().when(prestation.getStatus()).thenReturn(status);
        return prestation;
    }

    private static PrestationType type(String name, String label, boolean unique, Set<PrestationScopeType> allowedScopes,
                                       Integer maxTooth, boolean requiresOrigin) {
        PrestationType type = mock(PrestationType.class);
        lenient().when(type.getId()).thenReturn((long) Math.abs(name.hashCode()));
        lenient().when(type.getName()).thenReturn(name);
        lenient().when(type.getLabel()).thenReturn(label);
        lenient().when(type.isUnique()).thenReturn(unique);
        lenient().when(type.getAllowedScopes()).thenReturn(allowedScopes);
        lenient().when(type.getMaxTooth()).thenReturn(maxTooth);
        lenient().when(type.isRequiresOriginTreatment()).thenReturn(requiresOrigin);
        return type;
    }

    private static PrestationType simpleType(String name, String label) {
        return type(name, label, false, Set.of(PrestationScopeType.TOOTH), null, false);
    }

    private static Scope teeth(Tooth... teeth) {
        return Scope.build(List.of(teeth), null, null, null, false);
    }

    private static Scope teethWithFace(ToothFace face, Tooth... teeth) {
        return Scope.build(List.of(teeth), face, null, null, false);
    }

    private static Scope quadrant(Quadrant quadrant) {
        return Scope.build(List.of(), null, quadrant, null, false);
    }

    private static Scope maxillary(Maxillary maxillary) {
        return Scope.build(List.of(), null, null, maxillary, false);
    }

    private static TreatmentInstance treatmentAt(Scope scope) {
        TreatmentInstance treatment = mock(TreatmentInstance.class);
        lenient().when(treatment.getScope()).thenReturn(scope);
        return treatment;
    }

    /** Prestación IN_PROGRESS de un tipo, con scope propio y/o tratamiento (ubicación efectiva). */
    private static PrestationInstance prestationAt(PrestationType type, Scope ownScope, TreatmentInstance treatment) {
        return prestationAt(type, ownScope, treatment, PrestationInstanceStatus.IN_PROGRESS);
    }

    /** Prestación de un tipo en el estado indicado, con scope propio y/o tratamiento (ubicación efectiva). */
    private static PrestationInstance prestationAt(PrestationType type, Scope ownScope, TreatmentInstance treatment,
                                                   PrestationInstanceStatus status) {
        PrestationInstance prestation = mock(PrestationInstance.class);
        lenient().when(prestation.getType()).thenReturn(type);
        lenient().when(prestation.getStatus()).thenReturn(status);
        lenient().when(prestation.getScope()).thenReturn(ownScope);
        lenient().when(prestation.getTreatmentInstance()).thenReturn(treatment);
        return prestation;
    }

    private static StepItemDTO newStep(long stepId, PrestationStepStatus status) {
        return new StepItemDTO(null, stepId, status);
    }

    private static StepItemDTO existingStep(long instanceId, PrestationStepStatus status) {
        return new StepItemDTO(instanceId, null, status);
    }

    private static void assertKey(Throwable thrown, String keyFragment) {
        assertThat(((AppException) thrown).getUserMessageKey()).contains(keyFragment);
    }

    // ─── calculatePrestationInstanceStatus ────────────────────────────────────

    /**
     * CASO: Todos los steps del catálogo (S1..S3) quedaron FINISHED, o mezclados con OMITTED.
     * Regla: D-27 — sin REPLACED/CANCELED y con todos resueltos, la prestación es FINISHED.
     * Validación: devuelve FINISHED en ambos escenarios (nunca REPLACED/CANCELED).
     */
    @Test
    void calculatePrestationInstanceStatus_allFinishedOrOmitted_returnsFinished() {
        List<PrestationStep> catalog = catalog(3);

        PrestationInstanceStatus allFinished = service.calculatePrestationInstanceStatus(catalog, List.of(
                stepInstance(catalog.get(0), PrestationStepStatus.FINISHED),
                stepInstance(catalog.get(1), PrestationStepStatus.FINISHED),
                stepInstance(catalog.get(2), PrestationStepStatus.FINISHED)));
        PrestationInstanceStatus withOmitted = service.calculatePrestationInstanceStatus(catalog, List.of(
                stepInstance(catalog.get(0), PrestationStepStatus.FINISHED),
                stepInstance(catalog.get(1), PrestationStepStatus.OMITTED),
                stepInstance(catalog.get(2), PrestationStepStatus.FINISHED)));

        assertThat(allFinished).isEqualTo(PrestationInstanceStatus.FINISHED);
        assertThat(withOmitted).isEqualTo(PrestationInstanceStatus.FINISHED);
    }

    /**
     * CASO: S1 FINISHED, S2 IN_PROGRESS y S3 sin instancia.
     * Regla: D-27 — si falta algún step sin estado terminal, la prestación sigue IN_PROGRESS.
     * Validación: devuelve IN_PROGRESS.
     */
    @Test
    void calculatePrestationInstanceStatus_stepMissingWithoutTerminalState_returnsInProgress() {
        List<PrestationStep> catalog = catalog(3);

        PrestationInstanceStatus result = service.calculatePrestationInstanceStatus(catalog, List.of(
                stepInstance(catalog.get(0), PrestationStepStatus.FINISHED),
                stepInstance(catalog.get(1), PrestationStepStatus.IN_PROGRESS)));

        assertThat(result).isEqualTo(PrestationInstanceStatus.IN_PROGRESS);
    }

    /**
     * CASO: Cadena REPLACED completa — catálogo S1→S4, S1 FINISHED persistido, S2, S3 y S4 REPLACED.
     * Regla: D-27 — el REPLACED es el inmediato siguiente al último resuelto y todos los posteriores lo acompañan.
     * Validación: devuelve REPLACED (ejemplo del req).
     */
    @Test
    void calculatePrestationInstanceStatus_completeReplacedChain_returnsReplaced() {
        List<PrestationStep> catalog = catalog(4);

        PrestationInstanceStatus result = service.calculatePrestationInstanceStatus(catalog, List.of(
                stepInstance(catalog.get(0), PrestationStepStatus.FINISHED),
                stepInstance(catalog.get(1), PrestationStepStatus.REPLACED),
                stepInstance(catalog.get(2), PrestationStepStatus.REPLACED),
                stepInstance(catalog.get(3), PrestationStepStatus.REPLACED)));

        assertThat(result).isEqualTo(PrestationInstanceStatus.REPLACED);
    }

    /**
     * CASO: Misma cadena pero con S2, S3 y S4 CANCELED.
     * Regla: D-26/D-27 — CANCELED es el tercer terminal, con la misma regla de cadena que REPLACED.
     * Validación: devuelve CANCELED.
     */
    @Test
    void calculatePrestationInstanceStatus_completeCanceledChain_returnsCanceled() {
        List<PrestationStep> catalog = catalog(4);

        PrestationInstanceStatus result = service.calculatePrestationInstanceStatus(catalog, List.of(
                stepInstance(catalog.get(0), PrestationStepStatus.FINISHED),
                stepInstance(catalog.get(1), PrestationStepStatus.CANCELED),
                stepInstance(catalog.get(2), PrestationStepStatus.CANCELED),
                stepInstance(catalog.get(3), PrestationStepStatus.CANCELED)));

        assertThat(result).isEqualTo(PrestationInstanceStatus.CANCELED);
    }

    static Stream<Arguments> invalidReplacedOrCanceledChains() {
        return Stream.of(
                Arguments.of("(a) S2 REPLACED y S3 CANCELED mezclados",
                        new PrestationStepStatus[]{PrestationStepStatus.REPLACED, PrestationStepStatus.CANCELED, PrestationStepStatus.CANCELED}),
                Arguments.of("(b) solo S2 REPLACED, sin S3/S4",
                        new PrestationStepStatus[]{PrestationStepStatus.REPLACED, null, null}),
                Arguments.of("(c) S2 y S3 REPLACED con S4 FINISHED",
                        new PrestationStepStatus[]{PrestationStepStatus.REPLACED, PrestationStepStatus.REPLACED, PrestationStepStatus.FINISHED}));
    }

    /**
     * CASO: Cadenas REPLACED/CANCELED inválidas sobre catálogo S1→S4 con S1 FINISHED persistido.
     * Regla: D-27 — un hueco, una mezcla REPLACED/CANCELED o un posterior en otro estado es 409.
     * Validación: ConflictException (409) en los tres escenarios; nunca devuelve un estado.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidReplacedOrCanceledChains")
    void calculatePrestationInstanceStatus_invalidReplacedOrCanceledChain_throwsConflictException(
            String scenario, PrestationStepStatus[] statusesS2toS4) {
        List<PrestationStep> catalog = catalog(4);
        List<PrestationStepInstance> instances = new ArrayList<>();
        instances.add(stepInstance(catalog.get(0), PrestationStepStatus.FINISHED));
        for (int i = 0; i < statusesS2toS4.length; i++) {
            if (statusesS2toS4[i] != null) {
                instances.add(stepInstance(catalog.get(i + 1), statusesS2toS4[i]));
            }
        }

        assertThatThrownBy(() -> service.calculatePrestationInstanceStatus(catalog, instances))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: S1 FINISHED, S2 sin instancia, S3 y S4 REPLACED.
     * Regla: D-27 — el REPLACED tiene que ser el inmediato siguiente al último step resuelto.
     * Validación: ConflictException (409) — hay un hueco en S2.
     */
    @Test
    void calculatePrestationInstanceStatus_replacedNotImmediateAfterLastResolved_throwsConflictException() {
        List<PrestationStep> catalog = catalog(4);

        assertThatThrownBy(() -> service.calculatePrestationInstanceStatus(catalog, List.of(
                stepInstance(catalog.get(0), PrestationStepStatus.FINISHED),
                stepInstance(catalog.get(2), PrestationStepStatus.REPLACED),
                stepInstance(catalog.get(3), PrestationStepStatus.REPLACED))))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Catálogo de un único step con su instancia FINISHED.
     * Regla: D-27 — boundary del catálogo de un solo step.
     * Validación: devuelve FINISHED.
     */
    @Test
    void calculatePrestationInstanceStatus_singleStepCatalogFinished_returnsFinished() {
        List<PrestationStep> catalog = catalog(1);

        PrestationInstanceStatus result = service.calculatePrestationInstanceStatus(catalog, List.of(
                stepInstance(catalog.get(0), PrestationStepStatus.FINISHED)));

        assertThat(result).isEqualTo(PrestationInstanceStatus.FINISHED);
    }

    // ─── validateStepAdvancement ──────────────────────────────────────────────

    /**
     * CASO: Step persistido en estado terminal (FINISHED, OMITTED, REPLACED o CANCELED) al que el request le manda IN_PROGRESS.
     * Regla: D-27 — un step en estado terminal no admite ningún estado nuevo.
     * Validación: ConflictException (409) con la clave de "estado final" (alreadyFinal) en cada estado.
     */
    @ParameterizedTest
    @EnumSource(value = PrestationStepStatus.class, names = {"FINISHED", "OMITTED", "REPLACED", "CANCELED"})
    void validateStepAdvancement_terminalStepReceivesNewStatus_throwsConflictException(PrestationStepStatus persisted) {
        List<PrestationStep> catalog = catalog(1);
        PrestationStepInstance persistedStep = stepInstance(100L, catalog.get(0), persisted);

        assertThatThrownBy(() -> service.validateStepAdvancement(
                existingPrestation(PrestationInstanceStatus.IN_PROGRESS), catalog, List.of(persistedStep),
                List.of(existingStep(100L, PrestationStepStatus.IN_PROGRESS))))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertKey(e, "alreadyFinal"));
    }

    /**
     * CASO: Prestación con status FINISHED y un step nuevo en el request.
     * Regla: D-27 — una prestación terminal no admite avance de steps.
     * Validación: ConflictException (409).
     */
    @Test
    void validateStepAdvancement_terminalPrestation_throwsConflictException() {
        List<PrestationStep> catalog = catalog(2);

        assertThatThrownBy(() -> service.validateStepAdvancement(
                existingPrestation(PrestationInstanceStatus.FINISHED), catalog,
                List.of(stepInstance(catalog.get(0), PrestationStepStatus.FINISHED)),
                List.of(newStep(102L, PrestationStepStatus.FINISHED))))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Catálogo S1→S3 sin nada persistido; el request avanza solo S2 a FINISHED.
     * Regla: D-27 — un step solo avanza si todos los anteriores están resueltos (FINISHED/OMITTED).
     * Validación: ConflictException (409) con la clave previousNotResolved.
     */
    @Test
    void validateStepAdvancement_stepAdvancedWithPreviousUnresolved_throwsConflictException() {
        List<PrestationStep> catalog = catalog(3);

        assertThatThrownBy(() -> service.validateStepAdvancement(
                existingPrestation(PrestationInstanceStatus.IN_PROGRESS), catalog, List.of(),

                List.of(newStep(102L, PrestationStepStatus.FINISHED))))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertKey(e, "previousNotResolved"));
    }

    /**
     * CASO: Catálogo S1→S2; el request trae S1 FINISHED y S2 FINISHED (ambos nuevos, prestación que nace).
     * Regla: D-27 — los anteriores se cuentan desde lo persistido y desde el mismo request.
     * Validación: no lanza.
     */
    @Test
    void validateStepAdvancement_previousResolvedInSameRequest_isAccepted() {
        List<PrestationStep> catalog = catalog(2);

        assertThatCode(() -> service.validateStepAdvancement(null, catalog, List.of(),
                List.of(newStep(101L, PrestationStepStatus.FINISHED), newStep(102L, PrestationStepStatus.FINISHED))))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Step con required = true enviado OMITTED; el mismo caso con required = false.
     * Regla: D-27 — un step requerido no puede quedar OMITTED.
     * Validación: BadRequestException (400) con required = true; no lanza con required = false.
     */
    @Test
    void validateStepAdvancement_skipRequiredStepWithOmitted_throwsBadRequestException() {
        List<PrestationStep> requiredCatalog = List.of(step(101, 1, true));
        List<PrestationStep> optionalCatalog = List.of(step(101, 1, false));
        List<StepItemDTO> items = List.of(newStep(101L, PrestationStepStatus.OMITTED));

        assertThatThrownBy(() -> service.validateStepAdvancement(null, requiredCatalog, List.of(), items))
                .isInstanceOf(BadRequestException.class);
        assertThatCode(() -> service.validateStepAdvancement(null, optionalCatalog, List.of(), items))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Step nuevo (sin id) del primer lugar del catálogo con REPLACED.
     * Regla: D-27 — el primer step no tiene antecesor resuelto del cual ser sucesor.
     * Validación: BadRequestException (400).
     */
    @Test
    void validateStepAdvancement_firstStepBornReplaced_throwsBadRequestException() {
        List<PrestationStep> catalog = catalog(2);

        assertThatThrownBy(() -> service.validateStepAdvancement(null, catalog, List.of(),
                List.of(newStep(101L, PrestationStepStatus.REPLACED))))
                .isInstanceOf(BadRequestException.class);
    }

    /**
     * CASO: Step nuevo del primer lugar con CANCELED, con catálogo de dos steps y con catálogo de un solo step.
     * Regla: D-26/D-27 — el primer step nunca nace CANCELED (incluye el catálogo de un único step).
     * Validación: BadRequestException (400) en ambos.
     */
    @Test
    void validateStepAdvancement_firstStepBornCanceled_throwsBadRequestException() {
        List<StepItemDTO> items = List.of(newStep(101L, PrestationStepStatus.CANCELED));

        assertThatThrownBy(() -> service.validateStepAdvancement(null, catalog(2), List.of(), items))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.validateStepAdvancement(null, catalog(1), List.of(), items))
                .isInstanceOf(BadRequestException.class);
    }

    // ─── validatePrestationStep ───────────────────────────────────────────────

    /**
     * CASO: El catálogo de la prestación contiene el step pedido.
     * Regla: un step nuevo debe pertenecer al catálogo de la prestación.
     * Validación: no lanza.
     */
    @Test
    void validatePrestationStep_stepOfPrestationCatalog_isAccepted() {
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(catalog(3));

        assertThatCode(() -> service.validatePrestationStep(10L, 102L)).doesNotThrowAnyException();
    }

    /**
     * CASO: El step pedido no está en el catálogo de la prestación.
     * Regla: un step nuevo debe pertenecer al catálogo de la prestación (400).
     * Validación: BadRequestException (400) con la clave validatePrestationStep.
     */
    @Test
    void validatePrestationStep_stepNotInCatalog_throwsBadRequestException() {
        when(prestationStepService.findAllPrestationStepsByPrestation(10L)).thenReturn(catalog(3));

        assertThatThrownBy(() -> service.validatePrestationStep(10L, 99L))
                .isInstanceOf(BadRequestException.class)
                .satisfies(e -> assertKey(e, "validatePrestationStep"));
    }

    // ─── validatePrestationUnique ─────────────────────────────────────────────

    /**
     * CASO: Bety manda una Consulta (isUnique) y una Radiografía en el mismo request.
     * Regla: D-36 — un tipo único no convive con ninguna otra prestación.
     * Validación: ConflictException (409) con el label del tipo único ("Consulta") como argumento, no su name
     * ("CONSULTATION"). El name es distinto del label a propósito: con el mismo valor en los dos, el test pasaría
     * aunque producción usara getName().
     */
    @Test
    void validatePrestationUnique_uniqueTypeWithOtherPrestation_throwsConflictException() {
        PrestationType consulta = type("CONSULTATION", "Consulta", true, Set.of(PrestationScopeType.FULL_MOUTH), null, false);
        PrestationType radiografia = simpleType("Radiografía", "Radiografía");

        assertThatThrownBy(() -> service.validatePrestationUnique(List.of(consulta, radiografia)))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((AppException) e).getUserArgs())
                        .contains("Consulta")
                        .doesNotContain("CONSULTATION"));
    }

    /**
     * CASO: Un único ítem de tipo Consulta.
     * Regla: D-36 — el tipo único solo se rechaza si hay más de un ítem.
     * Validación: no lanza.
     */
    @Test
    void validatePrestationUnique_uniqueTypeAlone_isAccepted() {
        PrestationType consulta = type("Consulta", "Consulta", true, Set.of(PrestationScopeType.FULL_MOUTH), null, false);

        assertThatCode(() -> service.validatePrestationUnique(List.of(consulta))).doesNotThrowAnyException();
    }

    /**
     * CASO: Dos ítems, ninguno de tipo isUnique.
     * Regla: D-36 — sin tipos únicos la regla nunca aplica.
     * Validación: no lanza.
     */
    @Test
    void validatePrestationUnique_noUniqueTypes_isAccepted() {
        assertThatCode(() -> service.validatePrestationUnique(List.of(
                simpleType("Radiografía", "Radiografía"), simpleType("Limpieza", "Limpieza"))))
                .doesNotThrowAnyException();
    }

    // ─── validateScopeAllowedForType ──────────────────────────────────────────

    /**
     * CASO: allowedScopes = {MAXILLARY, FULL_MOUTH} y ubicación efectiva maxillary = UPPER.
     * Regla: D-07 — el nivel de la ubicación (Scope.level()) debe estar en allowedScopes.
     * Validación: no lanza.
     */
    @Test
    void validateScopeAllowedForType_levelAllowed_isAccepted() {
        PrestationType blanqueamiento = type("WHITENING", "Blanqueamiento",
                false, Set.of(PrestationScopeType.MAXILLARY, PrestationScopeType.FULL_MOUTH), null, false);

        assertThatCode(() -> service.validateScopeAllowedForType(blanqueamiento, maxillary(Maxillary.UPPER)))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Recementado (allowedScopes = {TOOTH, TOOTH_FACE}) con ubicación efectiva quadrant = UPPER_RIGHT.
     * Regla: D-07 — un nivel que el catálogo no admite se rechaza.
     * Validación: ConflictException (409) con el label de la prestación como argumento.
     */
    @Test
    void validateScopeAllowedForType_levelNotAllowed_throwsConflictException() {
        PrestationType recementado = type("RECEMENTING", "Recementado",
                false, Set.of(PrestationScopeType.TOOTH, PrestationScopeType.TOOTH_FACE), null, false);

        assertThatThrownBy(() -> service.validateScopeAllowedForType(recementado, quadrant(Quadrant.UPPER_RIGHT)))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((AppException) e).getUserArgs()).contains("Recementado"));
    }

    /**
     * CASO: allowedScopes = {TOOTH_FACE} con ubicación diente + cara; y allowedScopes = {TOOTH} con la misma ubicación.
     * Regla: D-03/D-07 — diente + cara es nivel TOOTH_FACE, distinto de TOOTH.
     * Validación: no lanza con {TOOTH_FACE}; ConflictException (409) con {TOOTH}.
     */
    @Test
    void validateScopeAllowedForType_toothFaceAllowedByToothFaceEntry_isAccepted() {
        Scope toothAndFace = teethWithFace(ToothFace.TOP, Tooth.T16);
        PrestationType onlyFace = type("FILLING", "Obturación", false, Set.of(PrestationScopeType.TOOTH_FACE), null, false);
        PrestationType onlyTooth = type("CLEANING_TOOTH", "Limpieza de diente", false, Set.of(PrestationScopeType.TOOTH), null, false);

        assertThatCode(() -> service.validateScopeAllowedForType(onlyFace, toothAndFace)).doesNotThrowAnyException();
        assertThatThrownBy(() -> service.validateScopeAllowedForType(onlyTooth, toothAndFace))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: allowedScopes no vacío y ubicación efectiva null (prestación con solo origen).
     * Regla: D-07 (ampliación de diseño) — una prestación con ubicación no puede quedar sin ella.
     * Validación: ConflictException (409).
     */
    @Test
    void validateScopeAllowedForType_noEffectiveLocationWithAllowedScopes_throwsConflictException() {
        PrestationType agregado = type("ADDITION", "Agregado", false, Set.of(PrestationScopeType.TOOTH), null, true);

        assertThatThrownBy(() -> service.validateScopeAllowedForType(agregado, null))
                .isInstanceOf(ConflictException.class);
    }

    // ─── validateMaxTooth ─────────────────────────────────────────────────────

    /**
     * CASO: maxTooth = 2, nivel TOOTH, 3 dientes.
     * Regla: D-29 — el techo de dientes aplica cuando la ubicación es por diente.
     * Validación: ConflictException (409) con el label de la prestación como argumento.
     */
    @Test
    void validateMaxTooth_exceedsAllowedForToothScope_throwsConflictException() {
        PrestationType puente = type("BRIDGE", "Puente", false, Set.of(PrestationScopeType.TOOTH), 2, false);

        assertThatThrownBy(() -> service.validateMaxTooth(puente, teeth(Tooth.T14, Tooth.T15, Tooth.T16)))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((AppException) e).getUserArgs()).contains("Puente"));
    }

    /**
     * CASO: maxTooth = 2 con exactamente 2 dientes.
     * Regla: D-29 — el techo es inclusivo.
     * Validación: no lanza.
     */
    @Test
    void validateMaxTooth_equalToMax_isAccepted() {
        PrestationType puente = type("BRIDGE", "Puente", false, Set.of(PrestationScopeType.TOOTH), 2, false);

        assertThatCode(() -> service.validateMaxTooth(puente, teeth(Tooth.T14, Tooth.T15))).doesNotThrowAnyException();
    }

    /**
     * CASO: maxTooth = null con 10 dientes.
     * Regla: D-29 — null significa sin techo.
     * Validación: no lanza.
     */
    @Test
    void validateMaxTooth_nullMax_isAccepted() {
        PrestationType sinTecho = type("SCALING", "Sarro", false, Set.of(PrestationScopeType.TOOTH), null, false);
        Scope tenTeeth = teeth(Tooth.T11, Tooth.T12, Tooth.T13, Tooth.T14, Tooth.T15,
                Tooth.T16, Tooth.T17, Tooth.T21, Tooth.T22, Tooth.T23);

        assertThatCode(() -> service.validateMaxTooth(sinTecho, tenTeeth)).doesNotThrowAnyException();
    }

    /**
     * CASO: maxTooth = 1 con una ubicación armada con dientes [11, 12, 13] y quadrant UPPER_RIGHT a la vez.
     * Regla: D-29 — un nivel geométrico (QUADRANT) no tiene cantidad de dientes exigible; el nivel se deriva del
     * grupo geométrico aunque la lista de dientes también esté poblada.
     * Validación: no lanza. Bug que lo pone en rojo: falta el guard por nivel y se cuentan los 3 dientes contra maxTooth = 1.
     */
    @Test
    void validateMaxTooth_geometricScope_isNotApplied() {
        PrestationType limitado = type("CLEANING", "Limpieza", false, Set.of(PrestationScopeType.QUADRANT), 1, false);
        Scope teethAndQuadrant = Scope.build(List.of(Tooth.T11, Tooth.T12, Tooth.T13), null, Quadrant.UPPER_RIGHT, null, false);

        assertThatCode(() -> service.validateMaxTooth(limitado, teethAndQuadrant)).doesNotThrowAnyException();
    }

    // ─── validateSameMaxillary ────────────────────────────────────────────────

    /**
     * CASO: maxTooth = 3 con dientes [16, 36] (maxilares distintos).
     * Regla: D-11 — con maxTooth > 1 todos los dientes son del mismo maxilar.
     * Validación: ConflictException (409).
     */
    @Test
    void validateSameMaxillary_mixedMaxillaryTeeth_throwsConflictException() {
        PrestationType type = type("CROWN", "Corona", false, Set.of(PrestationScopeType.TOOTH), 3, false);

        assertThatThrownBy(() -> service.validateSameMaxillary(type, teeth(Tooth.T16, Tooth.T36)))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: maxTooth = 3 con dientes [14, 17] (salteados, mismo maxilar).
     * Regla: D-11 — no se exige contigüidad.
     * Validación: no lanza.
     */
    @Test
    void validateSameMaxillary_sameMaxillaryNotContiguous_isAccepted() {
        PrestationType type = type("CROWN", "Corona", false, Set.of(PrestationScopeType.TOOTH), 3, false);

        assertThatCode(() -> service.validateSameMaxillary(type, teeth(Tooth.T14, Tooth.T17))).doesNotThrowAnyException();
    }

    /**
     * CASO: maxTooth = 1 y maxTooth = null, con dientes de maxilares distintos.
     * Regla: D-11 — la regla solo aplica con maxTooth > 1.
     * Validación: no lanza en ninguno de los dos.
     */
    @Test
    void validateSameMaxillary_maxToothOneOrNull_isNotApplied() {
        Scope mixed = teeth(Tooth.T16, Tooth.T36);
        PrestationType maxOne = type("A", "A", false, Set.of(PrestationScopeType.TOOTH), 1, false);
        PrestationType maxNull = type("B", "B", false, Set.of(PrestationScopeType.TOOTH), null, false);

        assertThatCode(() -> service.validateSameMaxillary(maxOne, mixed)).doesNotThrowAnyException();
        assertThatCode(() -> service.validateSameMaxillary(maxNull, mixed)).doesNotThrowAnyException();
    }

    // ─── validateSplintingContiguity ──────────────────────────────────────────

    /**
     * CASO: Ferulización (SPLINTING) con dientes [13, 15] (hay un hueco en el 14).
     * Regla: D-59 — Ferulización exige dientes contiguos.
     * Validación: ConflictException (409) con el label como argumento.
     */
    @Test
    void validateSplintingContiguity_nonContiguousTeeth_throwsConflictException() {
        PrestationType splinting = type("SPLINTING", "Ferulización", false, Set.of(PrestationScopeType.TOOTH), 6, false);

        assertThatThrownBy(() -> service.validateSplintingContiguity(splinting, teeth(Tooth.T13, Tooth.T15)))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((AppException) e).getUserArgs()).contains("Ferulización"));
    }

    /**
     * CASO: SPLINTING con dientes [12, 11, 21, 22] (cruzan la línea media).
     * Regla: D-59 — la contigüidad cruza la línea media: 11 y 21 son contiguos.
     * Validación: no lanza.
     */
    @Test
    void validateSplintingContiguity_contiguousTeethCrossingMidline_isAccepted() {
        PrestationType splinting = type("SPLINTING", "Ferulización", false, Set.of(PrestationScopeType.TOOTH), 6, false);

        assertThatCode(() -> service.validateSplintingContiguity(splinting,
                teeth(Tooth.T12, Tooth.T11, Tooth.T21, Tooth.T22))).doesNotThrowAnyException();
    }

    /**
     * CASO: Tipo distinto de SPLINTING con dientes salteados.
     * Regla: D-59 — la contigüidad es exclusiva de Ferulización.
     * Validación: no lanza.
     */
    @Test
    void validateSplintingContiguity_otherPrestationType_isNotApplied() {
        PrestationType crown = type("CROWN", "Corona", false, Set.of(PrestationScopeType.TOOTH), 6, false);

        assertThatCode(() -> service.validateSplintingContiguity(crown, teeth(Tooth.T13, Tooth.T15)))
                .doesNotThrowAnyException();
    }

    // ─── validatePrestationWithoutOwnLocation ─────────────────────────────────

    /**
     * CASO: Rebasado (allowedScopes vacío) con origen presente pero con scope propio con quadrant.
     * Regla: D-50 — una prestación sin ubicación propia no declara scope propio.
     * Validación: ConflictException (409) con la clave de "no debe declararse scope propio" (scopePopulated).
     */
    @Test
    void validatePrestationWithoutOwnLocation_scopePopulated_throwsConflictException() {
        PrestationType rebasado = type("RELINING", "Rebasado", false, Set.of(), null, false);

        assertThatThrownBy(() -> service.validatePrestationWithoutOwnLocation(
                rebasado, quadrant(Quadrant.UPPER_RIGHT), null, new TreatmentInstanceRefDTO(145L, null)))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertKey(e, "scopePopulated"));
    }

    /**
     * CASO: allowedScopes vacío y sin origen.
     * Regla: D-50 — estas prestaciones exigen originTreatmentInstanceRef.
     * Validación: ConflictException (409) con la clave de "debe referenciar un origen" (originMissing).
     */
    @Test
    void validatePrestationWithoutOwnLocation_originMissing_throwsConflictException() {
        PrestationType rebasado = type("RELINING", "Rebasado", false, Set.of(), null, false);

        assertThatThrownBy(() -> service.validatePrestationWithoutOwnLocation(
                rebasado, Scope.empty(), null, null))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertKey(e, "originMissing"));
    }

    /**
     * CASO: allowedScopes vacío, con treatmentInstanceRef y con origen.
     * Regla: D-50 (ampliación de diseño) — estas prestaciones no declaran TreatmentInstance.
     * Validación: ConflictException (409).
     */
    @Test
    void validatePrestationWithoutOwnLocation_treatmentInstanceRefPopulated_throwsConflictException() {
        PrestationType rebasado = type("RELINING", "Rebasado", false, Set.of(), null, false);

        assertThatThrownBy(() -> service.validatePrestationWithoutOwnLocation(
                rebasado, Scope.empty(), new TreatmentInstanceRefDTO(null, "tmp-1"), new TreatmentInstanceRefDTO(145L, null)))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: allowedScopes vacío con solo el origen poblado.
     * Regla: D-45/D-50 — el origen es la única referencia de estas prestaciones.
     * Validación: no lanza.
     */
    @Test
    void validatePrestationWithoutOwnLocation_onlyOriginPopulated_isAccepted() {
        PrestationType rebasado = type("RELINING", "Rebasado", false, Set.of(), null, false);

        assertThatCode(() -> service.validatePrestationWithoutOwnLocation(
                rebasado, Scope.empty(), null, new TreatmentInstanceRefDTO(145L, null)))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: allowedScopes = {TOOTH} con scope propio.
     * Regla: D-50 — la regla solo aplica con allowedScopes vacío.
     * Validación: no lanza.
     */
    @Test
    void validatePrestationWithoutOwnLocation_typeWithLocation_isNotApplied() {
        PrestationType withLocation = type("FILLING", "Obturación", false, Set.of(PrestationScopeType.TOOTH), null, false);

        assertThatCode(() -> service.validatePrestationWithoutOwnLocation(
                withLocation, teeth(Tooth.T16), null, null))
                .doesNotThrowAnyException();
    }

    // ─── validateOriginRequired ───────────────────────────────────────────────

    /**
     * CASO: Agregado (allowedScopes no vacío, requiresOriginTreatment = true) sin origen.
     * Regla: D-41 — el marcador de catálogo exige el origen.
     * Validación: ConflictException (409) con la clave de "requiere una asociación explícita al tratamiento de colocación" (requiresOrigin).
     */
    @Test
    void validateOriginRequired_requiresOriginFlagWithoutOrigin_throwsConflictException() {
        PrestationType agregado = type("ADDITION", "Agregado", false, Set.of(PrestationScopeType.TOOTH), null, true);

        assertThatThrownBy(() -> service.validateOriginRequired(agregado, null))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertKey(e, "requiresOrigin"));
    }

    /**
     * CASO: allowedScopes vacío, flag false y origen null.
     * Regla: D-41/D-50 — sin ubicación propia el origen es obligatorio aunque el flag sea false.
     * Validación: ConflictException (409).
     */
    @Test
    void validateOriginRequired_emptyAllowedScopesWithoutOrigin_throwsConflictException() {
        PrestationType rebasado = type("RELINING", "Rebasado", false, Set.of(), null, false);

        assertThatThrownBy(() -> service.validateOriginRequired(rebasado, null))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Restauración (allowedScopes no vacío, flag false) sin origen.
     * Regla: D-41 — ni scopes vacíos ni flag: el origen no se exige.
     * Validación: no lanza.
     */
    @Test
    void validateOriginRequired_neitherEmptyScopesNorFlag_isAccepted() {
        PrestationType restauracion = type("FILLING", "Restauración", false, Set.of(PrestationScopeType.TOOTH), null, false);

        assertThatCode(() -> service.validateOriginRequired(restauracion, null)).doesNotThrowAnyException();
    }

    /**
     * CASO: Flag true con origen presente.
     * Regla: D-41 — con el origen poblado la exigencia se cumple.
     * Validación: no lanza.
     */
    @Test
    void validateOriginRequired_flagWithOrigin_isAccepted() {
        PrestationType agregado = type("ADDITION", "Agregado", false, Set.of(PrestationScopeType.TOOTH), null, true);

        assertThatCode(() -> service.validateOriginRequired(agregado, mock(TreatmentInstance.class)))
                .doesNotThrowAnyException();
    }

    // ─── validatePrestationInstancePending ────────────────────────────────────

    /**
     * CASO: Limpieza nueva en el diente 16; en base hay otra Limpieza IN_PROGRESS con maxillary = UPPER.
     * Regla: D-37 — mismo tipo con ubicación solapada (el 16 está dentro del maxilar superior) es 409.
     * Validación: ConflictException (409).
     */
    @Test
    void validatePrestationInstancePending_sameTypeOverlappingLocation_throwsConflictException() {
        PrestationType limpieza = simpleType("CLEANING", "Limpieza");
        PrestationInstance nueva = prestationAt(limpieza, teeth(Tooth.T16), null);
        PrestationInstance enBase = prestationAt(limpieza, maxillary(Maxillary.UPPER), null);

        assertThatThrownBy(() -> service.validatePrestationInstancePending(List.of(nueva), List.of(enBase)))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Limpieza nueva en el diente 16 que terminó FINISHED, REPLACED o CANCELED; en la lista de IN_PROGRESS hay otra
     * Limpieza IN_PROGRESS con maxillary = UPPER (solapada).
     * Regla: D-37 — solo se evalúan las prestaciones nuevas que terminan IN_PROGRESS; una que termina cerrada no queda
     * viva y no choca.
     * Validación: no lanza en ninguno de los tres. Si el método no mirara el estado de la nueva, daría 409.
     */
    @ParameterizedTest
    @EnumSource(value = PrestationInstanceStatus.class, names = {"FINISHED", "REPLACED", "CANCELED"})
    void validatePrestationInstancePending_newPrestationEndedTerminal_isNotEvaluated(PrestationInstanceStatus terminal) {
        PrestationType limpieza = simpleType("CLEANING", "Limpieza");
        PrestationInstance nueva = prestationAt(limpieza, teeth(Tooth.T16), null, terminal);
        PrestationInstance enBase = prestationAt(limpieza, maxillary(Maxillary.UPPER), null);

        assertThatCode(() -> service.validatePrestationInstancePending(List.of(nueva), List.of(enBase)))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Misma Limpieza nueva en el 16; la otra está en maxillary = LOWER.
     * Regla: D-37 — ubicaciones que no se solapan conviven.
     * Validación: no lanza.
     */
    @Test
    void validatePrestationInstancePending_sameTypeNonOverlappingLocation_isAccepted() {
        PrestationType limpieza = simpleType("CLEANING", "Limpieza");
        PrestationInstance nueva = prestationAt(limpieza, teeth(Tooth.T16), null);
        PrestationInstance enBase = prestationAt(limpieza, maxillary(Maxillary.LOWER), null);

        assertThatCode(() -> service.validatePrestationInstancePending(List.of(nueva), List.of(enBase)))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Dos Radiografías nuevas del diente 24, ambas IN_PROGRESS en la lista que se pasa.
     * Regla: D-37 — lo que nace en el mismo request también cuenta: dos nuevas solapadas chocan entre sí.
     * Validación: ConflictException (409).
     */
    @Test
    void validatePrestationInstancePending_twoNewSameTypeOverlappingInSameRequest_throwsConflictException() {
        PrestationType radiografia = simpleType("XRAY", "Radiografía");
        PrestationInstance primera = prestationAt(radiografia, teeth(Tooth.T24), null);
        PrestationInstance segunda = prestationAt(radiografia, teeth(Tooth.T24), null);

        assertThatThrownBy(() -> service.validatePrestationInstancePending(
                List.of(primera, segunda), List.of(primera, segunda)))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Implante nuevo y Radiografía de control IN_PROGRESS sobre el mismo diente.
     * Regla: D-37 — el solapamiento solo cuenta entre prestaciones del mismo tipo.
     * Validación: no lanza.
     */
    @Test
    void validatePrestationInstancePending_differentTypeSameLocation_isAccepted() {
        PrestationInstance implante = prestationAt(simpleType("IMPLANT", "Implante"), teeth(Tooth.T16), null);
        PrestationInstance control = prestationAt(simpleType("XRAY", "Radiografía"), teeth(Tooth.T16), null);

        assertThatCode(() -> service.validatePrestationInstancePending(List.of(implante), List.of(control)))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: La prestación nueva aparece también en la lista de IN_PROGRESS.
     * Regla: D-37 — la prestación evaluada se excluye por identidad y no choca consigo misma.
     * Validación: no lanza.
     */
    @Test
    void validatePrestationInstancePending_newPrestationExcludesItself_isAccepted() {
        PrestationInstance nueva = prestationAt(simpleType("CLEANING", "Limpieza"), teeth(Tooth.T16), null);

        assertThatCode(() -> service.validatePrestationInstancePending(List.of(nueva), List.of(nueva)))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Las dos prestaciones sin scope propio; su ubicación sale del scope del TreatmentInstance y se solapa.
     * Regla: D-37 — la ubicación efectiva es el scope propio o el del tratamiento referenciado.
     * Validación: ConflictException (409).
     */
    @Test
    void validatePrestationInstancePending_locationFromTreatmentInstanceScope_throwsConflictException() {
        PrestationType restauracion = simpleType("FILLING", "Restauración");
        PrestationInstance nueva = prestationAt(restauracion, Scope.empty(), treatmentAt(teeth(Tooth.T16)));
        PrestationInstance enBase = prestationAt(restauracion, Scope.empty(), treatmentAt(quadrant(Quadrant.UPPER_RIGHT)));

        assertThatThrownBy(() -> service.validatePrestationInstancePending(List.of(nueva), List.of(enBase)))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Prestación nueva solo con origen (sin ubicación efectiva), con otra del mismo tipo IN_PROGRESS en base.
     * Regla: D-37 — una prestación sin ubicación no tiene con qué solaparse.
     * Validación: no lanza.
     */
    @Test
    void validatePrestationInstancePending_prestationWithoutLocation_isSkipped() {
        PrestationType rebasado = type("RELINING", "Rebasado", false, Set.of(), null, false);
        PrestationInstance nueva = prestationAt(rebasado, Scope.empty(), null);
        PrestationInstance enBase = prestationAt(rebasado, Scope.empty(), null);

        assertThatCode(() -> service.validatePrestationInstancePending(List.of(nueva), List.of(enBase)))
                .doesNotThrowAnyException();
    }

    // ─── validatePrestationInstanceReplacementTraceability ────────────────────

    /**
     * CASO: Prestación de scope propio tooth [16] cerrada REPLACED; nace un TreatmentInstance nuevo en el 16.
     * Regla: D-40 Caso D — algún nuevo del request tiene que cubrir la ubicación reemplazada.
     * Validación: no lanza.
     */
    @Test
    void validatePrestationInstanceReplacementTraceability_ownScopeReplacedCoveredByNewTreatment_isAccepted() {
        PrestationInstance replaced = prestationAt(simpleType("FILLING", "Restauración"), teeth(Tooth.T16), null);

        assertThatCode(() -> service.validatePrestationInstanceReplacementTraceability(
                List.of(replaced), List.of(treatmentAt(teeth(Tooth.T16))), List.of()))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Prestación tooth [16] cerrada REPLACED; los nuevos están solo sobre el diente 26.
     * Regla: D-40 — el reemplazo nunca puede ser una ubicación que no toque el diente reemplazado.
     * Validación: ConflictException (409) con el label de la prestación como argumento.
     */
    @Test
    void validatePrestationInstanceReplacementTraceability_ownScopeReplacedWithoutCoveringNew_throwsConflictException() {
        PrestationInstance replaced = prestationAt(simpleType("FILLING", "Restauración"), teeth(Tooth.T16), null);

        assertThatThrownBy(() -> service.validatePrestationInstanceReplacementTraceability(
                List.of(replaced), List.of(treatmentAt(teeth(Tooth.T26))), List.of()))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((AppException) e).getUserArgs()).contains("Restauración"));
    }

    /**
     * CASO: Prestación tooth [16] REPLACED; nace una prestación de scope propio con quadrant = UPPER_RIGHT.
     * Regla: D-40 — una ubicación más amplia que incluye el 16 cubre el reemplazo.
     * Validación: no lanza.
     */
    @Test
    void validatePrestationInstanceReplacementTraceability_ownScopeReplacedCoveredByNewOwnScopePrestation_isAccepted() {
        PrestationInstance replaced = prestationAt(simpleType("FILLING", "Restauración"), teeth(Tooth.T16), null);
        PrestationInstance nueva = prestationAt(simpleType("CLEANING", "Limpieza"), quadrant(Quadrant.UPPER_RIGHT), null);

        assertThatCode(() -> service.validatePrestationInstanceReplacementTraceability(
                List.of(replaced), List.of(), List.of(nueva)))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Prestación solo-origen (sin ubicación efectiva) cerrada REPLACED y ninguna entidad nueva.
     * Regla: D-40 precisión (a) — sin ubicación efectiva no hay nada que cubrir.
     * Validación: no lanza.
     */
    @Test
    void validatePrestationInstanceReplacementTraceability_replacedPrestationWithoutLocation_isSkipped() {
        PrestationInstance replaced = prestationAt(type("RELINING", "Rebasado", false, Set.of(), null, false), Scope.empty(), null);

        assertThatCode(() -> service.validatePrestationInstanceReplacementTraceability(
                List.of(replaced), List.of(), List.of()))
                .doesNotThrowAnyException();
    }

    // ─── resolveLocation ──────────────────────────────────────────────────────

    /**
     * CASO: Prestación con scope propio.
     * Regla: D-37 — la ubicación efectiva es el scope propio si no está vacío.
     * Validación: devuelve ese mismo Scope.
     */
    @Test
    void resolveLocation_ownScope_returnsOwnScope() {
        Scope own = teeth(Tooth.T24);

        assertThat(service.resolveLocation(own, null)).isSameAs(own);
    }

    /**
     * CASO: Prestación sin scope propio, con TreatmentInstance.
     * Regla: D-37 — sin scope propio, la ubicación es la del tratamiento referenciado.
     * Validación: devuelve el scope del tratamiento.
     */
    @Test
    void resolveLocation_treatmentInstanceScope_returnsTreatmentScope() {
        Scope treatmentScope = teeth(Tooth.T16);

        assertThat(service.resolveLocation(Scope.empty(), treatmentAt(treatmentScope))).isSameAs(treatmentScope);
    }

    /**
     * CASO: Prestación solo con origen (sin scope propio ni tratamiento).
     * Regla: D-37/D-45 — sin ubicación efectiva.
     * Validación: devuelve null.
     */
    @Test
    void resolveLocation_originOnly_returnsNull() {
        assertThat(service.resolveLocation(null, null)).isNull();
    }

    // ─── validateStepAdvancement (casos agregados en checkpoint y revisión de diseño) ──

    /**
     * CASO: Catálogo S1→S4, S1 FINISHED persistido; el request manda S2, S3 y S4 en REPLACED.
     * Regla: D-27 — el step anterior declarado REPLACED en el mismo request es antecedente válido del siguiente.
     * Validación: no lanza; la misma secuencia con S3 en FINISHED lanza ConflictException (409).
     */
    @Test
    void validateStepAdvancement_replacedChainFollowingSameRequestStep_isAccepted() {
        List<PrestationStep> catalog = catalog(4);
        PrestationInstance prestation = existingPrestation(PrestationInstanceStatus.IN_PROGRESS);
        List<PrestationStepInstance> persisted = List.of(stepInstance(catalog.get(0), PrestationStepStatus.FINISHED));

        assertThatCode(() -> service.validateStepAdvancement(prestation, catalog, persisted, List.of(
                newStep(102L, PrestationStepStatus.REPLACED),
                newStep(103L, PrestationStepStatus.REPLACED),
                newStep(104L, PrestationStepStatus.REPLACED))))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> service.validateStepAdvancement(prestation, catalog, persisted, List.of(

                newStep(102L, PrestationStepStatus.REPLACED),
                newStep(103L, PrestationStepStatus.FINISHED),
                newStep(104L, PrestationStepStatus.REPLACED))))
                .isInstanceOf(ConflictException.class);
    }

    // ─── resolveLocation (checkpoint) ─────────────────────────────────────────

    /**
     * CASO: Scope.empty() propio (como queda persistido) y sin tratamiento.
     * Regla: D-37 — un scope vacío cuenta como "sin scope propio".
     * Validación: devuelve null, no un Scope vacío.
     */
    @Test
    void resolveLocation_emptyOwnScopeAndNoTreatment_returnsNull() {
        assertThat(service.resolveLocation(Scope.empty(), null)).isNull();
    }

    // ─── validateStepAdvancement (revisión de diseño) ─────────────────────────

    /**
     * CASO: Step persistido IN_PROGRESS; el request le manda FINISHED, REPLACED o CANCELED.
     * Regla: D-27 — un step empezado puede cerrarse o abandonarse.
     * Validación: no lanza en ninguno.
     */
    @ParameterizedTest
    @EnumSource(value = PrestationStepStatus.class, names = {"FINISHED", "REPLACED", "CANCELED"})
    void validateStepAdvancement_inProgressStepReceivesFinishedReplacedOrCanceled_isAccepted(PrestationStepStatus requested) {
        List<PrestationStep> catalog = catalog(2);
        PrestationStepInstance persisted = stepInstance(100L, catalog.get(0), PrestationStepStatus.IN_PROGRESS);

        assertThatCode(() -> service.validateStepAdvancement(
                existingPrestation(PrestationInstanceStatus.IN_PROGRESS), catalog, List.of(persisted),
                List.of(existingStep(100L, requested))))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Step persistido IN_PROGRESS; el request le manda IN_PROGRESS u OMITTED (step no requerido, para aislar el 409 del 400).
     * Regla: D-27 — un step IN_PROGRESS solo admite FINISHED, REPLACED o CANCELED.
     * Validación: ConflictException (409) en ambos.
     */
    @ParameterizedTest
    @EnumSource(value = PrestationStepStatus.class, names = {"IN_PROGRESS", "OMITTED"})
    void validateStepAdvancement_inProgressStepReceivesInProgressOrOmitted_throwsConflictException(PrestationStepStatus requested) {
        List<PrestationStep> catalog = catalog(2);
        PrestationStepInstance persisted = stepInstance(100L, catalog.get(0), PrestationStepStatus.IN_PROGRESS);

        assertThatThrownBy(() -> service.validateStepAdvancement(
                existingPrestation(PrestationInstanceStatus.IN_PROGRESS), catalog, List.of(persisted),
                List.of(existingStep(100L, requested))))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Prestación existente que ya tiene instancia del step de catálogo 3; el request manda un step nuevo {stepId: 3}.
     * Regla: D-27 — un step nuevo cuyo stepId ya tiene instancia persistida en la prestación es duplicado.
     * Validación: ConflictException (409) con la clave duplicateStep de HEAD.
     */
    @Test
    void validateStepAdvancement_newStepAlreadyPersisted_throwsConflictException() {
        List<PrestationStep> catalog = List.of(step(3, 1, false));
        PrestationStepInstance persisted = stepInstance(500L, catalog.get(0), PrestationStepStatus.IN_PROGRESS);

        assertThatThrownBy(() -> service.validateStepAdvancement(
                existingPrestation(PrestationInstanceStatus.IN_PROGRESS), catalog, List.of(persisted),
                List.of(newStep(3L, PrestationStepStatus.FINISHED))))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertKey(e, "duplicateStep"));
    }

    // ─── Casos agregados por la auditoría (filas 65 a 75) ─────────────────────

    /**
     * CASO: Catálogo S1..S3 con solo S1 FINISHED; S2 y S3 sin instancia.
     * Regla: D-27 — la prestación se calcula contra el catálogo completo, no solo contra las instancias existentes.
     * Validación: devuelve IN_PROGRESS. Bug que lo pone en rojo: calcular solo sobre las instancias existentes y cerrar
     * FINISHED con S2 y S3 sin hacer.
     */
    @Test
    void calculatePrestationInstanceStatus_onlyFirstStepFinishedRestMissing_returnsInProgress() {
        List<PrestationStep> catalog = catalog(3);

        PrestationInstanceStatus result = service.calculatePrestationInstanceStatus(catalog, List.of(
                stepInstance(catalog.get(0), PrestationStepStatus.FINISHED)));

        assertThat(result).isEqualTo(PrestationInstanceStatus.IN_PROGRESS);
    }

    /**
     * CASO: Catálogo S1..S4 con los cuatro steps en REPLACED (se empezó el primero y se abandona).
     * Regla: D-27 — la cadena REPLACED puede arrancar en el primer step cuando ya existe persistido.
     * Validación: devuelve REPLACED. Bug que lo pone en rojo: aplicar a esta regla "el primer step no puede ser
     * REPLACED", que solo vale para steps nuevos.
     */
    @Test
    void calculatePrestationInstanceStatus_allStepsReplacedFromFirst_returnsReplaced() {
        List<PrestationStep> catalog = catalog(4);

        PrestationInstanceStatus result = service.calculatePrestationInstanceStatus(catalog, List.of(
                stepInstance(catalog.get(0), PrestationStepStatus.REPLACED),
                stepInstance(catalog.get(1), PrestationStepStatus.REPLACED),
                stepInstance(catalog.get(2), PrestationStepStatus.REPLACED),
                stepInstance(catalog.get(3), PrestationStepStatus.REPLACED)));

        assertThat(result).isEqualTo(PrestationInstanceStatus.REPLACED);
    }

    /**
     * CASO: Catálogo S1→S2; el request trae S1 IN_PROGRESS y S2 FINISHED (ambos nuevos).
     * Regla: D-27 — un step solo avanza si todos los anteriores están resueltos (FINISHED/OMITTED); IN_PROGRESS no
     * cuenta como resuelto aunque venga en el mismo request.
     * Validación: ConflictException (409) con la clave previousNotResolved. Bug que lo pone en rojo: contar como
     * "resuelto" cualquier step presente en el request.
     */
    @Test
    void validateStepAdvancement_previousInProgressNextFinished_throwsConflictException() {
        List<PrestationStep> catalog = catalog(2);

        assertThatThrownBy(() -> service.validateStepAdvancement(null, catalog, List.of(), List.of(
                newStep(101L, PrestationStepStatus.IN_PROGRESS),
                newStep(102L, PrestationStepStatus.FINISHED))))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertKey(e, "previousNotResolved"));
    }

    /**
     * CASO: Catálogo S1→S2 (S1 no requerido); el request trae S1 OMITTED y S2 FINISHED.
     * Regla: D-27 — OMITTED resuelve al step anterior.
     * Validación: no lanza.
     */
    @Test
    void validateStepAdvancement_previousOmittedNextFinished_isAccepted() {
        List<PrestationStep> catalog = catalog(2);

        assertThatCode(() -> service.validateStepAdvancement(null, catalog, List.of(), List.of(
                newStep(101L, PrestationStepStatus.OMITTED),
                newStep(102L, PrestationStepStatus.FINISHED))))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: maxTooth = 2, nivel TOOTH_FACE (dientes 14, 15 y 16 con cara TOP), 3 dientes.
     * Regla: D-29 — el techo de dientes aplica a los niveles TOOTH y TOOTH_FACE.
     * Validación: ConflictException (409) con el label de la prestación como argumento. Bug que lo pone en rojo:
     * guard por nivel que solo mira TOOTH.
     */
    @Test
    void validateMaxTooth_toothFaceExceedsAllowed_throwsConflictException() {
        PrestationType puente = type("BRIDGE", "Puente", false, Set.of(PrestationScopeType.TOOTH_FACE), 2, false);

        assertThatThrownBy(() -> service.validateMaxTooth(puente,
                teethWithFace(ToothFace.TOP, Tooth.T14, Tooth.T15, Tooth.T16)))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((AppException) e).getUserArgs()).contains("Puente"));
    }

    /**
     * CASO: maxTooth = 3, dientes [16, 26] (los dos del maxilar superior, en cuadrantes distintos).
     * Regla: D-11 — la regla compara maxilar, no cuadrante.
     * Validación: no lanza. Bug que lo pone en rojo: comparar cuadrante en vez de maxilar.
     */
    @Test
    void validateSameMaxillary_sameMaxillaryDifferentQuadrant_isAccepted() {
        PrestationType type = type("CROWN", "Corona", false, Set.of(PrestationScopeType.TOOTH), 3, false);

        assertThatCode(() -> service.validateSameMaxillary(type, teeth(Tooth.T16, Tooth.T26))).doesNotThrowAnyException();
    }

    /**
     * CASO: maxTooth = 2, dientes [16, 36] (maxilares distintos).
     * Regla: D-11 — la regla aplica con maxTooth > 1, y 2 ya es mayor que 1.
     * Validación: ConflictException (409). Bug que lo pone en rojo: umbral "> 2" en vez de "> 1".
     */
    @Test
    void validateSameMaxillary_maxToothTwoMixedMaxillary_throwsConflictException() {
        PrestationType type = type("CROWN", "Corona", false, Set.of(PrestationScopeType.TOOTH), 2, false);

        assertThatThrownBy(() -> service.validateSameMaxillary(type, teeth(Tooth.T16, Tooth.T36)))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Dos prestaciones de scope propio REPLACED (tooth [16] y tooth [26]); el único nuevo está sobre el 16.
     * Regla: D-40 Caso D — cada prestación reemplazada tiene que quedar cubierta por algún nuevo.
     * Validación: ConflictException (409) porque el 26 queda sin cobertura. Bug que lo pone en rojo: anyMatch en
     * lugar de exigir cobertura de cada reemplazada.
     */
    @Test
    void validatePrestationInstanceReplacementTraceability_twoReplacedOnlyOneCovered_throwsConflictException() {
        PrestationType restauracion = simpleType("FILLING", "Restauración");
        PrestationInstance replacedOn16 = prestationAt(restauracion, teeth(Tooth.T16), null);
        PrestationInstance replacedOn26 = prestationAt(restauracion, teeth(Tooth.T26), null);

        assertThatThrownBy(() -> service.validatePrestationInstanceReplacementTraceability(
                List.of(replacedOn16, replacedOn26), List.of(treatmentAt(teeth(Tooth.T16))), List.of()))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Una reemplazada tooth [16]; dos prestaciones nuevas, la primera sobre el 26 y la segunda con quadrant =
     * UPPER_RIGHT propio; y una variante donde la cobertura de la segunda viene del scope del TreatmentInstance
     * (quadrant = UPPER_RIGHT) porque no tiene scope propio.
     * Regla: D-40 — alcanza con que algún nuevo cubra la ubicación reemplazada, sin importar el orden, usando la
     * ubicación efectiva (scope propio o del tratamiento).
     * Validación: no lanza en ninguna de las dos variantes. Bug que lo pone en rojo: mirar solo la primera nueva o
     * ignorar la ubicación efectiva vía treatmentInstance.
     */
    @Test
    void validatePrestationInstanceReplacementTraceability_twoNewOnlySecondCovers_isAccepted() {
        PrestationType limpieza = simpleType("CLEANING", "Limpieza");
        PrestationInstance replaced = prestationAt(simpleType("FILLING", "Restauración"), teeth(Tooth.T16), null);
        PrestationInstance firstOn26 = prestationAt(limpieza, teeth(Tooth.T26), null);
        PrestationInstance secondOwnScope = prestationAt(limpieza, quadrant(Quadrant.UPPER_RIGHT), null);
        PrestationInstance secondViaTreatment = prestationAt(limpieza, Scope.empty(), treatmentAt(quadrant(Quadrant.UPPER_RIGHT)));

        assertThatCode(() -> service.validatePrestationInstanceReplacementTraceability(
                List.of(replaced), List.of(), List.of(firstOn26, secondOwnScope)))
                .doesNotThrowAnyException();
        assertThatCode(() -> service.validatePrestationInstanceReplacementTraceability(
                List.of(replaced), List.of(), List.of(firstOn26, secondViaTreatment)))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Tipo con allowedScopes = {TOOTH} y ubicación efectiva Scope.empty().
     * Regla: D-07 — una ubicación vacía se trata igual que una nula: no hay ubicación efectiva y el tipo exige una.
     * Validación: ConflictException (409). Scope.empty().level() cae en TOOTH, por eso un scope vacío no debe aceptarse
     * por coincidir con el nivel permitido.
     */
    @Test
    void validateScopeAllowedForType_emptyScope_throwsConflictException() {
        PrestationType obturacion = type("FILLING", "Obturación", false, Set.of(PrestationScopeType.TOOTH), null, false);

        assertThatThrownBy(() -> service.validateScopeAllowedForType(obturacion, Scope.empty()))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Dos Limpiezas nuevas IN_PROGRESS, la primera sobre el 16 (sin choque) y la segunda sobre el 24; en la lista
     * de IN_PROGRESS hay una Limpieza sobre el 11 y otra sobre el 24.
     * Regla: D-37 — cada prestación nueva se evalúa contra cada una de las IN_PROGRESS; el choque puede estar solo en
     * la segunda nueva y con la segunda de la lista.
     * Validación: ConflictException (409). Bug que lo pone en rojo: evaluar solo la primera nueva o solo la primera
     * IN_PROGRESS.
     */
    @Test
    void validatePrestationInstancePending_collisionOnlyInSecondItem_throwsConflictException() {
        PrestationType limpieza = simpleType("CLEANING", "Limpieza");
        PrestationInstance nuevaOn16 = prestationAt(limpieza, teeth(Tooth.T16), null);
        PrestationInstance nuevaOn24 = prestationAt(limpieza, teeth(Tooth.T24), null);
        PrestationInstance enBaseOn11 = prestationAt(limpieza, teeth(Tooth.T11), null);
        PrestationInstance enBaseOn24 = prestationAt(limpieza, teeth(Tooth.T24), null);

        assertThatThrownBy(() -> service.validatePrestationInstancePending(
                List.of(nuevaOn16, nuevaOn24), List.of(enBaseOn11, enBaseOn24)))
                .isInstanceOf(ConflictException.class);
    }
}
