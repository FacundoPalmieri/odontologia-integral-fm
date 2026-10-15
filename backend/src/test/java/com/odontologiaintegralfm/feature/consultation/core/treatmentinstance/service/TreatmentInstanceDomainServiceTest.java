package com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.ToothFace;
import com.odontologiaintegralfm.feature.consultation.core.scope.model.Scope;
import com.odontologiaintegralfm.feature.consultation.core.scope.service.ScopeDomainService;
import com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.model.TreatmentInstance;
import com.odontologiaintegralfm.shared.exception.AppException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

/**
 * Decisión de diseño del test: la geometría (solapar, cubrir) vive en ScopeDomainService (ADR-0023) y
 * ya tiene su propia suite; acá se usa una instancia REAL como @Spy para que cada caso ejercite la regla
 * de negocio del tratamiento con ubicaciones verdaderas, en vez de stubbear overlaps/covers (lo que volvería
 * tautológicos los casos de colisión y cobertura). Las entidades de entrada (TreatmentInstance,
 * PrestationInstance, Treatment) son mocks con stubs lenient: son datos que el SUT lee; Scope es un valor real.
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class TreatmentInstanceDomainServiceTest {

    @Spy private ScopeDomainService scopeDomainService = new ScopeDomainService();

    @InjectMocks private TreatmentInstanceDomainService service;

    // ─── helpers ──────────────────────────────────────────────────────────────

    private static Scope teeth(Tooth... teeth) {
        return Scope.build(new ArrayList<>(List.of(teeth)), null, null, null, false);
    }

    private static Scope toothFace(Tooth tooth, ToothFace face) {
        return Scope.build(new ArrayList<>(List.of(tooth)), face, null, null, false);
    }

    private static Scope quadrant(Quadrant quadrant) {
        return Scope.build(new ArrayList<>(), null, quadrant, null, false);
    }

    private static Treatment treatment(Long id) {
        Treatment treatment = mock(Treatment.class, withSettings().lenient());
        org.mockito.Mockito.lenient().when(treatment.getId()).thenReturn(id);
        return treatment;
    }

    private static TreatmentInstance treatmentInstance(Scope scope, TreatmentConditionType condition, Treatment treatment) {
        TreatmentInstance ti = mock(TreatmentInstance.class, withSettings().lenient());
        org.mockito.Mockito.lenient().when(ti.getScope()).thenReturn(scope);
        org.mockito.Mockito.lenient().when(ti.getTreatmentCondition()).thenReturn(condition);
        org.mockito.Mockito.lenient().when(ti.getTreatment()).thenReturn(treatment);
        return ti;
    }

    private static TreatmentInstance treatmentInstance(Scope scope, TreatmentConditionType condition) {
        return treatmentInstance(scope, condition, treatment(1L));
    }

    private static TreatmentInstance treatmentInstance(Long id, Scope scope, TreatmentConditionType condition) {
        TreatmentInstance ti = treatmentInstance(scope, condition);
        org.mockito.Mockito.lenient().when(ti.getId()).thenReturn(id);
        return ti;
    }

    private static PrestationInstance prestation(PrestationInstanceStatus status) {
        PrestationInstance pi = mock(PrestationInstance.class, withSettings().lenient());
        org.mockito.Mockito.lenient().when(pi.getStatus()).thenReturn(status);
        return pi;
    }

    private static PrestationInstance prestation(PrestationInstanceStatus status, TreatmentInstance treatmentInstance, Scope ownScope) {
        PrestationInstance pi = prestation(status);
        org.mockito.Mockito.lenient().when(pi.getTreatmentInstance()).thenReturn(treatmentInstance);
        org.mockito.Mockito.lenient().when(pi.getScope()).thenReturn(ownScope);
        return pi;
    }

    // ─── calculateTreatmentCondition ──────────────────────────────────────────

    /**
     * CASO: T100 sobre el 16 de Bety con la Restauración REPLACED, una prestación FINISHED, otra CANCELED y el Sellador IN_PROGRESS.
     * Regla: D-28 — si al menos una sigue IN_PROGRESS, el tratamiento sigue IN_PROGRESS (máxima prioridad, pisa a REPLACED).
     * Validación: devuelve IN_PROGRESS. Con precedencia REPLACED sobre IN_PROGRESS devolvería REPLACED.
     */
    @Test
    void calculateTreatmentCondition_anyInProgress_returnsInProgress() {
        List<PrestationInstance> prestations = List.of(
                prestation(PrestationInstanceStatus.REPLACED),
                prestation(PrestationInstanceStatus.FINISHED),
                prestation(PrestationInstanceStatus.CANCELED),
                prestation(PrestationInstanceStatus.IN_PROGRESS));

        assertThat(service.calculateTreatmentCondition(prestations)).isEqualTo(TreatmentConditionType.IN_PROGRESS);
    }

    /**
     * CASO: Una prestación FINISHED y otra REPLACED.
     * Regla: D-28 — gana REPLACED sobre FINISHED (deja atada una continuidad).
     * Validación: devuelve REPLACED.
     */
    @Test
    void calculateTreatmentCondition_mixedFinishedAndReplaced_returnsReplaced() {
        List<PrestationInstance> prestations = List.of(
                prestation(PrestationInstanceStatus.FINISHED),
                prestation(PrestationInstanceStatus.REPLACED));

        assertThat(service.calculateTreatmentCondition(prestations)).isEqualTo(TreatmentConditionType.REPLACED);
    }

    /**
     * CASO: Una prestación FINISHED y dos CANCELED.
     * Regla: D-28 — CANCELED tiene la precedencia más baja: nunca es el resumen si hay algo finalizado.
     * Validación: devuelve FINISHED (nunca CANCELED).
     */
    @Test
    void calculateTreatmentCondition_finishedAndCanceledMixed_returnsFinished() {
        List<PrestationInstance> prestations = List.of(
                prestation(PrestationInstanceStatus.FINISHED),
                prestation(PrestationInstanceStatus.CANCELED),
                prestation(PrestationInstanceStatus.CANCELED));

        assertThat(service.calculateTreatmentCondition(prestations)).isEqualTo(TreatmentConditionType.FINISHED);
    }

    /**
     * CASO: Una prestación REPLACED y otra CANCELED.
     * Regla: D-28 — REPLACED gana sobre CANCELED.
     * Validación: devuelve REPLACED.
     */
    @Test
    void calculateTreatmentCondition_replacedAndCanceledMixed_returnsReplaced() {
        List<PrestationInstance> prestations = List.of(
                prestation(PrestationInstanceStatus.REPLACED),
                prestation(PrestationInstanceStatus.CANCELED));

        assertThat(service.calculateTreatmentCondition(prestations)).isEqualTo(TreatmentConditionType.REPLACED);
    }

    /**
     * CASO: Todas las prestaciones FINISHED.
     * Regla: D-28 — mismo estado terminal en todas → el tratamiento lo toma.
     * Validación: FINISHED.
     */
    @Test
    void calculateTreatmentCondition_allFinished_returnsFinished() {
        List<PrestationInstance> prestations = List.of(
                prestation(PrestationInstanceStatus.FINISHED),
                prestation(PrestationInstanceStatus.FINISHED));

        assertThat(service.calculateTreatmentCondition(prestations)).isEqualTo(TreatmentConditionType.FINISHED);
    }

    /**
     * CASO: Todas las prestaciones REPLACED.
     * Regla: D-28.
     * Validación: REPLACED.
     */
    @Test
    void calculateTreatmentCondition_allReplaced_returnsReplaced() {
        List<PrestationInstance> prestations = List.of(
                prestation(PrestationInstanceStatus.REPLACED),
                prestation(PrestationInstanceStatus.REPLACED));

        assertThat(service.calculateTreatmentCondition(prestations)).isEqualTo(TreatmentConditionType.REPLACED);
    }

    /**
     * CASO: Todas las prestaciones CANCELED.
     * Regla: D-28 — solo cuando todas cerraron CANCELED el tratamiento toma CANCELED.
     * Validación: CANCELED.
     */
    @Test
    void calculateTreatmentCondition_allCanceled_returnsCanceled() {
        List<PrestationInstance> prestations = List.of(
                prestation(PrestationInstanceStatus.CANCELED),
                prestation(PrestationInstanceStatus.CANCELED));

        assertThat(service.calculateTreatmentCondition(prestations)).isEqualTo(TreatmentConditionType.CANCELED);
    }

    /**
     * CASO: Una sola prestación IN_PROGRESS.
     * Regla: D-28.
     * Validación: IN_PROGRESS.
     */
    @Test
    void calculateTreatmentCondition_singleInProgress_returnsInProgress() {
        List<PrestationInstance> prestations = List.of(prestation(PrestationInstanceStatus.IN_PROGRESS));

        assertThat(service.calculateTreatmentCondition(prestations)).isEqualTo(TreatmentConditionType.IN_PROGRESS);
    }

    // ─── validateExplicitCloseAllowed ─────────────────────────────────────────

    /**
     * CASO: Tratamiento persistido en REQUIRED.
     * Regla: D-33 — el cierre explícito (REPLACED/CANCELED) solo se admite desde REQUIRED.
     * Validación: no lanza.
     */
    @Test
    void validateExplicitCloseAllowed_requiredTreatment_isAccepted() {
        TreatmentInstance required = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REQUIRED);

        assertThatCode(() -> service.validateExplicitCloseAllowed(required)).doesNotThrowAnyException();
    }

    /**
     * CASO: Tratamiento persistido en IN_PROGRESS, FINISHED, REPLACED, CANCELED o PREEXISTING.
     * Regla: D-33 — si no está en REQUIRED, 409.
     * Validación: ConflictException (clave de "no está en Requerido") en cada caso.
     */
    @ParameterizedTest
    @EnumSource(value = TreatmentConditionType.class, names = "REQUIRED", mode = EnumSource.Mode.EXCLUDE)
    void validateExplicitCloseAllowed_notRequired_throwsConflictException(TreatmentConditionType condition) {
        TreatmentInstance notRequired = treatmentInstance(teeth(Tooth.T16), condition);

        assertThatThrownBy(() -> service.validateExplicitCloseAllowed(notRequired))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((AppException) e).getUserMessageKey()).contains("validateExplicitCloseAllowed"));
    }

    // ─── validateReferenceNotTerminal ─────────────────────────────────────────

    /**
     * CASO: Tratamiento referenciado en FINISHED, REPLACED, CANCELED o PREEXISTING.
     * Regla: D-38 — nada nuevo cuelga de un tratamiento ya cerrado.
     * Validación: ConflictException (409) en los cuatro.
     */
    @ParameterizedTest
    @EnumSource(value = TreatmentConditionType.class, names = {"FINISHED", "REPLACED", "CANCELED", "PREEXISTING"})
    void validateReferenceNotTerminal_terminalStates_throwsConflictException(TreatmentConditionType condition) {
        TreatmentInstance terminal = treatmentInstance(teeth(Tooth.T16), condition);

        assertThatThrownBy(() -> service.validateReferenceNotTerminal(terminal)).isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Tratamiento referenciado en REQUIRED.
     * Regla: D-38.
     * Validación: no lanza.
     */
    @Test
    void validateReferenceNotTerminal_requiredTreatment_isAccepted() {
        TreatmentInstance required = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REQUIRED);

        assertThatCode(() -> service.validateReferenceNotTerminal(required)).doesNotThrowAnyException();
    }

    /**
     * CASO: Tratamiento referenciado en IN_PROGRESS.
     * Regla: D-38.
     * Validación: no lanza.
     */
    @Test
    void validateReferenceNotTerminal_inProgressTreatment_isAccepted() {
        TreatmentInstance inProgress = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.IN_PROGRESS);

        assertThatCode(() -> service.validateReferenceNotTerminal(inProgress)).doesNotThrowAnyException();
    }

    // ─── validateOriginState ──────────────────────────────────────────────────

    /**
     * CASO: Origen de prótesis FINISHED o PREEXISTING (el flujo de Alan).
     * Regla: D-38 — el origen tiene que estar FINISHED o PREEXISTING.
     * Validación: no lanza en ninguno.
     */
    @ParameterizedTest
    @EnumSource(value = TreatmentConditionType.class, names = {"FINISHED", "PREEXISTING"})
    void validateOriginState_originFinishedOrPreexisting_isAccepted(TreatmentConditionType condition) {
        TreatmentInstance origin = treatmentInstance(teeth(Tooth.T12, Tooth.T13, Tooth.T14), condition);

        assertThatCode(() -> service.validateOriginState(origin)).doesNotThrowAnyException();
    }

    /**
     * CASO: Origen IN_PROGRESS (la Colocación todavía está en curso).
     * Regla: D-38 — una prótesis que se está colocando no recibe Agregado/Compostura.
     * Validación: ConflictException (409).
     */
    @Test
    void validateOriginState_originInProgress_throwsConflictException() {
        TreatmentInstance origin = treatmentInstance(teeth(Tooth.T12, Tooth.T13, Tooth.T14), TreatmentConditionType.IN_PROGRESS);

        assertThatThrownBy(() -> service.validateOriginState(origin)).isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Origen REQUIRED, REPLACED o CANCELED.
     * Regla: D-38 — requerida sin colocar o abandonada no recibe Agregado.
     * Validación: ConflictException (409) en los tres.
     */
    @ParameterizedTest
    @EnumSource(value = TreatmentConditionType.class, names = {"REQUIRED", "REPLACED", "CANCELED"})
    void validateOriginState_originRequiredReplacedOrCanceled_throwsConflictException(TreatmentConditionType condition) {
        TreatmentInstance origin = treatmentInstance(teeth(Tooth.T12), condition);

        assertThatThrownBy(() -> service.validateOriginState(origin)).isInstanceOf(ConflictException.class);
    }

    // ─── validateLocationCollision ────────────────────────────────────────────

    /**
     * CASO: Tratamiento nuevo con quadrant UPPER_RIGHT (REQUIRED) y un activo del paciente sobre el diente 16 IN_PROGRESS.
     * Regla: D-35 — el cuadrante cubre al diente 16.
     * Validación: ConflictException (409) con la ubicación como argumento del mensaje.
     */
    @Test
    void validateLocationCollision_quadrantCoveredByExistingTooth_throwsConflictException() {
        TreatmentInstance nuevo = treatmentInstance(quadrant(Quadrant.UPPER_RIGHT), TreatmentConditionType.REQUIRED);
        TreatmentInstance activo = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.IN_PROGRESS);

        assertThatThrownBy(() -> service.validateLocationCollision(List.of(nuevo), List.of(activo)))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((AppException) e).getUserArgs()).isNotEmpty());
    }

    /**
     * CASO: T1 PREEXISTING en 12/13/14 figura en la lista de activos; tratamiento nuevo IN_PROGRESS sobre el 13.
     * Regla: D-51 — PREEXISTING nunca cuenta como activo persistido (la lista real no lo incluye, y aunque llegara no cuenta).
     * Validación: no lanza. El nuevo figura también en activas (como queda tras el flush) y no choca consigo mismo.
     */
    @Test
    void validateLocationCollision_preexistingNotCountedAsActive_isAccepted() {
        TreatmentInstance preexisting = treatmentInstance(teeth(Tooth.T12, Tooth.T13, Tooth.T14), TreatmentConditionType.PREEXISTING);
        TreatmentInstance nuevo = treatmentInstance(teeth(Tooth.T13), TreatmentConditionType.IN_PROGRESS);

        assertThatCode(() -> service.validateLocationCollision(List.of(nuevo), List.of(preexisting, nuevo)))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Tratamiento nuevo PREEXISTING sobre el 13 (id 1, solo en los nuevos) y un activo IN_PROGRESS real sobre el 13
     * (id 2, solo en la lista de activos).
     * Regla: D-51 — algo activo bloquea que nazca un PREEXISTING sobre esa ubicación; el activo no está en los
     * tratamientos nuevos, así que no se saltea.
     * Validación: ConflictException (409). Los ids son explícitos y distintos: sin id, Mockito devuelve 0L y el activo
     * pasaría por "coincidente por id" con la propia prótesis.
     */
    @Test
    void validateLocationCollision_newPreexistingOverActive_throwsConflictException() {
        TreatmentInstance nuevo = treatmentInstance(1L, teeth(Tooth.T13), TreatmentConditionType.PREEXISTING);
        TreatmentInstance activo = treatmentInstance(2L, teeth(Tooth.T13), TreatmentConditionType.IN_PROGRESS);

        assertThatThrownBy(() -> service.validateLocationCollision(List.of(nuevo), List.of(activo)))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Dos tratamientos nuevos REQUIRED, uno sobre el 16 y otro sobre quadrant UPPER_RIGHT, ambos en la lista de activos.
     * Regla: D-53 — dos nuevos activos solapados en el mismo request chocan entre sí.
     * Validación: ConflictException (409).
     */
    @Test
    void validateLocationCollision_twoNewActiveOverlappingInSameRequest_throwsConflictException() {
        TreatmentInstance sobreDiente = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REQUIRED);
        TreatmentInstance sobreCuadrante = treatmentInstance(quadrant(Quadrant.UPPER_RIGHT), TreatmentConditionType.REQUIRED);

        assertThatThrownBy(() -> service.validateLocationCollision(
                List.of(sobreDiente, sobreCuadrante), List.of(sobreDiente, sobreCuadrante)))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Un solo tratamiento nuevo que además figura en la lista de activos.
     * Regla: D-35 — se excluye a sí mismo (por identidad).
     * Validación: no lanza.
     */
    @Test
    void validateLocationCollision_newTreatmentExcludesItself_isAccepted() {
        TreatmentInstance nuevo = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REQUIRED);

        assertThatCode(() -> service.validateLocationCollision(List.of(nuevo), List.of(nuevo))).doesNotThrowAnyException();
    }

    /**
     * CASO: Tratamiento sobre el 11 nacido y derivado a un estado terminal (FINISHED como la extracción de Alan, REPLACED
     * o CANCELED), y un Agregado nuevo IN_PROGRESS sobre el 11.
     * Regla: D-35/D-53 — los terminales no se evalúan ni cuentan como activos, sea cual sea el estado terminal.
     * Las activas traen solo el Agregado.
     * Validación: no lanza en ninguno de los tres. Si el salteo se implementara solo para FINISHED, un REPLACED o un
     * CANCELED solapado daría 409 y el flujo de sustitución (D-40) quedaría rechazado.
     */
    @ParameterizedTest
    @EnumSource(value = TreatmentConditionType.class, names = {"FINISHED", "REPLACED", "CANCELED"})
    void validateLocationCollision_newTreatmentEndedTerminal_isNotEvaluated(TreatmentConditionType terminal) {
        TreatmentInstance extraction = treatmentInstance(teeth(Tooth.T11), terminal);
        TreatmentInstance agregado = treatmentInstance(teeth(Tooth.T11), TreatmentConditionType.IN_PROGRESS);

        assertThatCode(() -> service.validateLocationCollision(List.of(extraction, agregado), List.of(agregado)))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Nuevo diente 16 cara TOP y activo diente 16 cara BOTTOM; y luego una de las dos sin cara.
     * Regla: D-35 — caras distintas del mismo diente no chocan; diente completo contra una cara sí.
     * Validación: el primer caso no lanza; con una de las dos sin cara lanza ConflictException (409) en los dos órdenes:
     * nueva con cara contra activa sin cara, y nueva sin cara contra activa con cara.
     */
    @Test
    void validateLocationCollision_differentFacesOfSameTooth_isAccepted() {
        TreatmentInstance nuevoTop = treatmentInstance(toothFace(Tooth.T16, ToothFace.TOP), TreatmentConditionType.REQUIRED);
        TreatmentInstance activoBottom = treatmentInstance(toothFace(Tooth.T16, ToothFace.BOTTOM), TreatmentConditionType.IN_PROGRESS);
        TreatmentInstance activoSinCara = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.IN_PROGRESS);
        TreatmentInstance nuevoSinCara = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REQUIRED);
        TreatmentInstance activoTop = treatmentInstance(toothFace(Tooth.T16, ToothFace.TOP), TreatmentConditionType.IN_PROGRESS);

        assertThatCode(() -> service.validateLocationCollision(List.of(nuevoTop), List.of(activoBottom)))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> service.validateLocationCollision(List.of(nuevoTop), List.of(activoSinCara)))
                .as("nueva con cara contra activa sin cara")
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.validateLocationCollision(List.of(nuevoSinCara), List.of(activoTop)))
                .as("nueva sin cara contra activa con cara")
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Nuevo en el 16 y activo en el 26.
     * Regla: D-35 — sin solapamiento no hay colisión.
     * Validación: no lanza.
     */
    @Test
    void validateLocationCollision_noOverlap_isAccepted() {
        TreatmentInstance nuevo = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REQUIRED);
        TreatmentInstance activo = treatmentInstance(teeth(Tooth.T26), TreatmentConditionType.IN_PROGRESS);

        assertThatCode(() -> service.validateLocationCollision(List.of(nuevo), List.of(activo))).doesNotThrowAnyException();
    }

    /**
     * CASO: Prótesis nueva PREEXISTING de Alan sobre el 12/13/14 y una compostura nueva sobre el 13 (REQUIRED o
     * IN_PROGRESS), las dos con id distinto y no nulo, las dos en los tratamientos nuevos y la compostura también en la
     * lista de activos.
     * Regla: D-51 — el PREEXISTING no se compara contra un activo que nació en el mismo request.
     * Validación: no lanza en ninguno de los dos casos. Si el PREEXISTING se comparara contra todos los activos, la
     * compostura nacida en el mismo request daría 409.
     */
    @ParameterizedTest
    @EnumSource(value = TreatmentConditionType.class, names = {"REQUIRED", "IN_PROGRESS"})
    void validateLocationCollision_newPreexistingOverActiveInSameRequest_isAccepted(TreatmentConditionType condition) {
        TreatmentInstance protesis = treatmentInstance(1L, teeth(Tooth.T12, Tooth.T13, Tooth.T14), TreatmentConditionType.PREEXISTING);
        TreatmentInstance compostura = treatmentInstance(2L, teeth(Tooth.T13), condition);

        assertThatCode(() -> service.validateLocationCollision(List.of(protesis, compostura), List.of(compostura)))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Prótesis nueva PREEXISTING sobre el 13 y otro tratamiento nuevo REQUIRED sobre el 26, los dos en los
     * tratamientos nuevos; un activo IN_PROGRESS ya guardado sobre el 13 (id distinto), solo en la lista de activos.
     * Regla: D-51 — la excepción solo saltea los activos que están en los tratamientos nuevos del request.
     * Validación: ConflictException (409). Si se salteara todo cruce del PREEXISTING apenas hay nuevos en el request, o
     * se salteara un activo de otro id, el activo guardado dejaría pasar la colisión.
     */
    @Test
    void validateLocationCollision_newPreexistingOverPersistedActiveWithOtherNewInRequest_throwsConflictException() {
        TreatmentInstance protesis = treatmentInstance(1L, teeth(Tooth.T13), TreatmentConditionType.PREEXISTING);
        TreatmentInstance otroNuevo = treatmentInstance(2L, teeth(Tooth.T26), TreatmentConditionType.REQUIRED);
        TreatmentInstance activoGuardado = treatmentInstance(3L, teeth(Tooth.T13), TreatmentConditionType.IN_PROGRESS);

        assertThatThrownBy(() -> service.validateLocationCollision(List.of(protesis, otroNuevo), List.of(activoGuardado)))
                .isInstanceOf(ConflictException.class);
    }

    // ─── validateReplacementTraceability ──────────────────────────────────────

    /**
     * CASO: Tratamiento REQUIRED sobre el 16 cerrado con REPLACED explícito, sin ningún tratamiento ni prestación nuevos.
     * Regla: D-40 Caso A — el reemplazo tiene que venir en el mismo request.
     * Validación: ConflictException (409) "Debe indicarse el reemplazo".
     */
    @Test
    void validateReplacementTraceability_requiredWithoutReplacement_throwsConflictException() {
        TreatmentInstance replaced = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REPLACED);

        assertThatThrownBy(() -> service.validateReplacementTraceability(List.of(replaced), List.of(), List.of(), List.of()))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((AppException) e).getUserMessageKey()).contains("validateReplacementTraceability"));
    }

    /**
     * CASO: REPLACED explícito del 16 y un TreatmentInstance nuevo de otro Treatment sobre quadrant UPPER_RIGHT.
     * Regla: D-40 — una ubicación más amplia que incluye el 16 cubre el reemplazo.
     * Validación: no lanza.
     */
    @Test
    void validateReplacementTraceability_requiredReplacedByNewTreatmentCoveringLocation_isAccepted() {
        TreatmentInstance replaced = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REPLACED, treatment(1L));
        TreatmentInstance nuevo = treatmentInstance(quadrant(Quadrant.UPPER_RIGHT), TreatmentConditionType.REQUIRED, treatment(2L));

        assertThatCode(() -> service.validateReplacementTraceability(List.of(replaced), List.of(), List.of(nuevo), List.of()))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: REPLACED explícito del 16 y una prestación nueva con scope propio tooth [16].
     * Regla: D-40 Caso A — una prestación nueva con Scope propio también cubre.
     * Validación: no lanza.
     */
    @Test
    void validateReplacementTraceability_requiredReplacedByOwnScopePrestation_isAccepted() {
        TreatmentInstance replaced = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REPLACED);
        PrestationInstance nueva = prestation(PrestationInstanceStatus.IN_PROGRESS, null, teeth(Tooth.T16));

        assertThatCode(() -> service.validateReplacementTraceability(List.of(replaced), List.of(), List.of(), List.of(nueva)))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Prestación A REPLACED cuyo tratamiento T sigue IN_PROGRESS (derivado); prestación nueva que referencia el mismo T.
     * Regla: D-40 Caso B (a) — la prestación nueva sobre el mismo TreatmentInstance cubre por construcción (hereda su ubicación).
     * Validación: no lanza. La prestación nueva tiene scope propio vacío: la cobertura sale del tratamiento referenciado.
     */
    @Test
    void validateReplacementTraceability_singleLivePrestationReplacedByNewPrestationOnSameTreatment_isAccepted() {
        TreatmentInstance t = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.IN_PROGRESS);
        PrestationInstance replacedPrestation = prestation(PrestationInstanceStatus.REPLACED, t, Scope.empty());
        PrestationInstance nueva = prestation(PrestationInstanceStatus.IN_PROGRESS, t, Scope.empty());

        assertThatCode(() -> service.validateReplacementTraceability(List.of(), List.of(replacedPrestation), List.of(), List.of(nueva)))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: T100 sigue IN_PROGRESS (la prestación B está viva); A queda REPLACED; nace un TreatmentInstance de otro Treatment sobre el 16.
     * Regla: D-40 Caso C — no puede haber dos Treatment distintos activos sobre la misma ubicación; el rechazo
     * es el específico ("no puede reemplazarse ya que cuenta con prestaciones vigentes") y no el de colisión.
     * Validación: ConflictException (409) de validateReplacementTraceability, no de validateLocationCollision.
     */
    @Test
    void validateReplacementTraceability_replacedWithDifferentTreatmentWhileOtherStillLive_throwsConflictException() {
        TreatmentInstance t100 = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.IN_PROGRESS, treatment(1L));
        PrestationInstance prestationA = prestation(PrestationInstanceStatus.REPLACED, t100, Scope.empty());
        TreatmentInstance otroTratamiento = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REQUIRED, treatment(2L));

        assertThatThrownBy(() -> service.validateReplacementTraceability(
                List.of(), List.of(prestationA), List.of(otroTratamiento), List.of()))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> {
                    String key = ((AppException) e).getUserMessageKey();
                    assertThat(key).contains("validateReplacementTraceability");
                    assertThat(key).doesNotContain("validateLocationCollision");
                });
    }

    /**
     * CASO: REPLACED del 16 y el nuevo tratamiento está sobre el 26.
     * Regla: D-40 — la ubicación nueva nunca puede ser una que no toque el diente reemplazado.
     * Validación: ConflictException (409).
     */
    @Test
    void validateReplacementTraceability_replacementNotCoveringReplacedLocation_throwsConflictException() {
        TreatmentInstance replaced = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REPLACED, treatment(1L));
        TreatmentInstance nuevo = treatmentInstance(teeth(Tooth.T26), TreatmentConditionType.REQUIRED, treatment(2L));

        assertThatThrownBy(() -> service.validateReplacementTraceability(List.of(replaced), List.of(), List.of(nuevo), List.of()))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Caso B (b): T100 sobre el 16 con una sola prestación viva (la Restauración de resina) enviada REPLACED; el
     * tratamiento queda cerrado REPLACED; nace un TreatmentInstance de otro Treatment sobre el 16.
     * Regla: D-40 Caso B (b) — la restricción del Caso C (no hay dos Treatment distintos) solo aplica si queda una
     * prestación viva; sin ninguna viva, alcanza con que el tratamiento nuevo cubra la ubicación reemplazada.
     * Validación: no lanza. Si se aplicara siempre la restricción del Caso C, daría 409.
     */
    @Test
    void validateReplacementTraceability_lastLivePrestationReplacedClosedTreatmentCoveredByNewTreatment_isAccepted() {
        TreatmentInstance t100 = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REPLACED, treatment(1L));
        PrestationInstance restauracion = prestation(PrestationInstanceStatus.REPLACED, t100, Scope.empty());
        TreatmentInstance nuevo = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REQUIRED, treatment(2L));

        assertThatCode(() -> service.validateReplacementTraceability(List.of(), List.of(restauracion), List.of(nuevo), List.of()))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Igual que el caso anterior (T100 cerrado REPLACED sin prestaciones vivas), pero el tratamiento nuevo está sobre el 26.
     * Regla: D-40 — el reemplazo tiene que cubrir la ubicación reemplazada aunque el tratamiento ya haya cerrado.
     * Validación: ConflictException (409). Si se ignorara la cobertura cuando el tratamiento cerró, no lanzaría.
     */
    @Test
    void validateReplacementTraceability_lastLivePrestationReplacedClosedTreatmentNewNotCovering_throwsConflictException() {
        TreatmentInstance t100 = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REPLACED, treatment(1L));
        PrestationInstance restauracion = prestation(PrestationInstanceStatus.REPLACED, t100, Scope.empty());
        TreatmentInstance nuevo = treatmentInstance(teeth(Tooth.T26), TreatmentConditionType.REQUIRED, treatment(2L));

        assertThatThrownBy(() -> service.validateReplacementTraceability(List.of(), List.of(restauracion), List.of(nuevo), List.of()))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Caso C: T100 sigue IN_PROGRESS (la prestación B está viva), la A queda REPLACED, y la prestación nueva
     * referencia otro TreatmentInstance existente (sobre el 16, para que la cobertura geométrica por sí sola la aceptaría).
     * Regla: D-40 Caso C — con otra prestación viva, el reemplazo solo puede ser una prestación nueva sobre el mismo
     * tratamiento o con scope propio. (Supuesto: deducido del design, que no nombra este caso literal.)
     * Validación: ConflictException (409).
     */
    @Test
    void validateReplacementTraceability_replacedWhileOtherLiveAndNewPrestationReferencesOtherTreatmentInstance_throwsConflictException() {
        TreatmentInstance t100 = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.IN_PROGRESS, treatment(1L));
        PrestationInstance prestationA = prestation(PrestationInstanceStatus.REPLACED, t100, Scope.empty());
        TreatmentInstance otroTratamiento = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.IN_PROGRESS, treatment(2L));
        PrestationInstance nueva = prestation(PrestationInstanceStatus.IN_PROGRESS, otroTratamiento, Scope.empty());

        assertThatThrownBy(() -> service.validateReplacementTraceability(List.of(), List.of(prestationA), List.of(), List.of(nueva)))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Listas de reemplazados vacías (solo cancelaciones o avances).
     * Regla: D-40 — CANCELED nunca entra a la trazabilidad; sin sujetos reemplazados no hay nada que validar.
     * Validación: no lanza, tenga o no cosas nuevas el request.
     */
    @Test
    void validateReplacementTraceability_noReplacedSubjects_isAccepted() {
        TreatmentInstance nuevo = treatmentInstance(teeth(Tooth.T26), TreatmentConditionType.REQUIRED);

        assertThatCode(() -> service.validateReplacementTraceability(List.of(), List.of(), List.of(), List.of()))
                .doesNotThrowAnyException();
        assertThatCode(() -> service.validateReplacementTraceability(List.of(), List.of(), List.of(nuevo), List.of()))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: REPLACED explícito del 16 de Bety; nacen dos TreatmentInstance, el primero sobre el 26 (no cubre) y el
     * segundo sobre quadrant UPPER_RIGHT (cubre el 16).
     * Regla: D-40 — alcanza con que al menos una de las entidades nuevas cubra la ubicación reemplazada.
     * Validación: no lanza. Con un allMatch sobre los nuevos daría 409 por el del 26.
     */
    @Test
    void validateReplacementTraceability_twoNewTreatmentsOnlySecondCovers_isAccepted() {
        TreatmentInstance replaced = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REPLACED, treatment(1L));
        TreatmentInstance noCubre = treatmentInstance(teeth(Tooth.T26), TreatmentConditionType.REQUIRED, treatment(2L));
        TreatmentInstance cubre = treatmentInstance(quadrant(Quadrant.UPPER_RIGHT), TreatmentConditionType.REQUIRED, treatment(3L));

        assertThatCode(() -> service.validateReplacementTraceability(List.of(replaced), List.of(), List.of(noCubre, cubre), List.of()))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Dos TreatmentInstance REQUIRED cerrados con REPLACED explícito, uno sobre el 16 y otro sobre el 26; nace un
     * solo TreatmentInstance, sobre el 16.
     * Regla: D-40 — cada sujeto reemplazado tiene que quedar cubierto por algún nuevo.
     * Validación: ConflictException (409). Con un anyMatch sobre los reemplazados, el del 16 alcanzaría y no lanzaría.
     */
    @Test
    void validateReplacementTraceability_twoReplacedOnlyOneCovered_throwsConflictException() {
        TreatmentInstance replaced16 = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REPLACED, treatment(1L));
        TreatmentInstance replaced26 = treatmentInstance(teeth(Tooth.T26), TreatmentConditionType.REPLACED, treatment(1L));
        TreatmentInstance nuevo = treatmentInstance(teeth(Tooth.T16), TreatmentConditionType.REQUIRED, treatment(2L));

        assertThatThrownBy(() -> service.validateReplacementTraceability(
                List.of(replaced16, replaced26), List.of(), List.of(nuevo), List.of()))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((AppException) e).getUserMessageKey()).contains("validateReplacementTraceability"));
    }
}
