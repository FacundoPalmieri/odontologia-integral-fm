package com.odontologiaintegralfm.feature.consultation.consultationinstance.service;

import com.odontologiaintegralfm.feature.consultation.consultationinstance.dto.ClinicalContextResponseDTO;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.dto.PrestationInstanceContextResponseDTO;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.dto.StepContextResponseDTO;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.dto.TreatmentInstanceContextResponseDTO;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.prestation.model.Step;
import com.odontologiaintegralfm.feature.consultation.prestation.repository.IPrestationStepRepository;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationStepInstance;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.repository.IPrestationStepInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.scope.mapper.ScopeMapper;
import com.odontologiaintegralfm.feature.consultation.scope.mapper.ScopeMapperImpl;
import com.odontologiaintegralfm.feature.consultation.scope.model.Scope;
import com.odontologiaintegralfm.feature.consultation.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.treatment.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.model.TreatmentInstance;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.repository.ITreatmentInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.service.ProsthesisDomainService;
import com.odontologiaintegralfm.feature.dentist.dto.DentistSpecialtyResponseDTO;
import com.odontologiaintegralfm.feature.dentist.model.DentistSpecialty;
import com.odontologiaintegralfm.feature.patient.model.Patient;
import com.odontologiaintegralfm.feature.patient.service.IPatientService;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests de la orquestación de GetClinicalContextByPatientUseCase (REQ-027).
 *
 * Decisiones de armado (ADR-0031):
 * - Patient, TreatmentInstance, PrestationInstance, PrestationStepInstance, PrestationStep, Step, Treatment,
 *   PrestationType y DentistSpecialty se mockean: son datos de entrada que el use case solo lee.
 *   Un mock devuelve null por defecto, así que los helpers dejan stubbeados (lenient) id, createdAt, scope y label.
 * - ScopeMapper es un @Spy real (ScopeMapperImpl): la conversión de Scope a ScopeDTO se ejercita de verdad.
 * - ProsthesisDomainService está mockeado: sus reglas se prueban en ProsthesisDomainServiceTest.
 * - Lo que el use case no lee de forma distinta entre casos (instancias de step, prestaciones
 *   habilitadas, mapa de scopes por origen) queda en el valor por defecto del mock: lista o mapa vacío, nunca null.
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class GetClinicalContextByPatientUseCaseTest {

    private static final Long PATIENT_ID = 7L;

    @Mock private IPatientService patientService;
    @Mock private ITreatmentInstanceRepository treatmentInstanceRepository;
    @Mock private IPrestationInstanceRepository prestationInstanceRepository;
    @Mock private IPrestationStepInstanceRepository prestationStepInstanceRepository;
    @Mock private IPrestationStepRepository prestationStepRepository;
    @Mock private ProsthesisDomainService prosthesisDomainService;
    @Spy private ScopeMapper scopeMapper = new ScopeMapperImpl();

    @InjectMocks private GetClinicalContextByPatientUseCase useCase;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static LocalDateTime date(int year, int month) {
        return LocalDateTime.of(year, month, 15, 10, 0);
    }

    private static Scope teeth(int... codes) {
        return Scope.build(Arrays.stream(codes).mapToObj(Tooth::fromCode).toList(), null, null, null, false);
    }

    private static Treatment treatment(long id, String label) {
        Treatment treatment = mock(Treatment.class);
        lenient().when(treatment.getId()).thenReturn(id);
        lenient().when(treatment.getLabel()).thenReturn(label);
        return treatment;
    }

    private static TreatmentInstance treatmentInstance(long id, Treatment treatment, TreatmentConditionType condition,
                                                       LocalDateTime createdAt) {
        return treatmentInstance(id, treatment, condition, createdAt, teeth(16));
    }

    private static TreatmentInstance treatmentInstance(long id, Treatment treatment, TreatmentConditionType condition,
                                                       LocalDateTime createdAt, Scope scope) {
        TreatmentInstance instance = mock(TreatmentInstance.class);
        lenient().when(instance.getId()).thenReturn(id);
        lenient().when(instance.getCreatedAt()).thenReturn(createdAt);
        lenient().when(instance.getTreatment()).thenReturn(treatment);
        lenient().when(instance.getTreatmentCondition()).thenReturn(condition);
        lenient().when(instance.getScope()).thenReturn(scope);
        return instance;
    }

    private static TreatmentInstance treatmentInstance(long id, LocalDateTime createdAt) {
        return treatmentInstance(id, treatment(1000 + id, "Tratamiento " + id), TreatmentConditionType.IN_PROGRESS, createdAt);
    }

    private static PrestationType prestationType(long id, String label) {
        PrestationType type = mock(PrestationType.class);
        lenient().when(type.getId()).thenReturn(id);
        lenient().when(type.getLabel()).thenReturn(label);
        return type;
    }

    private static PrestationInstance prestationInstance(long id, PrestationType type, LocalDateTime createdAt) {
        PrestationInstance instance = mock(PrestationInstance.class);
        lenient().when(instance.getId()).thenReturn(id);
        lenient().when(instance.getCreatedAt()).thenReturn(createdAt);
        lenient().when(instance.getType()).thenReturn(type);
        lenient().when(instance.getStatus()).thenReturn(PrestationInstanceStatus.IN_PROGRESS);
        lenient().when(instance.getScope()).thenReturn(Scope.empty());
        return instance;
    }

    private static PrestationStep catalogStep(long id, int position, boolean required, String label, PrestationType type) {
        Step step = mock(Step.class);
        lenient().when(step.getLabel()).thenReturn(label);
        PrestationStep catalogStep = mock(PrestationStep.class);
        lenient().when(catalogStep.getId()).thenReturn(id);
        lenient().when(catalogStep.getPosition()).thenReturn(position);
        lenient().when(catalogStep.isRequired()).thenReturn(required);
        lenient().when(catalogStep.getStep()).thenReturn(step);
        lenient().when(catalogStep.getPrestation()).thenReturn(type);
        return catalogStep;
    }

    private static PrestationStepInstance stepInstance(long id, PrestationInstance prestation, PrestationStep catalogStep,
                                                       PrestationStepStatus status) {
        PrestationStepInstance instance = mock(PrestationStepInstance.class);
        lenient().when(instance.getId()).thenReturn(id);
        lenient().when(instance.getPrestationInstance()).thenReturn(prestation);
        lenient().when(instance.getStep()).thenReturn(catalogStep);
        lenient().when(instance.getStatus()).thenReturn(status);
        return instance;
    }

    private static DentistSpecialty specialty(long id, String name) {
        DentistSpecialty specialty = mock(DentistSpecialty.class);
        lenient().when(specialty.getId()).thenReturn(id);
        lenient().when(specialty.getName()).thenReturn(name);
        return specialty;
    }

    /** Matcher que pide que la colección tenga, como conjunto, exactamente estos orígenes. */
    private static ArgumentMatcher<Collection<TreatmentInstance>> exactlyOrigins(TreatmentInstance... expected) {
        return origins -> origins != null && new HashSet<>(origins).equals(Set.of(expected));
    }

    /** Matcher que pide que la colección de ids de tipo tenga exactamente estos ids, cada uno una sola vez. */
    private static ArgumentMatcher<Collection<Long>> exactlyIds(Long... expected) {
        return ids -> ids != null && ids.size() == expected.length && new HashSet<>(ids).equals(Set.of(expected));
    }

    /** Lecturas abiertas más el catálogo mínimo: un step por cada tipo distinto de las prestaciones abiertas. */
    private void stubOpenReadsWithCatalog(List<TreatmentInstance> treatments, List<PrestationInstance> prestations) {
        stubOpenReads(treatments, prestations);
        List<PrestationType> types = prestations.stream().map(PrestationInstance::getType).distinct().toList();
        List<PrestationStep> minimumCatalog = types.stream()
                .map(type -> catalogStep(9000 + type.getId(), 1, true, "Step " + type.getId(), type))
                .toList();
        when(prestationStepRepository.findByPrestationIdInFetchStep(anyCollection())).thenReturn(minimumCatalog);
    }

    private void stubOpenReads(List<TreatmentInstance> treatments, List<PrestationInstance> prestations) {
        when(patientService.findByIdAndEnabledTrue(PATIENT_ID)).thenReturn(mock(Patient.class));
        when(treatmentInstanceRepository.findActiveByPatientId(PATIENT_ID)).thenReturn(treatments);
        when(prestationInstanceRepository.findInProgressByPatientId(PATIENT_ID)).thenReturn(prestations);
    }

    // ─── Tests ────────────────────────────────────────────────────────────────

    /**
     * CASO: el paciente 9 está dado de baja.
     * Regla: D-05 — se valida al paciente antes de leer nada.
     * Validación: execute(9L) lanza la misma NotFoundException que el service (misma instancia); findByIdAndEnabledTrue
     * se llamó con 9L; no hay ninguna interacción con los repositorios, el service de steps ni el domain service.
     */
    @Test
    void execute_patientDisabled_returnsNotFound() {
        NotFoundException notFound = new NotFoundException(
                "exception.patientNotFound.user", null, "exception.patientNotFound.log", null, LogLevel.ERROR);
        when(patientService.findByIdAndEnabledTrue(9L)).thenThrow(notFound);

        assertThatThrownBy(() -> useCase.execute(9L)).isSameAs(notFound);

        verify(patientService).findByIdAndEnabledTrue(9L);
        verifyNoInteractions(treatmentInstanceRepository, prestationInstanceRepository,
                prestationStepInstanceRepository, prestationStepRepository, prosthesisDomainService);
    }

    /**
     * CASO: Bety tiene el conducto del 16 en curso, la caries del 24 requerida y un "TC unirradicular" en curso.
     * Regla: D-01 — se leen solo los tratamientos abiertos y las prestaciones en curso, con las queries del create.
     * Validación: success true y message null; 2 tratamientos (10 / "Tratamiento de conducto" / IN_PROGRESS y
     * 12 / "Caries" / REQUIRED, con scope.tooth [16] y [24]) y 1 prestación (21 / "TC unirradicular" / IN_PROGRESS); las tres lecturas se hicieron
     * con 7L y findByPatientIdAndEnabledTrue de tratamientos nunca se llamó.
     */
    @Test
    void execute_openPatient_returnsLiveAndEnabledTreatmentsAndPrestations() {
        TreatmentInstance conduct = treatmentInstance(10L, treatment(1L, "Tratamiento de conducto"),
                TreatmentConditionType.IN_PROGRESS, date(2026, 1), teeth(16));
        TreatmentInstance caries = treatmentInstance(12L, treatment(2L, "Caries"),
                TreatmentConditionType.REQUIRED, date(2026, 2), teeth(24));
        PrestationInstance unirradicular = prestationInstance(21L, prestationType(5L, "TC unirradicular"), date(2026, 1));
        stubOpenReadsWithCatalog(List.of(conduct, caries), List.of(unirradicular));

        Response<ClinicalContextResponseDTO> response = useCase.execute(PATIENT_ID);

        assertThat(response.success()).isTrue();
        assertThat(response.message()).isNull();
        assertThat(response.data().treatmentInstances())
                .extracting(TreatmentInstanceContextResponseDTO::id, TreatmentInstanceContextResponseDTO::label,
                        TreatmentInstanceContextResponseDTO::treatmentCondition,
                        treatment -> treatment.scope().tooth())
                .containsExactly(
                        tuple(10L, "Tratamiento de conducto", TreatmentConditionType.IN_PROGRESS, List.of(Tooth.fromCode(16))),
                        tuple(12L, "Caries", TreatmentConditionType.REQUIRED, List.of(Tooth.fromCode(24))));
        assertThat(response.data().prestationInstances())
                .extracting(PrestationInstanceContextResponseDTO::id, PrestationInstanceContextResponseDTO::label,
                        PrestationInstanceContextResponseDTO::status)
                .containsExactly(tuple(21L, "TC unirradicular", PrestationInstanceStatus.IN_PROGRESS));
        verify(patientService).findByIdAndEnabledTrue(PATIENT_ID);
        verify(treatmentInstanceRepository).findActiveByPatientId(PATIENT_ID);
        verify(prestationInstanceRepository).findInProgressByPatientId(PATIENT_ID);
        verify(treatmentInstanceRepository, never()).findByPatientIdAndEnabledTrue(any());
    }

    /**
     * CASO: un paciente nuevo, sin nada abierto.
     * Regla: D-06 — responde 200 con las dos listas vacías, no 404.
     * Validación: execute(7L) no lanza excepción; success true; treatmentInstances y prestationInstances no son null y
     * están vacías.
     */
    @Test
    void execute_patientWithoutItems_returnsEmptyLists() {
        stubOpenReads(List.of(), List.of());

        Response<ClinicalContextResponseDTO> response = useCase.execute(PATIENT_ID);

        assertThat(response.success()).isTrue();
        assertThat(response.data().treatmentInstances()).isNotNull().isEmpty();
        assertThat(response.data().prestationInstances()).isNotNull().isEmpty();
    }

    /**
     * CASO: Bety tiene solo la caries del 24 abierta y ninguna prestación.
     * Regla: D-06 — con una sola de las dos listas vacía, responde igual.
     * Validación: execute(7L) no lanza excepción; treatmentInstances tiene 1 elemento con id 12; prestationInstances no
     * es null y está vacía.
     */
    @Test
    void execute_onlyTreatments_returnsEmptyPrestations() {
        TreatmentInstance caries = treatmentInstance(12L, treatment(2L, "Caries"),
                TreatmentConditionType.REQUIRED, date(2026, 2));
        stubOpenReads(List.of(caries), List.of());

        Response<ClinicalContextResponseDTO> response = useCase.execute(PATIENT_ID);

        assertThat(response.data().treatmentInstances()).extracting(TreatmentInstanceContextResponseDTO::id)
                .containsExactly(12L);
        assertThat(response.data().prestationInstances()).isNotNull().isEmpty();
    }

    /**
     * CASO: la base devuelve los tratamientos de Bety desordenados.
     * Regla: D-02 — se ordenan por createdAt ascendente y, a igual fecha, por id.
     * Validación: treatmentInstances sale con los ids [12, 11, 13, 14] (enero, febrero, marzo id 13, marzo id 14).
     */
    @Test
    void execute_treatmentsOutOfOrder_returnsSortedByDateAndId() {
        stubOpenReads(List.of(
                treatmentInstance(11L, date(2026, 2)),
                treatmentInstance(12L, date(2026, 1)),
                treatmentInstance(14L, date(2026, 3)),
                treatmentInstance(13L, date(2026, 3))), List.of());

        Response<ClinicalContextResponseDTO> response = useCase.execute(PATIENT_ID);

        assertThat(response.data().treatmentInstances()).extracting(TreatmentInstanceContextResponseDTO::id)
                .containsExactly(12L, 11L, 13L, 14L);
    }

    /**
     * CASO: la base devuelve las prestaciones en curso de Bety desordenadas.
     * Regla: D-02 — se ordenan por createdAt ascendente y, a igual fecha, por id.
     * Validación: prestationInstances sale con los ids [23, 22, 24, 25]. Cada prestación lleva el catálogo
     * mínimo (un step).
     */
    @Test
    void execute_prestationsOutOfOrder_returnsSortedByDateAndId() {
        PrestationType type = prestationType(5L, "TC unirradicular");
        stubOpenReadsWithCatalog(List.of(), List.of(
                prestationInstance(22L, type, date(2026, 2)),
                prestationInstance(23L, type, date(2026, 1)),
                prestationInstance(25L, type, date(2026, 3)),
                prestationInstance(24L, type, date(2026, 3))));

        Response<ClinicalContextResponseDTO> response = useCase.execute(PATIENT_ID);

        assertThat(response.data().prestationInstances()).extracting(PrestationInstanceContextResponseDTO::id)
                .containsExactly(23L, 22L, 24L, 25L);
    }

    /**
     * CASO: el "TC unirradicular" de Bety tiene cuatro steps en el catálogo y ya hizo dos.
     * Regla: D-03 — se devuelven todos los steps del catálogo por position; los hechos con id y estado, los pendientes
     * con el stepId del catálogo. El label sale de Step.getLabel().
     * Validación: 4 steps en orden: Apertura (id 100, stepId null, FINISHED, required true), Desobturación (id 101,
     * stepId null, IN_PROGRESS, required true), Tallado (id null, stepId 203, status null, required true), Impresión
     * definitiva (id null, stepId 201, status null, required false). findByPrestationIdInFetchStep se llamó con el conjunto de tipos {5L}
     * y findByPrestationInstanceIn recibió exactamente la lista de prestaciones abiertas.
     */
    @Test
    void execute_prestationWithStartedSteps_returnsAllStepsSorted() {
        PrestationType type = prestationType(5L, "TC unirradicular");
        PrestationInstance unirradicular = prestationInstance(21L, type, date(2026, 1));
        stubOpenReads(List.of(), List.of(unirradicular));
        PrestationStep carving = catalogStep(203L, 3, true, "Tallado", type);
        PrestationStep opening = catalogStep(204L, 1, true, "Apertura", type);
        PrestationStep finalImpression = catalogStep(201L, 4, false, "Impresión definitiva", type);
        PrestationStep rootFillingRemoval = catalogStep(202L, 2, true, "Desobturación", type);
        when(prestationStepRepository.findByPrestationIdInFetchStep(argThat(exactlyIds(5L))))
                .thenReturn(List.of(carving, opening, finalImpression, rootFillingRemoval));
        PrestationStepInstance finishedOpening = stepInstance(100L, unirradicular, opening, PrestationStepStatus.FINISHED);
        PrestationStepInstance startedRemoval = stepInstance(101L, unirradicular, rootFillingRemoval, PrestationStepStatus.IN_PROGRESS);
        when(prestationStepInstanceRepository.findByPrestationInstanceIn(List.of(unirradicular)))
                .thenReturn(List.of(finishedOpening, startedRemoval));

        Response<ClinicalContextResponseDTO> response = useCase.execute(PATIENT_ID);

        assertThat(response.data().prestationInstances()).hasSize(1);
        assertThat(response.data().prestationInstances().get(0).steps())
                .extracting(StepContextResponseDTO::label, StepContextResponseDTO::id, StepContextResponseDTO::stepId,
                        StepContextResponseDTO::status, StepContextResponseDTO::required)
                .containsExactly(
                        tuple("Apertura", 100L, null, PrestationStepStatus.FINISHED, true),
                        tuple("Desobturación", 101L, null, PrestationStepStatus.IN_PROGRESS, true),
                        tuple("Tallado", null, 203L, null, true),
                        tuple("Impresión definitiva", null, 201L, null, false));
        verify(prestationStepRepository).findByPrestationIdInFetchStep(argThat(exactlyIds(5L)));
        verify(prestationStepInstanceRepository).findByPrestationInstanceIn(List.of(unirradicular));
    }

    /**
     * CASO: Bety tiene dos "TC unirradicular", en el 16 y en el 26, y solo en el del 16 hizo la Apertura.
     * Regla: D-03 y T-04 — cada prestación muestra sus propios steps y el catálogo del tipo se lee una sola vez.
     * Validación: la prestación 21 trae Apertura con id 100 / FINISHED y Desobturación con stepId 202 / status null; la
     * 24 trae Apertura con stepId 201 / status null y Desobturación con stepId 202 / status null;
     * findByPrestationIdInFetchStep se llamó exactamente una vez con el conjunto de tipos {5L}
     * y findByPrestationInstanceIn se llamó exactamente una vez con [prestación 21, prestación 24].
     */
    @Test
    void execute_twoPrestationsSameType_keepsStepsApartAndReadsCatalogOnce() {
        PrestationType type = prestationType(5L, "TC unirradicular");
        PrestationInstance tooth16 = prestationInstance(21L, type, date(2026, 1));
        PrestationInstance tooth26 = prestationInstance(24L, type, date(2026, 2));
        stubOpenReads(List.of(), List.of(tooth16, tooth26));
        PrestationStep opening = catalogStep(201L, 1, true, "Apertura", type);
        PrestationStep rootFillingRemoval = catalogStep(202L, 2, true, "Desobturación", type);
        when(prestationStepRepository.findByPrestationIdInFetchStep(argThat(exactlyIds(5L))))
                .thenReturn(List.of(opening, rootFillingRemoval));
        PrestationStepInstance finishedOpening = stepInstance(100L, tooth16, opening, PrestationStepStatus.FINISHED);
        when(prestationStepInstanceRepository.findByPrestationInstanceIn(List.of(tooth16, tooth26)))
                .thenReturn(List.of(finishedOpening));

        Response<ClinicalContextResponseDTO> response = useCase.execute(PATIENT_ID);

        Map<Long, List<StepContextResponseDTO>> stepsByPrestation = Map.of(
                21L, response.data().prestationInstances().get(0).steps(),
                24L, response.data().prestationInstances().get(1).steps());
        assertThat(response.data().prestationInstances()).extracting(PrestationInstanceContextResponseDTO::id)
                .containsExactly(21L, 24L);
        assertThat(stepsByPrestation.get(21L))
                .extracting(StepContextResponseDTO::label, StepContextResponseDTO::id, StepContextResponseDTO::stepId,
                        StepContextResponseDTO::status)
                .containsExactly(
                        tuple("Apertura", 100L, null, PrestationStepStatus.FINISHED),
                        tuple("Desobturación", null, 202L, null));
        assertThat(stepsByPrestation.get(24L))
                .extracting(StepContextResponseDTO::label, StepContextResponseDTO::id, StepContextResponseDTO::stepId,
                        StepContextResponseDTO::status)
                .containsExactly(
                        tuple("Apertura", null, 201L, null),
                        tuple("Desobturación", null, 202L, null));
        verify(prestationStepRepository, times(1)).findByPrestationIdInFetchStep(argThat(exactlyIds(5L)));
        verify(prestationStepInstanceRepository, times(1)).findByPrestationInstanceIn(List.of(tooth16, tooth26));
    }

    /**
     * CASO: Bety tiene una prótesis de origen en el 14, 16 y 17, con un arreglo terminado en el 18, y un "Rebasado" en
     * curso sobre ella.
     * Regla: D-02 — originScopes sale de buildScopesByOrigin, sin el descarte por solapamiento.
     * Validación: la prestación 23 trae originTreatmentInstanceId 30 y originScopes con dos ScopeDTO: tooth [14, 16, 17]
     * y tooth [18], en ese orden; la 21 trae originTreatmentInstanceId null y originScopes vacía; buildScopesByOrigin se llamó con un conjunto de orígenes igual a {tratamiento 30} (no el
     * 10 del que cuelga la 21) y con la misma lista que devolvió findByPatientIdAndEnabledTrue(7L) (same);
     * buildCurrentProstheses nunca se llamó.
     */
    @Test
    void execute_prestationWithOrigin_returnsOriginScopes() {
        TreatmentInstance conduct = treatmentInstance(10L, date(2026, 1));
        TreatmentInstance prosthesis = treatmentInstance(30L, date(2025, 6));
        PrestationType unirradicularType = prestationType(5L, "TC unirradicular");
        PrestationInstance unirradicular = prestationInstance(21L, unirradicularType, date(2026, 1));
        when(unirradicular.getTreatmentInstance()).thenReturn(conduct);
        PrestationInstance relining = prestationInstance(23L, prestationType(8L, "Rebasado"), date(2026, 2));
        when(relining.getOriginTreatmentInstance()).thenReturn(prosthesis);
        stubOpenReadsWithCatalog(List.of(conduct), List.of(unirradicular, relining));
        List<PrestationInstance> allPrestations = List.of(unirradicular, relining, mock(PrestationInstance.class));
        when(prestationInstanceRepository.findByPatientIdAndEnabledTrue(PATIENT_ID)).thenReturn(allPrestations);
        when(prosthesisDomainService.buildScopesByOrigin(argThat(exactlyOrigins(prosthesis)), same(allPrestations)))
                .thenReturn(new HashMap<>(Map.of(prosthesis, List.of(teeth(14, 16, 17), teeth(18)))));

        Response<ClinicalContextResponseDTO> response = useCase.execute(PATIENT_ID);

        PrestationInstanceContextResponseDTO unirradicularDto = response.data().prestationInstances().get(0);
        assertThat(unirradicularDto.id()).isEqualTo(21L);
        assertThat(unirradicularDto.originTreatmentInstanceId()).isNull();
        assertThat(unirradicularDto.originScopes()).isNotNull().isEmpty();
        PrestationInstanceContextResponseDTO reliningDto = response.data().prestationInstances().get(1);
        assertThat(reliningDto.id()).isEqualTo(23L);
        assertThat(reliningDto.originTreatmentInstanceId()).isEqualTo(30L);
        assertThat(reliningDto.originScopes()).hasSize(2);
        assertThat(reliningDto.originScopes().get(0).tooth())
                .containsExactly(Tooth.fromCode(14), Tooth.fromCode(16), Tooth.fromCode(17));
        assertThat(reliningDto.originScopes().get(1).tooth()).containsExactly(Tooth.fromCode(18));
        verify(prosthesisDomainService).buildScopesByOrigin(argThat(exactlyOrigins(prosthesis)), same(allPrestations));
        verify(prosthesisDomainService, never()).buildCurrentProstheses(anyList(), anyList(), anySet());
    }

    /**
     * CASO: un "Blanqueamiento" suelto no tiene tratamiento ni origen, y el "TC unirradicular" cuelga del conducto 10
     * sin origen.
     * Regla: D-02 — los ids que no aplican van en null y originScopes va vacío.
     * Validación: la prestación 22 trae treatmentInstanceId null, originTreatmentInstanceId null y originScopes vacía
     * (no null); la 21 trae treatmentInstanceId 10, originTreatmentInstanceId null y originScopes vacía (no null).
     * buildScopesByOrigin no se stubbea: sin orígenes no se llama.
     */
    @Test
    void execute_prestationWithoutTreatmentOrOrigin_returnsNullIdsAndEmptyScopes() {
        TreatmentInstance conduct = treatmentInstance(10L, date(2026, 1));
        PrestationInstance unirradicular = prestationInstance(21L, prestationType(5L, "TC unirradicular"), date(2026, 1));
        when(unirradicular.getTreatmentInstance()).thenReturn(conduct);
        PrestationInstance whitening = prestationInstance(22L, prestationType(9L, "Blanqueamiento ambulatorio"), date(2026, 2));
        stubOpenReadsWithCatalog(List.of(conduct), List.of(unirradicular, whitening));

        Response<ClinicalContextResponseDTO> response = useCase.execute(PATIENT_ID);

        assertThat(response.data().prestationInstances())
                .extracting(PrestationInstanceContextResponseDTO::id,
                        PrestationInstanceContextResponseDTO::treatmentInstanceId,
                        PrestationInstanceContextResponseDTO::originTreatmentInstanceId)
                .containsExactly(
                        tuple(21L, 10L, null),
                        tuple(22L, null, null));
        response.data().prestationInstances()
                .forEach(prestation -> assertThat(prestation.originScopes()).isNotNull().isEmpty());
    }

    /**
     * CASO: el "TC unirradicular" cuelga del conducto del 16 y no tiene ubicación propia; la limpieza tiene maxilar
     * superior.
     * Regla: D-02 — el scope de la prestación es su ubicación propia, vacía (no null) si no tiene; no copia la del
     * tratamiento.
     * Validación: el scope de la 21 no es null y tiene tooth, toothFace, quadrant, maxillary y fullMouth en null; el de
     * la 26 trae maxillary UPPER y el resto en null.
     */
    @Test
    void execute_prestationWithoutOwnScope_returnsEmptyScopeNotNull() {
        TreatmentInstance conduct = treatmentInstance(10L, date(2026, 1));
        PrestationInstance unirradicular = prestationInstance(21L, prestationType(5L, "TC unirradicular"), date(2026, 1));
        when(unirradicular.getTreatmentInstance()).thenReturn(conduct);
        PrestationInstance cleaning = prestationInstance(26L, prestationType(6L, "Limpieza"), date(2026, 2));
        when(cleaning.getScope()).thenReturn(Scope.build(null, null, null, Maxillary.UPPER, false));
        stubOpenReadsWithCatalog(List.of(conduct), List.of(unirradicular, cleaning));

        Response<ClinicalContextResponseDTO> response = useCase.execute(PATIENT_ID);

        var unirradicularScope = response.data().prestationInstances().get(0).scope();
        var cleaningScope = response.data().prestationInstances().get(1).scope();
        assertThat(unirradicularScope).isNotNull();
        assertThat(unirradicularScope.tooth()).isNull();
        assertThat(unirradicularScope.toothFace()).isNull();
        assertThat(unirradicularScope.quadrant()).isNull();
        assertThat(unirradicularScope.maxillary()).isNull();
        assertThat(unirradicularScope.fullMouth()).isNull();
        assertThat(cleaningScope.maxillary()).isEqualTo(Maxillary.UPPER);
        assertThat(cleaningScope.tooth()).isNull();
        assertThat(cleaningScope.toothFace()).isNull();
        assertThat(cleaningScope.quadrant()).isNull();
        assertThat(cleaningScope.fullMouth()).isNull();
    }

    /**
     * CASO: un odontólogo general atiende a Bety y hay un TC de Endodoncia, una Limpieza sin especialidades y una caries
     * de Ortodoncia.
     * Regla: D-06 y D-07 — no se filtra por especialidad y cada elemento trae las suyas.
     * Validación: 1 tratamiento y 2 prestaciones; la caries trae [(5, "Ortodoncia")], el TC trae [(2, "Endodoncia")] y la
     * limpieza trae specialties vacía (no null).
     */
    @Test
    void execute_itemsWithAndWithoutSpecialties_returnsAllWithSpecialties() {
        DentistSpecialty orthodontics = specialty(5L, "Ortodoncia");
        DentistSpecialty endodontics = specialty(2L, "Endodoncia");
        Treatment cariesTreatment = treatment(2L, "Caries");
        when(cariesTreatment.getDentistSpecialties()).thenReturn(Set.of(orthodontics));
        TreatmentInstance caries = treatmentInstance(12L, cariesTreatment, TreatmentConditionType.REQUIRED, date(2026, 2));
        PrestationType rootCanalType = prestationType(5L, "TC unirradicular");
        when(rootCanalType.getDentistSpecialties()).thenReturn(Set.of(endodontics));
        PrestationType cleaningType = prestationType(6L, "Limpieza");
        when(cleaningType.getDentistSpecialties()).thenReturn(Set.of());
        stubOpenReadsWithCatalog(List.of(caries), List.of(
                prestationInstance(21L, rootCanalType, date(2026, 1)),
                prestationInstance(27L, cleaningType, date(2026, 2))));

        Response<ClinicalContextResponseDTO> response = useCase.execute(PATIENT_ID);

        assertThat(response.data().treatmentInstances()).hasSize(1);
        assertThat(response.data().prestationInstances()).hasSize(2);
        assertThat(response.data().treatmentInstances().get(0).specialties())
                .containsExactly(new DentistSpecialtyResponseDTO(5L, "Ortodoncia"));
        assertThat(response.data().prestationInstances().get(0).specialties())
                .containsExactly(new DentistSpecialtyResponseDTO(2L, "Endodoncia"));
        assertThat(response.data().prestationInstances().get(1).specialties()).isNotNull().isEmpty();
    }

    /**
     * CASO: un tratamiento y un tipo de prestación tienen cargadas las especialidades 5 ("Endodoncia") y 2 ("Ortodoncia"), en ese orden de iteración.
     * Regla: D-07 — specialties va ordenada por id ascendente (no por nombre).
     * Validación: specialties de la caries y del TC salen [(2, "Ortodoncia"), (5, "Endodoncia")].
     */
    @Test
    void execute_specialtiesOutOfOrder_returnsSortedById() {
        DentistSpecialty endodontics = specialty(5L, "Endodoncia");
        DentistSpecialty orthodontics = specialty(2L, "Ortodoncia");
        Treatment cariesTreatment = treatment(2L, "Caries");
        when(cariesTreatment.getDentistSpecialties()).thenReturn(new LinkedHashSet<>(List.of(endodontics, orthodontics)));
        TreatmentInstance caries = treatmentInstance(12L, cariesTreatment, TreatmentConditionType.REQUIRED, date(2026, 2));
        PrestationType rootCanalType = prestationType(5L, "TC unirradicular");
        when(rootCanalType.getDentistSpecialties()).thenReturn(new LinkedHashSet<>(List.of(endodontics, orthodontics)));
        stubOpenReadsWithCatalog(List.of(caries), List.of(prestationInstance(21L, rootCanalType, date(2026, 1))));

        Response<ClinicalContextResponseDTO> response = useCase.execute(PATIENT_ID);

        assertThat(response.data().treatmentInstances().get(0).specialties()).containsExactly(
                new DentistSpecialtyResponseDTO(2L, "Ortodoncia"), new DentistSpecialtyResponseDTO(5L, "Endodoncia"));
        assertThat(response.data().prestationInstances().get(0).specialties()).containsExactly(
                new DentistSpecialtyResponseDTO(2L, "Ortodoncia"), new DentistSpecialtyResponseDTO(5L, "Endodoncia"));
    }

    /**
     * CASO: el tratamiento y el tipo de prestación llegan con las especialidades en null.
     * Regla: D-07 — un Set null da lista vacía.
     * Validación: execute(7L) no lanza excepción; specialties de la caries y del TC son listas vacías (no null).
     */
    @Test
    void execute_specialtiesNull_returnsEmptyList() {
        Treatment cariesTreatment = treatment(2L, "Caries");
        when(cariesTreatment.getDentistSpecialties()).thenReturn(null);
        TreatmentInstance caries = treatmentInstance(12L, cariesTreatment, TreatmentConditionType.REQUIRED, date(2026, 2));
        PrestationType rootCanalType = prestationType(5L, "TC unirradicular");
        when(rootCanalType.getDentistSpecialties()).thenReturn(null);
        stubOpenReadsWithCatalog(List.of(caries), List.of(prestationInstance(21L, rootCanalType, date(2026, 1))));

        Response<ClinicalContextResponseDTO> response = useCase.execute(PATIENT_ID);

        assertThat(response.data().treatmentInstances().get(0).specialties()).isNotNull().isEmpty();
        assertThat(response.data().prestationInstances().get(0).specialties()).isNotNull().isEmpty();
    }

    /**
     * CASO: Bety tiene un "TC unirradicular" y un "Blanqueamiento" en curso, ninguno con origen.
     * Regla: D-02 — sin orígenes no se lee el historial de prestaciones ni se calculan ubicaciones de origen.
     * Validación: execute(7L) no lanza excepción; las prestaciones 21 y 22 traen originTreatmentInstanceId null y
     * originScopes vacía (no null); findByPatientIdAndEnabledTrue de prestaciones y buildScopesByOrigin nunca se llamaron.
     */
    @Test
    void execute_prestationsWithoutOrigin_doesNotReadAllPrestations() {
        PrestationInstance unirradicular = prestationInstance(21L, prestationType(5L, "TC unirradicular"), date(2026, 1));
        when(unirradicular.getOriginTreatmentInstance()).thenReturn(null);
        PrestationInstance whitening = prestationInstance(22L, prestationType(9L, "Blanqueamiento ambulatorio"), date(2026, 2));
        when(whitening.getOriginTreatmentInstance()).thenReturn(null);
        stubOpenReadsWithCatalog(List.of(), List.of(unirradicular, whitening));

        Response<ClinicalContextResponseDTO> response = useCase.execute(PATIENT_ID);

        assertThat(response.data().prestationInstances())
                .extracting(PrestationInstanceContextResponseDTO::id,
                        PrestationInstanceContextResponseDTO::originTreatmentInstanceId)
                .containsExactly(tuple(21L, null), tuple(22L, null));
        response.data().prestationInstances()
                .forEach(prestation -> assertThat(prestation.originScopes()).isNotNull().isEmpty());
        verify(prestationInstanceRepository, never()).findByPatientIdAndEnabledTrue(any());
        verify(prosthesisDomainService, never()).buildScopesByOrigin(anyCollection(), anyList());
    }

    /**
     * CASO: Bety tiene un "TC unirradicular" (tipo 5) y un "Blanqueamiento ambulatorio" (tipo 6) en curso.
     * Regla: D-03 y T-04 — el catálogo de todos los tipos se lee en una sola consulta y cada prestación muestra solo los
     * steps de su tipo.
     * Validación: la prestación 21 trae 2 steps (stepId 201 y 202) y la 22 trae 1 (stepId 301); findByPrestationIdInFetchStep
     * se llamó exactamente una vez con un conjunto de tipos igual a {5L, 6L}.
     */
    @Test
    void execute_twoDifferentTypes_readsCatalogOnceWithBothIds() {
        PrestationType rootCanalType = prestationType(5L, "TC unirradicular");
        PrestationType whiteningType = prestationType(6L, "Blanqueamiento ambulatorio");
        stubOpenReads(List.of(), List.of(
                prestationInstance(21L, rootCanalType, date(2026, 1)),
                prestationInstance(22L, whiteningType, date(2026, 2))));
        PrestationStep opening = catalogStep(201L, 1, true, "Apertura", rootCanalType);
        PrestationStep rootFillingRemoval = catalogStep(202L, 2, true, "Desobturación", rootCanalType);
        PrestationStep application = catalogStep(301L, 1, true, "Aplicación", whiteningType);
        when(prestationStepRepository.findByPrestationIdInFetchStep(argThat(exactlyIds(5L, 6L))))
                .thenReturn(List.of(opening, application, rootFillingRemoval));

        Response<ClinicalContextResponseDTO> response = useCase.execute(PATIENT_ID);

        assertThat(response.data().prestationInstances()).extracting(PrestationInstanceContextResponseDTO::id)
                .containsExactly(21L, 22L);
        assertThat(response.data().prestationInstances().get(0).steps()).extracting(StepContextResponseDTO::stepId)
                .containsExactly(201L, 202L);
        assertThat(response.data().prestationInstances().get(1).steps()).extracting(StepContextResponseDTO::stepId)
                .containsExactly(301L);
        verify(prestationStepRepository, times(1)).findByPrestationIdInFetchStep(argThat(exactlyIds(5L, 6L)));
    }
}