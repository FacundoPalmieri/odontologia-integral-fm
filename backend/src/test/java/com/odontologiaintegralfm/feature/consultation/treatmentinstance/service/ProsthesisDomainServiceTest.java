package com.odontologiaintegralfm.feature.consultation.treatmentinstance.service;

import com.odontologiaintegralfm.feature.consultation.prestation.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.prestation.service.PrestationStepService;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.service.PrestationInstanceDomainService;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.scope.model.Scope;
import com.odontologiaintegralfm.feature.consultation.scope.service.ScopeDomainService;
import com.odontologiaintegralfm.feature.consultation.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.treatment.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.model.TreatmentInstance;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * Tests de las reglas de negocio de ProsthesisDomainService (REQ-038).
 *
 * Decisiones de armado (ADR-0031):
 * - TreatmentInstance, PrestationInstance, PrestationType y Treatment son datos de entrada: se mockean con
 *   stubs lenient en los helpers. Cada TreatmentInstance y cada PrestationInstance tiene un id distinto.
 * - Scope es un valor: se construye real con Scope.build.
 * - ScopeDomainService y PrestationInstanceDomainService son reales (@Spy sin stubs): si se mockeara
 *   resolveLocation u overlaps, las reglas de ubicación y solape serían tautológicas.
 * - Las listas de entrada se pasan en orden inverso al esperado, para que ninguna fila dependa del orden de llegada.
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ProsthesisDomainServiceTest {

    private static final long LINKED_TREATMENT_ID = 9L;
    private static final long UNLINKED_TREATMENT_ID = 3L;
    private static final Set<Long> REPAIR_LINKED_IDS = Set.of(LINKED_TREATMENT_ID);

    private static final LocalDateTime JAN = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalDateTime FEB = LocalDateTime.of(2026, 2, 1, 10, 0);
    private static final LocalDateTime MAR = LocalDateTime.of(2026, 3, 1, 10, 0);

    @Spy private ScopeDomainService scopeDomainService = new ScopeDomainService();
    @Spy private PrestationInstanceDomainService prestationInstanceDomainService =
            new PrestationInstanceDomainService(mock(PrestationStepService.class), scopeDomainService);

    @InjectMocks private ProsthesisDomainService service;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static Scope teeth(int... codes) {
        return Scope.build(Arrays.stream(codes).mapToObj(Tooth::fromCode).toList(), null, null, null, false);
    }

    private static TreatmentInstance treatmentInstance(long id, TreatmentConditionType condition, long treatmentId,
                                                       Scope scope, LocalDateTime createdAt) {
        TreatmentInstance instance = mock(TreatmentInstance.class);
        Treatment treatment = mock(Treatment.class);
        lenient().when(treatment.getId()).thenReturn(treatmentId);
        lenient().when(instance.getId()).thenReturn(id);
        lenient().when(instance.getTreatment()).thenReturn(treatment);
        lenient().when(instance.getTreatmentCondition()).thenReturn(condition);
        lenient().when(instance.getScope()).thenReturn(scope);
        lenient().when(instance.getCreatedAt()).thenReturn(createdAt);
        return instance;
    }

    private static TreatmentInstance finishedOrigin(long id, Scope scope, LocalDateTime createdAt) {
        return treatmentInstance(id, TreatmentConditionType.FINISHED, UNLINKED_TREATMENT_ID, scope, createdAt);
    }

    /** Colocación: prestación de un tratamiento, sin origen propio. */
    private static PrestationInstance placement(long id, TreatmentInstance treatment, PrestationInstanceStatus status,
                                                boolean canBeOrigin) {
        return placement(id, treatment, status, canBeOrigin, JAN);
    }

    private static PrestationInstance placement(long id, TreatmentInstance treatment, PrestationInstanceStatus status,
                                                boolean canBeOrigin, LocalDateTime createdAt) {
        PrestationType type = mock(PrestationType.class);
        lenient().when(type.isCanBeOrigin()).thenReturn(canBeOrigin);
        PrestationInstance prestation = mock(PrestationInstance.class);
        lenient().when(prestation.getId()).thenReturn(id);
        lenient().when(prestation.getStatus()).thenReturn(status);
        lenient().when(prestation.getType()).thenReturn(type);
        lenient().when(prestation.getTreatmentInstance()).thenReturn(treatment);
        lenient().when(prestation.getScope()).thenReturn(Scope.empty());
        lenient().when(prestation.getCreatedAt()).thenReturn(createdAt);
        return prestation;
    }

    private static PrestationInstance finishedPlacement(long id, TreatmentInstance treatment) {
        return placement(id, treatment, PrestationInstanceStatus.FINISHED, true);
    }

    /** Arreglo: prestación con origen, con el tipo de ubicaciones que admite y, opcional, ubicación propia o tratamiento propio. */
    private static PrestationInstance repair(long id, TreatmentInstance origin, PrestationInstanceStatus status,
                                             Set<PrestationScopeType> allowedScopes, Scope ownScope,
                                             TreatmentInstance ownTreatment, LocalDateTime createdAt) {
        PrestationType type = mock(PrestationType.class);
        lenient().when(type.isCanBeOrigin()).thenReturn(false);
        lenient().when(type.getAllowedScopes()).thenReturn(allowedScopes);
        PrestationInstance prestation = mock(PrestationInstance.class);
        lenient().when(prestation.getId()).thenReturn(id);
        lenient().when(prestation.getStatus()).thenReturn(status);
        lenient().when(prestation.getType()).thenReturn(type);
        lenient().when(prestation.getOriginTreatmentInstance()).thenReturn(origin);
        lenient().when(prestation.getTreatmentInstance()).thenReturn(ownTreatment);
        lenient().when(prestation.getScope()).thenReturn(ownScope);
        lenient().when(prestation.getCreatedAt()).thenReturn(createdAt);
        return prestation;
    }

    /** Agregado con su propio tratamiento: la ubicación está en el tratamiento y el scope propio queda vacío. */
    private static PrestationInstance addition(long id, TreatmentInstance origin, PrestationInstanceStatus status,
                                               Scope toothScope, LocalDateTime createdAt) {
        return addition(id, origin, status, toothScope, createdAt, id * 1000, createdAt);
    }

    /** Igual, pero el tratamiento propio tiene su propio id y su propio createdAt, distintos de los de la prestación. */
    private static PrestationInstance addition(long id, TreatmentInstance origin, PrestationInstanceStatus status,
                                               Scope toothScope, LocalDateTime createdAt,
                                               long ownTreatmentId, LocalDateTime ownTreatmentCreatedAt) {
        TreatmentInstance own = treatmentInstance(ownTreatmentId, TreatmentConditionType.FINISHED, UNLINKED_TREATMENT_ID,
                toothScope, ownTreatmentCreatedAt);
        return repair(id, origin, status, Set.of(PrestationScopeType.TOOTH), Scope.empty(), own, createdAt);
    }

    private List<ProsthesisDomainService.CurrentProsthesis> build(List<TreatmentInstance> treatments,
                                                                  List<PrestationInstance> prestations) {
        return service.buildCurrentProstheses(treatments, prestations, REPAIR_LINKED_IDS);
    }

    // ─── D-01 ─────────────────────────────────────────────────────────────────

    /**
     * CASO: un tratamiento en REQUIRED, IN_PROGRESS, REPLACED o CANCELED no se ofrece, aunque su Treatment esté ligado
     * a arreglos y tenga una Colocación FINISHED marcada canBeOrigin.
     * Regla: D-01 — solo FINISHED o PREEXISTING entran.
     * Validación: el resultado es una lista vacía para cada condición.
     */
    @ParameterizedTest
    @EnumSource(value = TreatmentConditionType.class, names = {"REQUIRED", "IN_PROGRESS", "REPLACED", "CANCELED"})
    void buildCurrentProstheses_originConditionNotFinishedOrPreexisting_notListed(TreatmentConditionType condition) {
        TreatmentInstance origin = treatmentInstance(1L, condition, LINKED_TREATMENT_ID, teeth(14, 16, 17), JAN);
        PrestationInstance placement = finishedPlacement(10L, origin);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(List.of(origin), List.of(placement));

        assertThat(result).isEmpty();
    }

    // ─── D-02 ─────────────────────────────────────────────────────────────────

    /**
     * CASO: la prótesis removible que trajo Alan, cargada PREEXISTING, con Treatment ligado a arreglos y sin prestaciones.
     * Regla: D-02.
     * Validación: 1 elemento; origin() es ese TreatmentInstance; scopes() tiene 1 elemento, el mismo Scope del origen.
     */
    @Test
    void buildCurrentProstheses_preexistingWithRepairLinkedTreatment_listedWithOriginScopeFirst() {
        Scope originScope = teeth(12, 13, 14);
        TreatmentInstance origin = treatmentInstance(1L, TreatmentConditionType.PREEXISTING, LINKED_TREATMENT_ID, originScope, JAN);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(List.of(origin), List.of());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin()).isSameAs(origin);
        assertThat(result.get(0).scopes()).hasSize(1);
        assertThat(result.get(0).scopes().get(0)).isSameAs(originScope);
    }

    /**
     * CASO: una Corona vieja cargada PREEXISTING; su Treatment (id 4) no está entre los ligados a arreglos (id 9).
     * Regla: D-02.
     * Validación: lista vacía.
     */
    @Test
    void buildCurrentProstheses_preexistingWithUnlinkedTreatment_notListed() {
        TreatmentInstance origin = treatmentInstance(1L, TreatmentConditionType.PREEXISTING, 4L, teeth(14), JAN);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(List.of(origin), List.of());

        assertThat(result).isEmpty();
    }

    // ─── D-03 ─────────────────────────────────────────────────────────────────

    /**
     * CASO: la prótesis que le colocan a Bety: tratamiento FINISHED con Colocación FINISHED marcada canBeOrigin.
     * Regla: D-03. El Treatment (id 3) no está entre los ligados, así que solo la Colocación la hace entrar.
     * Validación: 1 elemento con ese origin() y scopes() con solo el Scope del origen.
     */
    @Test
    void buildCurrentProstheses_finishedWithCanBeOriginPrestation_listed() {
        Scope originScope = teeth(14, 16, 17);
        TreatmentInstance origin = treatmentInstance(1L, TreatmentConditionType.FINISHED, 3L, originScope, JAN);
        PrestationInstance placement = finishedPlacement(10L, origin);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(List.of(origin), List.of(placement));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin()).isSameAs(origin);
        assertThat(result.get(0).scopes()).hasSize(1);
        assertThat(result.get(0).scopes().get(0)).isSameAs(originScope);
    }

    /**
     * CASO: un blanqueamiento FINISHED cuyo Treatment (id 9) está ligado a arreglos, pero su prestación no tiene canBeOrigin.
     * Regla: D-03 — un FINISHED se decide por canBeOrigin, no por el Treatment ligado (esa es la regla del PREEXISTING).
     * Validación: lista vacía.
     */
    @Test
    void buildCurrentProstheses_finishedWithoutCanBeOriginPrestation_notListed() {
        TreatmentInstance origin = treatmentInstance(1L, TreatmentConditionType.FINISHED, LINKED_TREATMENT_ID, teeth(14), JAN);
        PrestationInstance whitening = placement(10L, origin, PrestationInstanceStatus.FINISHED, false);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(List.of(origin), List.of(whitening));

        assertThat(result).isEmpty();
    }

    /**
     * CASO: la Colocación marcada canBeOrigin está IN_PROGRESS, REPLACED o CANCELED, y el tratamiento quedó FINISHED
     * por una segunda prestación FINISHED sin la marca.
     * Regla: D-03 — solo cuenta la Colocación FINISHED.
     * Validación: lista vacía para cada estado.
     */
    @ParameterizedTest
    @EnumSource(value = PrestationInstanceStatus.class, names = {"IN_PROGRESS", "REPLACED", "CANCELED"})
    void buildCurrentProstheses_canBeOriginPrestationNotFinished_notListed(PrestationInstanceStatus status) {
        TreatmentInstance origin = finishedOrigin(1L, teeth(14, 16, 17), JAN);
        PrestationInstance placement = placement(10L, origin, status, true);
        PrestationInstance other = placement(11L, origin, PrestationInstanceStatus.FINISHED, false);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(List.of(origin), List.of(other, placement));

        assertThat(result).isEmpty();
    }

    // ─── D-04 ─────────────────────────────────────────────────────────────────

    /**
     * CASO: el Agregado FINISHED del diente 18 de Bety, con tratamiento propio y scope propio vacío.
     * Regla: D-04 — la ubicación se resuelve con resolveLocation (scope propio, o el del tratamiento).
     * Validación: scopes() tiene 2 elementos: [0] es el Scope del origen y [1] es el Scope del tratamiento del Agregado.
     */
    @Test
    void buildCurrentProstheses_repairFinishedWithOwnTreatment_scopeTakenFromTreatmentAfterOrigin() {
        Scope originScope = teeth(14, 16, 17);
        Scope additionScope = teeth(18);
        TreatmentInstance origin = finishedOrigin(1L, originScope, JAN);
        PrestationInstance placement = finishedPlacement(10L, origin);
        PrestationInstance addition = addition(20L, origin, PrestationInstanceStatus.FINISHED, additionScope, FEB);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(List.of(origin), List.of(addition, placement));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).scopes()).hasSize(2);
        assertThat(result.get(0).scopes().get(0)).isSameAs(originScope);
        assertThat(result.get(0).scopes().get(1)).isSameAs(additionScope);
    }

    /**
     * CASO: una Compostura FINISHED del diente 13 que lleva su propia ubicación y no tiene tratamiento propio, sobre
     * la prótesis PREEXISTING de Alan.
     * Regla: D-04 — si el scope propio no está vacío, se usa ese.
     * Validación: scopes() tiene 2 elementos y [1] es el Scope propio de la prestación.
     */
    @Test
    void buildCurrentProstheses_repairFinishedWithOwnScope_scopeTakenFromPrestation() {
        Scope originScope = teeth(12, 13, 14);
        Scope repairScope = teeth(13);
        TreatmentInstance origin = treatmentInstance(1L, TreatmentConditionType.PREEXISTING, LINKED_TREATMENT_ID, originScope, JAN);
        PrestationInstance repair = repair(20L, origin, PrestationInstanceStatus.FINISHED,
                Set.of(PrestationScopeType.TOOTH), repairScope, null, FEB);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(List.of(origin), List.of(repair));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).scopes()).hasSize(2);
        assertThat(result.get(0).scopes().get(0)).isSameAs(originScope);
        assertThat(result.get(0).scopes().get(1)).isSameAs(repairScope);
    }

    /**
     * CASO: un Agregado IN_PROGRESS, REPLACED o CANCELED con tratamiento propio en el diente 18.
     * Regla: D-04 — solo suman los arreglos FINISHED.
     * Validación: scopes() tiene 1 elemento (el origen) para cada estado.
     */
    @ParameterizedTest
    @EnumSource(value = PrestationInstanceStatus.class, names = {"IN_PROGRESS", "REPLACED", "CANCELED"})
    void buildCurrentProstheses_repairNotFinished_notAdded(PrestationInstanceStatus status) {
        TreatmentInstance origin = finishedOrigin(1L, teeth(14, 16, 17), JAN);
        PrestationInstance placement = finishedPlacement(10L, origin);
        PrestationInstance addition = addition(20L, origin, status, teeth(18), FEB);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(List.of(origin), List.of(addition, placement));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).scopes()).hasSize(1);
    }

    /**
     * CASO: un Rebasado FINISHED, cuyo tipo no admite ninguna ubicación (allowedScopes vacío).
     * Regla: D-04 — solo suman los arreglos con allowedScopes no vacío.
     * Validación: scopes() tiene 1 elemento.
     */
    @Test
    void buildCurrentProstheses_repairWithoutAllowedScopes_notAdded() {
        TreatmentInstance origin = finishedOrigin(1L, teeth(14, 16, 17), JAN);
        PrestationInstance placement = finishedPlacement(10L, origin);
        PrestationInstance relining = repair(20L, origin, PrestationInstanceStatus.FINISHED,
                Set.of(), Scope.empty(), null, FEB);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(List.of(origin), List.of(relining, placement));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).scopes()).hasSize(1);
    }

    /**
     * CASO: dos prótesis FINISHED sin dientes en común (A en 14/16/17, B en 21/22) y un Agregado del origen de B en el 23.
     * Regla: D-04 — cada arreglo suma solo a la prótesis a la que apunta.
     * Validación: la prótesis A tiene scopes() de 1 elemento; la B tiene 2 y [1] es el diente 23.
     */
    @Test
    void buildCurrentProstheses_repairOfAnotherOrigin_addedOnlyToItsProsthesis() {
        TreatmentInstance originA = finishedOrigin(1L, teeth(14, 16, 17), JAN);
        TreatmentInstance originB = finishedOrigin(2L, teeth(21, 22), FEB);
        Scope additionScope = teeth(23);
        PrestationInstance placementA = finishedPlacement(10L, originA);
        PrestationInstance placementB = finishedPlacement(11L, originB);
        PrestationInstance addition = addition(20L, originB, PrestationInstanceStatus.FINISHED, additionScope, MAR);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(
                List.of(originB, originA), List.of(addition, placementB, placementA));

        assertThat(result).hasSize(2);
        ProsthesisDomainService.CurrentProsthesis prosthesisA = result.stream()
                .filter(p -> p.origin() == originA).findFirst().orElseThrow();
        ProsthesisDomainService.CurrentProsthesis prosthesisB = result.stream()
                .filter(p -> p.origin() == originB).findFirst().orElseThrow();
        assertThat(prosthesisA.scopes()).hasSize(1);
        assertThat(prosthesisB.scopes()).hasSize(2);
        assertThat(prosthesisB.scopes().get(1)).isSameAs(additionScope);
    }

    // ─── D-06: orden ──────────────────────────────────────────────────────────

    /**
     * CASO: dos Agregados FINISHED (dientes 18 y 28) con createdAt 1/2 y 1/3, pasados de más nuevo a más viejo.
     * Regla: D-06 — los arreglos suman de la prestación más antigua a la más reciente.
     * Validación: scopes() es [origen, diente 18, diente 28].
     */
    @Test
    void buildCurrentProstheses_repairsFinished_orderedByPrestationCreatedAt() {
        Scope originScope = teeth(14, 16, 17);
        Scope scope18 = teeth(18);
        Scope scope28 = teeth(28);
        TreatmentInstance origin = finishedOrigin(1L, originScope, JAN);
        PrestationInstance placement = finishedPlacement(10L, origin);
        // El tratamiento propio de cada Agregado tiene el createdAt en sentido contrario al de su prestación.
        PrestationInstance newer = addition(21L, origin, PrestationInstanceStatus.FINISHED, scope28, MAR, 100L, JAN);
        PrestationInstance older = addition(22L, origin, PrestationInstanceStatus.FINISHED, scope18, FEB, 900L, MAR);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(List.of(origin), List.of(newer, older, placement));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).scopes()).hasSize(3);
        assertThat(result.get(0).scopes().get(0)).isSameAs(originScope);
        assertThat(result.get(0).scopes().get(1)).isSameAs(scope18);
        assertThat(result.get(0).scopes().get(2)).isSameAs(scope28);
    }

    /**
     * CASO: dos Agregados FINISHED con el mismo createdAt, id 20 (diente 18) e id 10 (diente 28), pasados con el 20 primero.
     * Regla: D-06 — ante igual createdAt, va primero el de menor id.
     * Validación: scopes() es [origen, diente 28, diente 18].
     */
    @Test
    void buildCurrentProstheses_repairsSameCreatedAt_orderedById() {
        Scope originScope = teeth(14, 16, 17);
        Scope scope18 = teeth(18);
        Scope scope28 = teeth(28);
        TreatmentInstance origin = finishedOrigin(1L, originScope, JAN);
        PrestationInstance placement = finishedPlacement(5L, origin);
        // El id del tratamiento propio va en sentido contrario al id de la prestación.
        PrestationInstance id20 = addition(20L, origin, PrestationInstanceStatus.FINISHED, scope18, FEB, 100L, FEB);
        PrestationInstance id10 = addition(10L, origin, PrestationInstanceStatus.FINISHED, scope28, FEB, 200L, FEB);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(List.of(origin), List.of(id20, id10, placement));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).scopes()).hasSize(3);
        assertThat(result.get(0).scopes().get(0)).isSameAs(originScope);
        assertThat(result.get(0).scopes().get(1)).isSameAs(scope28);
        assertThat(result.get(0).scopes().get(2)).isSameAs(scope18);
    }

    // ─── D-05: solapes ────────────────────────────────────────────────────────

    /**
     * CASO: Bety tiene una prótesis en 14/16/17 (1/1) y después otra en 14/15/16/17 (1/2); entran como [B, A].
     * Regla: D-05 — si comparten un diente, queda solo la más nueva.
     * Validación: 1 elemento y su origin() es B.
     */
    @Test
    void buildCurrentProstheses_sharedTooth_onlyNewestKept() {
        TreatmentInstance originA = finishedOrigin(1L, teeth(14, 16, 17), JAN);
        TreatmentInstance originB = finishedOrigin(2L, teeth(14, 15, 16, 17), FEB);
        PrestationInstance placementA = finishedPlacement(10L, originA);
        PrestationInstance placementB = finishedPlacement(11L, originB);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(
                List.of(originB, originA), List.of(placementB, placementA));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin()).isSameAs(originB);
    }

    /**
     * CASO: dos prótesis vigentes en lugares distintos, A en 14/16/17 (más vieja) y B en 21/22.
     * Regla: D-05 — sin diente en común, quedan las dos.
     * Validación: 2 elementos, en el orden [A, B].
     */
    @Test
    void buildCurrentProstheses_noSharedTooth_bothKept() {
        TreatmentInstance originA = finishedOrigin(1L, teeth(14, 16, 17), JAN);
        TreatmentInstance originB = finishedOrigin(2L, teeth(21, 22), FEB);
        PrestationInstance placementA = finishedPlacement(10L, originA);
        PrestationInstance placementB = finishedPlacement(11L, originB);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(
                List.of(originB, originA), List.of(placementB, placementA));

        assertThat(result).extracting(ProsthesisDomainService.CurrentProsthesis::origin)
                .containsExactly(originA, originB);
    }

    /**
     * CASO: A {11, 12} (1/1), B {12, 13} (1/2) y C {13, 14} (1/3); A y C no comparten dientes.
     * Regla: D-05 — A queda afuera por B, y B queda afuera por C, aunque B ya esté descartada.
     * Validación: 1 elemento y es C.
     */
    @Test
    void buildCurrentProstheses_chainedOverlap_onlyLastKept() {
        TreatmentInstance originA = finishedOrigin(1L, teeth(11, 12), JAN);
        TreatmentInstance originB = finishedOrigin(2L, teeth(12, 13), FEB);
        TreatmentInstance originC = finishedOrigin(3L, teeth(13, 14), MAR);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(
                List.of(originB, originC, originA),
                List.of(finishedPlacement(11L, originB), finishedPlacement(12L, originC), finishedPlacement(10L, originA)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin()).isSameAs(originC);
    }

    /**
     * CASO: una prótesis completa del maxilar superior (sin lista de dientes, 1/1) y otra en el diente 14 (1/2).
     * Regla: D-05 — un maxilar sin lista se compara como todos sus dientes.
     * Validación: 1 elemento, el del diente 14.
     */
    @Test
    void buildCurrentProstheses_fullJawVsTooth_overlapDetected() {
        TreatmentInstance jaw = finishedOrigin(1L, Scope.build(null, null, null, Maxillary.UPPER, false), JAN);
        TreatmentInstance tooth14 = finishedOrigin(2L, teeth(14), FEB);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(
                List.of(tooth14, jaw), List.of(finishedPlacement(11L, tooth14), finishedPlacement(10L, jaw)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin()).isSameAs(tooth14);
    }

    /**
     * CASO: A en el 14 (1/1) con un Agregado FINISHED en el 18; B en 17/18 (1/2). Los orígenes no comparten diente.
     * Regla: D-05 — el solape usa también las ubicaciones de los arreglos.
     * Validación: 1 elemento, B.
     */
    @Test
    void buildCurrentProstheses_overlapOnlyThroughRepairScope_olderDropped() {
        TreatmentInstance originA = finishedOrigin(1L, teeth(14), JAN);
        TreatmentInstance originB = finishedOrigin(2L, teeth(17, 18), FEB);
        PrestationInstance addition = addition(20L, originA, PrestationInstanceStatus.FINISHED, teeth(18), FEB);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(
                List.of(originB, originA),
                List.of(addition, finishedPlacement(11L, originB), finishedPlacement(10L, originA)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin()).isSameAs(originB);
    }

    /**
     * CASO: una prótesis FINISHED en el 14 (1/1) y un tratamiento más nuevo en el 14 (1/2) que está IN_PROGRESS, con su
     * prestación canBeOrigin también IN_PROGRESS (todavía no es prótesis vigente).
     * Regla: D-05 — solo cuentan como "más nueva" las que también son prótesis vigentes.
     * Validación: 1 elemento, A.
     */
    @Test
    void buildCurrentProstheses_overlapWithNonProsthesis_olderKept() {
        TreatmentInstance originA = finishedOrigin(1L, teeth(14), JAN);
        TreatmentInstance inProgress = treatmentInstance(2L, TreatmentConditionType.IN_PROGRESS,
                UNLINKED_TREATMENT_ID, teeth(14), FEB);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(
                List.of(inProgress, originA),
                List.of(placement(11L, inProgress, PrestationInstanceStatus.IN_PROGRESS, true), finishedPlacement(10L, originA)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin()).isSameAs(originA);
    }

    /**
     * CASO: dos prótesis que comparten el 14, con el mismo createdAt, id 1 e id 2, pasadas como [2, 1].
     * Regla: D-05 — ante igual createdAt se considera más nuevo el de mayor id.
     * Validación: 1 elemento y su origin().getId() es 2.
     */
    @Test
    void buildCurrentProstheses_sameOriginCreatedAt_higherIdKept() {
        TreatmentInstance origin1 = finishedOrigin(1L, teeth(14), JAN);
        TreatmentInstance origin2 = finishedOrigin(2L, teeth(14), JAN);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(
                List.of(origin2, origin1), List.of(finishedPlacement(11L, origin2), finishedPlacement(10L, origin1)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin().getId()).isEqualTo(2L);
    }

    /**
     * CASO: la prótesis de Bety (14/16/17, con Colocación FINISHED canBeOrigin) y, aparte, un blanqueamiento FINISHED en
     * 21/22 cuya prestación FINISHED no tiene la marca. La prestación del blanqueamiento va primera en la lista.
     * Regla: D-03 — la Colocación tiene que ser una prestación del tratamiento que se evalúa, no de otro.
     * Validación: 1 elemento y su origin() es la prótesis de Bety.
     */
    @Test
    void buildCurrentProstheses_finishedWithoutOwnPlacement_notListedNextToOtherPlacement() {
        TreatmentInstance prosthesis = finishedOrigin(1L, teeth(14, 16, 17), JAN);
        TreatmentInstance whitening = finishedOrigin(2L, teeth(21, 22), FEB);
        PrestationInstance whiteningPrestation = placement(10L, whitening, PrestationInstanceStatus.FINISHED, false);
        PrestationInstance placement = finishedPlacement(11L, prosthesis);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(
                List.of(whitening, prosthesis), List.of(whiteningPrestation, placement));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin()).isSameAs(prosthesis);
    }

    /**
     * CASO: Bety tiene una prótesis en 14 (enero), otra en 21 (febrero) y otra en 14 (marzo), pasadas como [C, A, B].
     * Regla: D-05 — queda afuera la que tenga alguna más nueva que la solape, no solo la siguiente en fecha.
     * Validación: el resultado es [B, C].
     */
    @Test
    void buildCurrentProstheses_overlapWithNonAdjacentNewer_olderDropped() {
        TreatmentInstance originA = finishedOrigin(1L, teeth(14), JAN);
        TreatmentInstance originB = finishedOrigin(2L, teeth(21), FEB);
        TreatmentInstance originC = finishedOrigin(3L, teeth(14), MAR);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(
                List.of(originC, originA, originB),
                List.of(finishedPlacement(12L, originC), finishedPlacement(10L, originA), finishedPlacement(11L, originB)));

        assertThat(result).extracting(ProsthesisDomainService.CurrentProsthesis::origin)
                .containsExactly(originB, originC);
    }

    /**
     * CASO: la prótesis PREEXISTING de Alan en 12/13/14 (createdAt 1/2) y una prótesis FINISHED en 13/14/15
     * (createdAt 1/3) cuya Colocación tiene createdAt 1/1, anterior a la del PREEXISTING.
     * Regla: D-05 — "más nueva" se decide por el createdAt del tratamiento de origen, no por el de la Colocación.
     * Validación: 1 elemento y su origin() es el FINISHED.
     */
    @Test
    void buildCurrentProstheses_preexistingOlderOverlappedByFinishedNewer_onlyFinishedKept() {
        TreatmentInstance preexisting = treatmentInstance(1L, TreatmentConditionType.PREEXISTING, LINKED_TREATMENT_ID,
                teeth(12, 13, 14), FEB);
        TreatmentInstance finished = finishedOrigin(2L, teeth(13, 14, 15), MAR);
        PrestationInstance placement = placement(10L, finished, PrestationInstanceStatus.FINISHED, true, JAN);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(
                List.of(finished, preexisting), List.of(placement));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin()).isSameAs(finished);
    }

    /**
     * CASO: una prótesis FINISHED en 13/14/15 (createdAt 1/1) cuya Colocación tiene createdAt 1/3, y una prótesis
     * PREEXISTING en 12/13/14 (createdAt 1/2) cargada después.
     * Regla: D-05 — el PREEXISTING, que no tiene Colocación, cuenta como más nuevo por el createdAt de su origen (riesgo
     * aceptado en D-02).
     * Validación: 1 elemento y su origin() es el PREEXISTING.
     */
    @Test
    void buildCurrentProstheses_finishedOlderOverlappedByPreexistingNewer_onlyPreexistingKept() {
        TreatmentInstance finished = finishedOrigin(1L, teeth(13, 14, 15), JAN);
        TreatmentInstance preexisting = treatmentInstance(2L, TreatmentConditionType.PREEXISTING, LINKED_TREATMENT_ID,
                teeth(12, 13, 14), FEB);
        PrestationInstance placement = placement(10L, finished, PrestationInstanceStatus.FINISHED, true, MAR);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(
                List.of(preexisting, finished), List.of(placement));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).origin()).isSameAs(preexisting);
    }

    // ─── D-06: orden del resultado ────────────────────────────────────────────

    /**
     * CASO: tres prótesis sin solape con createdAt 1/3, 1/1 y 1/2, pasadas en ese orden.
     * Regla: D-06 — la lista sale de la más antigua a la más nueva.
     * Validación: los origin() salen en el orden 1/1, 1/2, 1/3.
     */
    @Test
    void buildCurrentProstheses_severalProstheses_orderedByOriginCreatedAt() {
        TreatmentInstance march = finishedOrigin(1L, teeth(11), MAR);
        TreatmentInstance january = finishedOrigin(2L, teeth(21), JAN);
        TreatmentInstance february = finishedOrigin(3L, teeth(31), FEB);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(
                List.of(march, january, february),
                List.of(finishedPlacement(10L, march), finishedPlacement(11L, january), finishedPlacement(12L, february)));

        assertThat(result).extracting(ProsthesisDomainService.CurrentProsthesis::origin)
                .containsExactly(january, february, march);
    }

    /**
     * CASO: dos prótesis sin diente en común, con el mismo createdAt, id 5 e id 3, pasadas como [5, 3].
     * Regla: D-06 — ante igual createdAt va primero el de menor id.
     * Validación: origin().getId() sale [3, 5].
     */
    @Test
    void buildCurrentProstheses_originsSameCreatedAt_orderedById() {
        TreatmentInstance origin5 = finishedOrigin(5L, teeth(11), JAN);
        TreatmentInstance origin3 = finishedOrigin(3L, teeth(21), JAN);

        List<ProsthesisDomainService.CurrentProsthesis> result = build(
                List.of(origin5, origin3), List.of(finishedPlacement(10L, origin5), finishedPlacement(11L, origin3)));

        assertThat(result).extracting(p -> p.origin().getId()).containsExactly(3L, 5L);
    }
}