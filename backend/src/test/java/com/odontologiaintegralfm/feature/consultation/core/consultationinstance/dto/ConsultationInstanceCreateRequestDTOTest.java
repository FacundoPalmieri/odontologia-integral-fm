package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto;

import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceItemDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.StepItemDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.core.scope.dto.ScopeDTO;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.dto.TreatmentInstanceItemDTO;
import com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.dto.TreatmentInstanceRefDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Forma del request de creación de la instancia de consulta (ADR-0030), sin Spring ni Mockito.
 * Todos los casos pasan por un Validator real: así se prueba también que la anotación existe y que
 * @Valid baja a los ítems anidados. Los ítems anidados de cada caso son válidos por sí mismos, para que la
 * única violación posible sea la regla del request que se prueba.
 */
@Tag("unit")
class ConsultationInstanceCreateRequestDTOTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    private static List<String> paths(ConsultationInstanceCreateRequestDTO dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .toList();
    }

    /** Tratamiento que nace: diente 16, treatmentId 5, condición ausente (REQUIRED por default). */
    private static TreatmentInstanceItemDTO newTreatment(String tempId) {
        return new TreatmentInstanceItemDTO(null, tempId,
                new ScopeDTO(List.of(Tooth.T16), null, null, null, null), 5L, null);
    }

    /** Tratamiento existente en REQUIRED que se cierra por REPLACED. */
    private static TreatmentInstanceItemDTO existingTreatment(Long id) {
        return new TreatmentInstanceItemDTO(id, null, null, null, TreatmentConditionType.REPLACED);
    }

    /** Prestación existente que avanza un step. */
    private static PrestationInstanceItemDTO existingPrestation(Long id) {
        return new PrestationInstanceItemDTO(id, null, null, null, null, null, null, null,
                List.of(new StepItemDTO(9L, null, PrestationStepStatus.FINISHED)));
    }

    /** Prestación que nace sobre el tratamiento naciente identificado por tempId. */
    private static PrestationInstanceItemDTO newPrestationOnTemp(Long typeId, String tempId, Long stepId) {
        return new PrestationInstanceItemDTO(null, typeId, new TreatmentInstanceRefDTO(null, tempId),
                null, null, null, null, null,
                List.of(new StepItemDTO(null, stepId, PrestationStepStatus.IN_PROGRESS)));
    }

    private static ConsultationInstanceCreateRequestDTO request(List<TreatmentInstanceItemDTO> treatments,
                                                                List<PrestationInstanceItemDTO> prestations) {
        return new ConsultationInstanceCreateRequestDTO(1L, null, treatments, prestations);
    }

    /**
     * CASO: Request de Bety sin ningún dato clínico: ambas listas vacías, y el caso de ambas null.
     * Regla: D-31 — el create exige al menos un tratamiento o una prestación.
     * Validación: en los dos, una única violación con propertyPath atLeastOneClinicalDataPresent (400 de forma).
     */
    @Test
    void isAtLeastOneClinicalDataPresent_bothListsEmpty_returnsFalse() {
        assertThat(paths(request(List.of(), List.of()))).containsExactly("atLeastOneClinicalDataPresent");
        assertThat(paths(request(null, null))).containsExactly("atLeastOneClinicalDataPresent");
    }

    /**
     * CASO: Una lista con un ítem y la otra vacía, en ambos sentidos.
     * Regla: D-31 — alcanza con uno de los dos datos clínicos.
     * Validación: ninguna violación con propertyPath atLeastOneClinicalDataPresent cuando solo viene el
     * tratamiento ni cuando solo viene la prestación.
     */
    @Test
    void isAtLeastOneClinicalDataPresent_onlyTreatmentOrOnlyPrestation_returnsTrue() {
        assertThat(paths(request(List.of(newTreatment("tmp-1")), List.of())))
                .doesNotContain("atLeastOneClinicalDataPresent");
        assertThat(paths(request(List.of(), List.of(existingPrestation(812L)))))
                .doesNotContain("atLeastOneClinicalDataPresent");
    }

    /**
     * CASO: Dos tratamientos nuevos del diente 16 con el mismo tempId "tmp-1".
     * Regla: D-23 — los tempId de un mismo request son únicos, si no la prestación quedaría colgada del tratamiento equivocado.
     * Validación: una única violación con propertyPath tempIdsUnique.
     */
    @Test
    void isTempIdsUnique_duplicatedTempId_returnsFalse() {
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1"), newTreatment("tmp-1")), List.of());

        assertThat(paths(dto)).containsExactly("tempIdsUnique");
    }

    /**
     * CASO: tempId "tmp-1", "tmp-2" y dos tratamientos con tempId null.
     * Regla: D-23 — los tempId nulos se ignoran (un tratamiento sin prestación asociada no necesita tempId).
     * Validación: ninguna violación con propertyPath tempIdsUnique.
     */
    @Test
    void isTempIdsUnique_distinctOrNullTempIds_returnsTrue() {
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1"), newTreatment("tmp-2"), newTreatment(null), newTreatment(null)),
                List.of());

        assertThat(paths(dto)).doesNotContain("tempIdsUnique");
    }

    /**
     * CASO: Dos ítems de treatmentInstance[] con id = 145; y dos ítems de prestation[] con id = 812.
     * Regla: D-23 — un mismo id no puede declararse dos veces como ítem de treatmentInstance[] ni de prestation[].
     * Validación: en ambos, una única violación con propertyPath itemIdsUnique.
     */
    @Test
    void isItemIdsUnique_duplicatedItemId_returnsFalse() {
        ConsultationInstanceCreateRequestDTO duplicatedTreatments = request(
                List.of(existingTreatment(145L), existingTreatment(145L)), List.of());
        ConsultationInstanceCreateRequestDTO duplicatedPrestations = request(
                List.of(), List.of(existingPrestation(812L), existingPrestation(812L)));

        assertThat(paths(duplicatedTreatments)).containsExactly("itemIdsUnique");
        assertThat(paths(duplicatedPrestations)).containsExactly("itemIdsUnique");
    }

    /**
     * CASO: ids distintos, ítems sin id, y dos prestaciones nuevas que referencian el mismo tempId.
     * Regla: D-23 — repetir una referencia no es repetir un ítem.
     * Validación: ninguna violación con propertyPath itemIdsUnique en los tres escenarios.
     */
    @Test
    void isItemIdsUnique_distinctOrNullIds_returnsTrue() {
        ConsultationInstanceCreateRequestDTO distinctIds = request(
                List.of(existingTreatment(145L), existingTreatment(146L)),
                List.of(existingPrestation(812L), existingPrestation(813L)));
        ConsultationInstanceCreateRequestDTO withoutIds = request(
                List.of(newTreatment("tmp-1"), newTreatment("tmp-2")),
                List.of(newPrestationOnTemp(3L, "tmp-1", 3L), newPrestationOnTemp(4L, "tmp-2", 5L)));
        ConsultationInstanceCreateRequestDTO sameTempIdReferenced = request(
                List.of(newTreatment("tmp-1")),
                List.of(newPrestationOnTemp(3L, "tmp-1", 3L), newPrestationOnTemp(4L, "tmp-1", 5L)));

        assertThat(paths(distinctIds)).doesNotContain("itemIdsUnique");
        assertThat(paths(withoutIds)).doesNotContain("itemIdsUnique");
        assertThat(paths(sameTempIdReferenced)).doesNotContain("itemIdsUnique");
    }

    /**
     * CASO: Request válido salvo un prestation[] con una prestación existente (id 812) cuya lista de steps viene vacía.
     * Regla: D-32 — toda prestación trae su lista de steps; el @Valid del request tiene que bajar a los ítems anidados.
     * Validación: una única violación con propertyPath prestation[0].steps. Si falta @Valid sobre la lista,
     * la violación no aparece y el test falla.
     */
    @Test
    void prestation_nestedItemInvalid_violatesThroughCascade() {
        PrestationInstanceItemDTO withoutSteps = new PrestationInstanceItemDTO(812L, null, null, null, null,
                null, null, null, List.of());

        assertThat(paths(request(List.of(), List.of(withoutSteps)))).containsExactly("prestation[0].steps");
    }

    /**
     * CASO: Bety solo con una Consulta (treatmentInstance = null y un prestation[] válido), y el caso inverso
     * (prestation = null con un treatmentInstance[] válido).
     * Regla: D-31 — una de las dos listas puede venir null y se trata como vacía; las reglas tempIdsUnique e itemIdsUnique
     * tienen que ser null-safe con una sola lista null.
     * Validación: ninguna violación en ninguno de los dos requests. Si una regla no es null-safe, el Validator
     * lanza excepción y el test falla.
     */
    @Test
    void nullTreatmentOrPrestationList_withOtherListPopulated_noViolations() {
        assertThat(paths(request(null, List.of(existingPrestation(812L))))).isEmpty();
        assertThat(paths(request(List.of(newTreatment("tmp-1")), null))).isEmpty();
    }

    /**
     * CASO: Request válido salvo un tratamiento nuevo de Bety (treatmentId 5) cuyo scope trae diente 16 y cuadrante a la vez.
     * Regla: D-10 — un scope tiene a lo sumo un grupo de ubicación; el @Valid del request tiene que bajar
     * a treatmentInstance[] y de ahí al scope.
     * Validación: una única violación con propertyPath treatmentInstance[0].scope.singleLocationGroup. Si falta
     * @Valid en la lista o en el scope del ítem, la violación no aparece y el test falla.
     */
    @Test
    void treatmentInstance_nestedScopeInvalid_violatesThroughCascade() {
        TreatmentInstanceItemDTO twoGroups = new TreatmentInstanceItemDTO(null, "tmp-1",
                new ScopeDTO(List.of(Tooth.T16), null, Quadrant.UPPER_RIGHT, null, null), 5L, null);

        assertThat(paths(request(List.of(twoGroups), List.of()))).containsExactly("treatmentInstance[0].scope.singleLocationGroup");
    }

    /**
     * CASO: Request válido salvo una prestación nueva (tipo 3, un step de catálogo) con scope propio que
     * trae cuadrante y maxilar a la vez.
     * Regla: D-10 — un scope tiene a lo sumo un grupo de ubicación; el @Valid tiene que bajar al scope
     * del ítem de prestación.
     * Validación: una única violación con propertyPath prestation[0].scope.singleLocationGroup. Si falta
     * @Valid en el scope del ítem, la violación no aparece y el test falla.
     */
    @Test
    void prestation_nestedScopeInvalid_violatesThroughCascade() {
        PrestationInstanceItemDTO withTwoGroupScope = new PrestationInstanceItemDTO(null, 3L, null,
                new ScopeDTO(null, null, Quadrant.UPPER_RIGHT, Maxillary.UPPER, null), null, null, null, null,
                List.of(new StepItemDTO(null, 3L, PrestationStepStatus.IN_PROGRESS)));

        assertThat(paths(request(List.of(), List.of(withTwoGroupScope)))).containsExactly("prestation[0].scope.singleLocationGroup");
    }

    /**
     * CASO: Request válido salvo una prestación nueva (tipo 3, sin scope propio ni origen) cuyo
     * treatmentInstanceRef trae id 145 y tempId "tmp-1" a la vez.
     * Regla: D-24 — la referencia a un tratamiento lleva exactamente un identificador; el @Valid tiene que
     * bajar a la referencia.
     * Validación: una única violación con propertyPath prestation[0].treatmentInstanceRef.exactlyOneIdentifier.
     */
    @Test
    void prestation_nestedTreatmentRefInvalid_violatesThroughCascade() {
        PrestationInstanceItemDTO withBadRef = new PrestationInstanceItemDTO(null, 3L,
                new TreatmentInstanceRefDTO(145L, "tmp-1"), null, null, null, null, null,
                List.of(new StepItemDTO(null, 3L, PrestationStepStatus.IN_PROGRESS)));

        assertThat(paths(request(List.of(), List.of(withBadRef))))
                .containsExactly("prestation[0].treatmentInstanceRef.exactlyOneIdentifier");
    }

    /**
     * CASO: Request válido salvo una prestación nueva (tipo 3, treatmentInstanceRef válida con tempId "tmp-1")
     * cuyo originTreatmentInstanceRef trae id 145 y tempId "tmp-2" a la vez.
     * Regla: D-24 — la referencia de origen lleva exactamente un identificador; el @Valid tiene que bajar
     * a la referencia de origen.
     * Validación: una única violación con propertyPath prestation[0].originTreatmentInstanceRef.exactlyOneIdentifier.
     */
    @Test
    void prestation_nestedOriginRefInvalid_violatesThroughCascade() {
        PrestationInstanceItemDTO withBadOrigin = new PrestationInstanceItemDTO(null, 3L,
                new TreatmentInstanceRefDTO(null, "tmp-1"), null, new TreatmentInstanceRefDTO(145L, "tmp-2"),
                null, null, null, List.of(new StepItemDTO(null, 3L, PrestationStepStatus.IN_PROGRESS)));

        assertThat(paths(request(List.of(newTreatment("tmp-1")), List.of(withBadOrigin))))
                .containsExactly("prestation[0].originTreatmentInstanceRef.exactlyOneIdentifier");
    }

    /**
     * CASO: Un tratamiento existente con id 5 y una prestación existente con id 5 en el mismo request
     * (son tablas distintas).
     * Regla: D-23 — la unicidad de id rige dentro de cada lista, no entre treatmentInstance[] y prestation[].
     * Validación: ninguna violación con propertyPath itemIdsUnique. Falla si la regla junta ambas listas en un solo conjunto.
     */
    @Test
    void isItemIdsUnique_sameIdInTreatmentAndPrestationLists_returnsTrue() {
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(existingTreatment(5L)), List.of(existingPrestation(5L)));

        assertThat(paths(dto)).doesNotContain("itemIdsUnique");
    }

    /**
     * CASO: Request válido (un tratamiento nuevo de Bety) salvo consultationId = null.
     * Regla: el request tiene que identificar a qué consulta pertenece la instancia.
     * Validación: una única violación con propertyPath consultationId.
     */
    @Test
    void consultationId_null_violatesNotNull() {
        ConsultationInstanceCreateRequestDTO dto = new ConsultationInstanceCreateRequestDTO(
                null, null, List.of(newTreatment("tmp-1")), List.of());

        assertThat(paths(dto)).containsExactly("consultationId");
    }
}
