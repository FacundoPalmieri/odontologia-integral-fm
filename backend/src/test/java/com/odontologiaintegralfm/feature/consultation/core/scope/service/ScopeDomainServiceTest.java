package com.odontologiaintegralfm.feature.consultation.core.scope.service;

import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.ToothFace;
import com.odontologiaintegralfm.feature.consultation.core.scope.model.Scope;
import com.odontologiaintegralfm.shared.exception.AppException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Geometría pura: sin colaboradores ni mocks. Scope es un valor y se construye real.
 */
@Tag("unit")
class ScopeDomainServiceTest {

    private final ScopeDomainService service = new ScopeDomainService();

    // ─── helpers ──────────────────────────────────────────────────────────────

    private static Scope teeth(Tooth... teeth) {
        return Scope.build(new ArrayList<>(List.of(teeth)), null, null, null, false);
    }

    private static Scope teeth(List<Tooth> teeth) {
        return Scope.build(new ArrayList<>(teeth), null, null, null, false);
    }

    private static Scope toothFace(Tooth tooth, ToothFace face) {
        return Scope.build(new ArrayList<>(List.of(tooth)), face, null, null, false);
    }

    private static Scope quadrant(Quadrant quadrant) {
        return Scope.build(new ArrayList<>(), null, quadrant, null, false);
    }

    private static Scope maxillary(Maxillary maxillary) {
        return Scope.build(new ArrayList<>(), null, null, maxillary, false);
    }

    private static Scope fullMouth() {
        return Scope.build(new ArrayList<>(), null, null, null, true);
    }

    private static List<Tooth> union(Quadrant a, Quadrant b) {
        List<Tooth> all = new ArrayList<>(a.getTeeth());
        all.addAll(b.getTeeth());
        return all;
    }

    // ─── validateToothListDoesNotCompleteLevel ────────────────────────────────

    /**
     * CASO: La lista de dientes es exactamente los dientes de un cuadrante (los 4 permanentes de 8 y los 4 temporales de 5).
     * Regla: D-08 — el front debe declarar el nivel explícito, no la lista disfrazada.
     * Validación: ConflictException (409) en los 8 cuadrantes, con la clave de "compone un nivel superior".
     */
    @ParameterizedTest
    @EnumSource(Quadrant.class)
    void validateToothListDoesNotCompleteLevel_toothListCompletesQuadrant_throwsConflictException(Quadrant quadrant) {
        Scope scope = teeth(new ArrayList<>(quadrant.getTeeth()));

        assertThatThrownBy(() -> service.validateToothListDoesNotCompleteLevel(scope))
                .isInstanceOf(ConflictException.class)
                .satisfies(e -> assertThat(((AppException) e).getUserMessageKey())
                        .contains("validateToothListDoesNotCompleteLevel"));
    }

    /**
     * CASO: Los dientes de un cuadrante (los 8 cuadrantes) en orden inverso, y los 16 dientes permanentes superiores
     * mezclados (el front manda los dientes en el orden en que el odontólogo los tocó).
     * Regla: D-08 — la comparación es por conjunto, no por lista ordenada.
     * Validación: ConflictException (409) en todos. Si la implementación comparara con List.equals, el orden distinto
     * dejaría pasar la lista disfrazada.
     */
    @ParameterizedTest
    @EnumSource(Quadrant.class)
    void validateToothListDoesNotCompleteLevel_completeLevelInAnyOrder_throwsConflictException(Quadrant quadrant) {
        List<Tooth> reversed = new ArrayList<>(quadrant.getTeeth());
        java.util.Collections.reverse(reversed);
        List<Tooth> maxillaryShuffled = union(Quadrant.UPPER_RIGHT, Quadrant.UPPER_LEFT);
        java.util.Collections.shuffle(maxillaryShuffled, new java.util.Random(7));

        assertThatThrownBy(() -> service.validateToothListDoesNotCompleteLevel(teeth(reversed)))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.validateToothListDoesNotCompleteLevel(teeth(maxillaryShuffled)))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Los 7 primeros dientes de UPPER_RIGHT más uno repetido (8 elementos, 7 distintos; falta el 18).
     * Regla: D-08 — solo la coincidencia exacta del conjunto con el nivel es rechazada.
     * Validación: no lanza. Un chequeo por cantidad (size == 8) más containsAll lo rechazaría por error.
     */
    @Test
    void validateToothListDoesNotCompleteLevel_eightTeethWithRepeated_isAccepted() {
        List<Tooth> sevenDistinct = new ArrayList<>(Quadrant.UPPER_RIGHT.getTeeth()).subList(0, 7);
        List<Tooth> eightWithRepeated = new ArrayList<>(sevenDistinct);
        eightWithRepeated.add(sevenDistinct.get(0));
        Scope scope = teeth(eightWithRepeated);

        assertThat(scope.getTooth()).hasSize(8);
        assertThatCode(() -> service.validateToothListDoesNotCompleteLevel(scope)).doesNotThrowAnyException();
    }

    /**
     * CASO: Los 16 dientes permanentes superiores (unión de UPPER_RIGHT y UPPER_LEFT).
     * Regla: D-08 / decisión 7 — un maxilar completo en dentición permanente también es un nivel.
     * Validación: ConflictException (409).
     */
    @Test
    void validateToothListDoesNotCompleteLevel_toothListCompletesMaxillaryPermanent_throwsConflictException() {
        Scope scope = teeth(union(Quadrant.UPPER_RIGHT, Quadrant.UPPER_LEFT));

        assertThat(scope.getTooth()).hasSize(16);
        assertThatThrownBy(() -> service.validateToothListDoesNotCompleteLevel(scope))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: Los 10 dientes temporales inferiores (unión de LOWER_LEFT_TEMPORARY y LOWER_RIGHT_TEMPORARY).
     * Regla: D-08 / decisión 7 — un maxilar completo en dentición temporal también es un nivel.
     * Validación: ConflictException (409).
     */
    @Test
    void validateToothListDoesNotCompleteLevel_toothListCompletesMaxillaryTemporary_throwsConflictException() {
        Scope scope = teeth(union(Quadrant.LOWER_LEFT_TEMPORARY, Quadrant.LOWER_RIGHT_TEMPORARY));

        assertThat(scope.getTooth()).hasSize(10);
        assertThatThrownBy(() -> service.validateToothListDoesNotCompleteLevel(scope))
                .isInstanceOf(ConflictException.class);
    }

    /**
     * CASO: 7 de los 8 dientes de UPPER_RIGHT.
     * Regla: D-08 — solo la coincidencia exacta con el nivel es rechazada.
     * Validación: no lanza.
     */
    @Test
    void validateToothListDoesNotCompleteLevel_sevenOfEightQuadrantTeeth_isAccepted() {
        List<Tooth> seven = new ArrayList<>(Quadrant.UPPER_RIGHT.getTeeth()).subList(0, 7);
        Scope scope = teeth(new ArrayList<>(seven));

        assertThat(scope.getTooth()).hasSize(7);
        assertThatCode(() -> service.validateToothListDoesNotCompleteLevel(scope)).doesNotThrowAnyException();
    }

    /**
     * CASO: Lista parcial [11, 12, 13] y un único diente [16].
     * Regla: D-08.
     * Validación: no lanza en ninguno.
     */
    @Test
    void validateToothListDoesNotCompleteLevel_partialToothList_isAccepted() {
        assertThatCode(() -> service.validateToothListDoesNotCompleteLevel(teeth(Tooth.T11, Tooth.T12, Tooth.T13)))
                .doesNotThrowAnyException();
        assertThatCode(() -> service.validateToothListDoesNotCompleteLevel(teeth(Tooth.T16)))
                .doesNotThrowAnyException();
    }

    /**
     * CASO: Scope con nivel geométrico explícito (quadrant = UPPER_RIGHT) y sin lista de dientes.
     * Regla: D-08 solo evalúa listas de dientes; el nivel explícito es justo lo que el front debe mandar.
     * Validación: no lanza.
     */
    @Test
    void validateToothListDoesNotCompleteLevel_explicitGeometricLevel_isNotEvaluated() {
        assertThatCode(() -> service.validateToothListDoesNotCompleteLevel(quadrant(Quadrant.UPPER_RIGHT)))
                .doesNotThrowAnyException();
    }

    // ─── overlaps ─────────────────────────────────────────────────────────────

    /**
     * CASO: [16] contra [16], ambos sin cara.
     * Regla: D-35 — mismo diente completo se solapa.
     * Validación: true.
     */
    @Test
    void overlaps_sameToothWithoutFace_returnsTrue() {
        assertThat(service.overlaps(teeth(Tooth.T16), teeth(Tooth.T16))).isTrue();
    }

    /**
     * CASO: [16] contra [26].
     * Regla: D-35 — dientes distintos no se solapan.
     * Validación: false.
     */
    @Test
    void overlaps_differentTeeth_returnsFalse() {
        assertThat(service.overlaps(teeth(Tooth.T16), teeth(Tooth.T26))).isFalse();
    }

    /**
     * CASO: [16] cara TOP contra [16] sin cara (y a la inversa).
     * Regla: D-35 — un diente completo incluye cualquiera de sus caras; la relación es simétrica.
     * Validación: true en ambos órdenes.
     */
    @Test
    void overlaps_toothWithFaceAndWholeTooth_returnsTrue() {
        Scope withFace = toothFace(Tooth.T16, ToothFace.TOP);
        Scope whole = teeth(Tooth.T16);

        assertThat(service.overlaps(withFace, whole)).isTrue();
        assertThat(service.overlaps(whole, withFace)).isTrue();
    }

    /**
     * CASO: [16] cara TOP contra [16] cara BOTTOM.
     * Regla: D-35 — caras distintas del mismo diente no se solapan.
     * Validación: false.
     */
    @Test
    void overlaps_sameToothDifferentFaces_returnsFalse() {
        assertThat(service.overlaps(toothFace(Tooth.T16, ToothFace.TOP), toothFace(Tooth.T16, ToothFace.BOTTOM))).isFalse();
    }

    /**
     * CASO: [16] contra quadrant UPPER_RIGHT, y contra UPPER_LEFT.
     * Regla: D-35 — un cuadrante incluye sus dientes.
     * Validación: true contra UPPER_RIGHT (en ambos órdenes), false contra UPPER_LEFT.
     */
    @Test
    void overlaps_toothInsideQuadrant_returnsTrue() {
        assertThat(service.overlaps(teeth(Tooth.T16), quadrant(Quadrant.UPPER_RIGHT))).isTrue();
        assertThat(service.overlaps(quadrant(Quadrant.UPPER_RIGHT), teeth(Tooth.T16))).isTrue();
        assertThat(service.overlaps(teeth(Tooth.T16), quadrant(Quadrant.UPPER_LEFT))).isFalse();
    }

    /**
     * CASO: quadrant LOWER_LEFT contra maxillary LOWER.
     * Regla: D-35 — un maxilar incluye sus cuadrantes.
     * Validación: true.
     */
    @Test
    void overlaps_quadrantInsideMaxillary_returnsTrue() {
        assertThat(service.overlaps(quadrant(Quadrant.LOWER_LEFT), maxillary(Maxillary.LOWER))).isTrue();
    }

    /**
     * CASO: maxillary UPPER contra fullMouth = true.
     * Regla: D-35 — boca completa incluye todo.
     * Validación: true en ambos órdenes.
     */
    @Test
    void overlaps_maxillaryAndFullMouth_returnsTrue() {
        assertThat(service.overlaps(maxillary(Maxillary.UPPER), fullMouth())).isTrue();
        assertThat(service.overlaps(fullMouth(), maxillary(Maxillary.UPPER))).isTrue();
    }

    /**
     * CASO: maxillary UPPER contra LOWER.
     * Regla: D-35 — maxilares opuestos no se solapan.
     * Validación: false.
     */
    @Test
    void overlaps_oppositeMaxillaries_returnsFalse() {
        assertThat(service.overlaps(maxillary(Maxillary.UPPER), maxillary(Maxillary.LOWER))).isFalse();
    }

    /**
     * CASO: [16] (permanente) contra [55] (temporal).
     * Regla: D-35 — son dientes distintos aunque estén en el mismo maxilar.
     * Validación: false.
     */
    @Test
    void overlaps_temporaryAndPermanentTeeth_returnsFalse() {
        assertThat(service.overlaps(teeth(Tooth.T16), teeth(Tooth.T55))).isFalse();
    }

    /** Afirma el resultado esperado en los dos órdenes (la relación es simétrica). */
    private void assertOverlapsBothOrders(Scope a, Scope b, boolean expected, String label) {
        assertThat(service.overlaps(a, b)).as(label + " (a, b)").isEqualTo(expected);
        assertThat(service.overlaps(b, a)).as(label + " (b, a)").isEqualTo(expected);
    }

    /**
     * CASO: Listas de varios dientes: [12,13,14] contra [13]; [16,17] contra [17,18]; [12,13] contra [14,15].
     * Regla: D-35 — dos listas se solapan si comparten al menos un diente, en cualquier posición.
     * Validación: true, true, false, y cada uno en ambos órdenes. Un chequeo que mirara solo el primer diente,
     * o que comparara las listas con equals, daría false en los dos primeros.
     */
    @Test
    void overlaps_multipleTeethLists_returnsExpected() {
        assertOverlapsBothOrders(teeth(Tooth.T12, Tooth.T13, Tooth.T14), teeth(Tooth.T13), true, "[12,13,14] vs [13]");
        assertOverlapsBothOrders(teeth(Tooth.T16, Tooth.T17), teeth(Tooth.T17, Tooth.T18), true, "[16,17] vs [17,18]");
        assertOverlapsBothOrders(teeth(Tooth.T12, Tooth.T13), teeth(Tooth.T14, Tooth.T15), false, "[12,13] vs [14,15]");
    }

    /**
     * CASO: Niveles geométricos cruzados: diente [16] contra maxillary LOWER (false) y UPPER (true); diente [16] contra
     * fullMouth (true); quadrant UPPER_RIGHT contra fullMouth (true); UPPER_RIGHT contra UPPER_LEFT (false);
     * quadrant LOWER_LEFT contra maxillary LOWER (true) y UPPER (false); cuadrante temporal UPPER_RIGHT_TEMPORARY
     * contra el diente permanente [16] (false).
     * Regla: D-35 — un nivel incluye a los niveles y dientes que contiene; lo demás no se solapa.
     * Validación: el resultado esperado de cada par, y el mismo en el orden inverso. Un cuadrante que solapara con
     * cualquier maxilar daría true en LOWER_LEFT contra UPPER.
     */
    @Test
    void overlaps_geometricLevelsBothOrders_returnsExpected() {
        assertOverlapsBothOrders(teeth(Tooth.T16), maxillary(Maxillary.LOWER), false, "[16] vs LOWER");
        assertOverlapsBothOrders(teeth(Tooth.T16), maxillary(Maxillary.UPPER), true, "[16] vs UPPER");
        assertOverlapsBothOrders(teeth(Tooth.T16), fullMouth(), true, "[16] vs fullMouth");
        assertOverlapsBothOrders(quadrant(Quadrant.UPPER_RIGHT), fullMouth(), true, "UPPER_RIGHT vs fullMouth");
        assertOverlapsBothOrders(quadrant(Quadrant.UPPER_RIGHT), quadrant(Quadrant.UPPER_LEFT), false, "UPPER_RIGHT vs UPPER_LEFT");
        assertOverlapsBothOrders(quadrant(Quadrant.LOWER_LEFT), maxillary(Maxillary.LOWER), true, "LOWER_LEFT vs LOWER");
        assertOverlapsBothOrders(quadrant(Quadrant.LOWER_LEFT), maxillary(Maxillary.UPPER), false, "LOWER_LEFT vs UPPER");
        assertOverlapsBothOrders(quadrant(Quadrant.UPPER_RIGHT_TEMPORARY), teeth(Tooth.T16), false, "UPPER_RIGHT_TEMPORARY vs [16]");
    }

    // ─── covers ───────────────────────────────────────────────────────────────

    /**
     * CASO: La nueva es [16] cara TOP y la reemplazada es [16] cara TOP; y la nueva es [16] cara TOP y la reemplazada
     * es el diente [16] completo (sin cara).
     * Regla: D-40 — si la nueva trae cara, la reemplazada tiene que traer exactamente la misma cara.
     * Validación: true en el primer caso y false en el segundo. Un chequeo que comparara caras solo cuando las dos son
     * no nulas daría true en el segundo.
     */
    @Test
    void covers_topFaceCases_returnsExpected() {
        assertThat(service.covers(toothFace(Tooth.T16, ToothFace.TOP), toothFace(Tooth.T16, ToothFace.TOP))).isTrue();
        assertThat(service.covers(toothFace(Tooth.T16, ToothFace.TOP), teeth(Tooth.T16))).isFalse();
    }

    /**
     * CASO: La ubicación amplia incluye todos los dientes de la reemplazada: quadrant UPPER_RIGHT sobre [16], y [16, 17] sobre [16].
     * Regla: D-40 — cobertura de la ubicación reemplazada.
     * Validación: covers(amplia, reemplazada) = true en ambos casos.
     */
    @Test
    void covers_widerScopeIncludesAllTeeth_returnsTrue() {
        assertThat(service.covers(quadrant(Quadrant.UPPER_RIGHT), teeth(Tooth.T16))).isTrue();
        assertThat(service.covers(teeth(Tooth.T16, Tooth.T17), teeth(Tooth.T16))).isTrue();
    }

    /**
     * CASO: La nueva es [16] y la reemplazada es quadrant UPPER_RIGHT.
     * Regla: D-40 — una ubicación más angosta no cubre a la reemplazada.
     * Validación: false.
     */
    @Test
    void covers_narrowerScope_returnsFalse() {
        assertThat(service.covers(teeth(Tooth.T16), quadrant(Quadrant.UPPER_RIGHT))).isFalse();
    }

    /**
     * CASO: Nueva [16] cara TOP, reemplazada [16] cara BOTTOM.
     * Regla: D-40 — si la nueva trae cara, la reemplazada tiene que traer la misma.
     * Validación: false.
     */
    @Test
    void covers_newFaceDifferentFromReplacedFace_returnsFalse() {
        assertThat(service.covers(toothFace(Tooth.T16, ToothFace.TOP), toothFace(Tooth.T16, ToothFace.BOTTOM))).isFalse();
    }

    /**
     * CASO: Nueva [16] sin cara, reemplazada [16] cara BOTTOM.
     * Regla: D-40 — el diente completo cubre cualquiera de sus caras.
     * Validación: true.
     */
    @Test
    void covers_newWithoutFaceCoversReplacedWithFace_returnsTrue() {
        assertThat(service.covers(teeth(Tooth.T16), toothFace(Tooth.T16, ToothFace.BOTTOM))).isTrue();
    }

    // ─── areContiguous ────────────────────────────────────────────────────────

    /**
     * CASO: [11, 12, 13] y [13, 12, 11] (desordenados).
     * Regla: D-59 — posiciones consecutivas en el orden de la arcada, sin importar el orden de entrada.
     * Validación: true en ambos.
     */
    @Test
    void areContiguous_adjacentTeeth_returnsTrue() {
        assertThat(service.areContiguous(List.of(Tooth.T11, Tooth.T12, Tooth.T13))).isTrue();
        assertThat(service.areContiguous(List.of(Tooth.T13, Tooth.T12, Tooth.T11))).isTrue();
    }

    /**
     * CASO: [13, 15] (falta el 14).
     * Regla: D-59 — dientes salteados no son contiguos.
     * Validación: false.
     */
    @Test
    void areContiguous_teethWithGap_returnsFalse() {
        assertThat(service.areContiguous(List.of(Tooth.T13, Tooth.T15))).isFalse();
    }

    /**
     * CASO: [12, 11, 21, 22] (cruza la línea media).
     * Regla: D-59 — 11 y 21 son contiguos.
     * Validación: true.
     */
    @Test
    void areContiguous_crossingMidline_returnsTrue() {
        assertThat(service.areContiguous(List.of(Tooth.T12, Tooth.T11, Tooth.T21, Tooth.T22))).isTrue();
    }

    /**
     * CASO: Dientes temporales [53, 52, 51, 61].
     * Regla: D-59 — la contigüidad también vale en dentición temporal, cruzando la línea media.
     * Validación: true.
     */
    @Test
    void areContiguous_temporaryTeeth_returnsTrue() {
        assertThat(service.areContiguous(List.of(Tooth.T53, Tooth.T52, Tooth.T51, Tooth.T61))).isTrue();
    }

    /**
     * CASO: Un único diente [16].
     * Regla: D-59 — un solo diente es trivialmente contiguo.
     * Validación: true.
     */
    @Test
    void areContiguous_singleTooth_returnsTrue() {
        assertThat(service.areContiguous(List.of(Tooth.T16))).isTrue();
    }

    /**
     * CASO: [41,31] (cruza la línea media inferior); [32,33]; [11,41] (arcadas distintas); [11,12,14] (hueco fuera del primer par).
     * Regla: D-59 — contigüidad por posición consecutiva dentro de una misma arcada, en la superior y en la inferior.
     * Validación: true, true, false, false. Una implementación que solo contemple la arcada superior, o que mire solo
     * el primer par, daría el resultado equivocado en alguno de los cuatro.
     */
    @Test
    void areContiguous_lowerArchAndMixedCases_returnsExpected() {
        assertThat(service.areContiguous(List.of(Tooth.T41, Tooth.T31))).as("[41,31]").isTrue();
        assertThat(service.areContiguous(List.of(Tooth.T32, Tooth.T33))).as("[32,33]").isTrue();
        assertThat(service.areContiguous(List.of(Tooth.T11, Tooth.T41))).as("[11,41]").isFalse();
        assertThat(service.areContiguous(List.of(Tooth.T11, Tooth.T12, Tooth.T14))).as("[11,12,14]").isFalse();
    }

    // ─── expandTeeth ──────────────────────────────────────────────────────────

    /**
     * CASO: fullMouth = true, y maxillary = UPPER.
     * Regla: D-07 — la expansión a dientes solo se hace al leer.
     * Validación: fullMouth devuelve los 52 dientes del enum; maxillary UPPER devuelve solo los de ese maxilar.
     */
    @Test
    void expandTeeth_fullMouth_returnsEveryTooth() {
        Set<Tooth> all = service.expandTeeth(fullMouth());
        Set<Tooth> upper = service.expandTeeth(maxillary(Maxillary.UPPER));

        assertThat(all).hasSize(52).containsExactlyInAnyOrder(Tooth.values());
        assertThat(upper).containsExactlyInAnyOrderElementsOf(Maxillary.UPPER.getTeeth());
    }
}
