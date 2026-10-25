package com.odontologiaintegralfm.feature.consultation.consultationinstance.service;

import com.odontologiaintegralfm.feature.consultation.consultationinstance.dto.ProsthesisResponseDTO;
import com.odontologiaintegralfm.feature.consultation.prestation.repository.IPrestationTypeRepository;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.scope.mapper.ScopeMapper;
import com.odontologiaintegralfm.feature.consultation.scope.mapper.ScopeMapperImpl;
import com.odontologiaintegralfm.feature.consultation.scope.model.Scope;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.model.TreatmentInstance;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.repository.ITreatmentInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.service.ProsthesisDomainService;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.service.ProsthesisDomainService.CurrentProsthesis;
import com.odontologiaintegralfm.feature.patient.model.Patient;
import com.odontologiaintegralfm.feature.patient.service.IPatientService;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests de la orquestación de GetProsthesesByPatientUseCase (REQ-038).
 *
 * Decisiones de armado (ADR-0031):
 * - Patient, TreatmentInstance, PrestationInstance y los colaboradores se mockean: son datos de entrada o capas inferiores.
 * - ScopeMapper es un @Spy real (ScopeMapperImpl): el mapeo de Scope a ScopeDTO se ejercita de verdad.
 * - ProsthesisDomainService está mockeado: sus reglas se prueban en ProsthesisDomainServiceTest.
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class GetProsthesesByPatientUseCaseTest {

    private static final Long PATIENT_ID = 1L;

    @Mock private IPatientService patientService;
    @Mock private ITreatmentInstanceRepository treatmentInstanceRepository;
    @Mock private IPrestationInstanceRepository prestationInstanceRepository;
    @Mock private IPrestationTypeRepository prestationTypeRepository;
    @Mock private ProsthesisDomainService prosthesisDomainService;
    @Spy private ScopeMapper scopeMapper = new ScopeMapperImpl();

    @InjectMocks private GetProsthesesByPatientUseCase useCase;

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static Scope teeth(int... codes) {
        return Scope.build(Arrays.stream(codes).mapToObj(Tooth::fromCode).toList(), null, null, null, false);
    }

    private static TreatmentInstance origin(long id) {
        TreatmentInstance origin = mock(TreatmentInstance.class);
        lenient().when(origin.getId()).thenReturn(id);
        return origin;
    }

    /** Stubs de lectura neutros (listas vacías) y el resultado que devuelve el domain. */
    private void stubReadsAndDomain(List<CurrentProsthesis> domainResult) {
        List<TreatmentInstance> treatments = List.of();
        List<PrestationInstance> prestations = List.of();
        when(patientService.findByIdAndEnabledTrue(PATIENT_ID)).thenReturn(mock(Patient.class));
        when(treatmentInstanceRepository.findByPatientIdAndEnabledTrue(PATIENT_ID)).thenReturn(treatments);
        when(prestationInstanceRepository.findByPatientIdAndEnabledTrue(PATIENT_ID)).thenReturn(prestations);
        when(prestationTypeRepository.findTreatmentIdsLinkedToOriginRepairs()).thenReturn(List.of());
        when(prosthesisDomainService.buildCurrentProstheses(same(treatments), same(prestations), eq(Set.of())))
                .thenReturn(domainResult);
    }

    // ─── Tests ────────────────────────────────────────────────────────────────

    /**
     * CASO: el paciente 9999 no existe o está dado de baja.
     * Regla: D-10 — se valida al paciente antes de leer nada.
     * Validación: execute(9999L) lanza NotFoundException y no hay ninguna interacción con los repositorios ni con el domain.
     */
    @Test
    void getProstheses_patientNotFound_throwsNotFoundException() {
        when(patientService.findByIdAndEnabledTrue(9999L)).thenThrow(new NotFoundException(
                "exception.patientNotFound.user", null, "exception.patientNotFound.log", null, LogLevel.ERROR));

        assertThatThrownBy(() -> useCase.execute(9999L)).isInstanceOf(NotFoundException.class);

        verifyNoInteractions(treatmentInstanceRepository, prestationInstanceRepository,
                prestationTypeRepository, prosthesisDomainService);
    }

    /**
     * CASO: el use case lee tratamientos, prestaciones y ids ligados con las búsquedas que excluyen lo dado de baja.
     * Regla: D-07 — lo leído se entrega al domain tal cual.
     * Validación: el domain recibe las mismas instancias de lista que devolvieron los repositorios y Set.of(9L, 10L);
     * no hay otra interacción con los repositorios.
     */
    @Test
    void getProstheses_readsEnabledOnly_passesReadListsToDomainService() {
        List<TreatmentInstance> treatments = List.of(origin(1L), origin(2L));
        List<PrestationInstance> prestations = List.of(mock(PrestationInstance.class));
        when(patientService.findByIdAndEnabledTrue(PATIENT_ID)).thenReturn(mock(Patient.class));
        when(treatmentInstanceRepository.findByPatientIdAndEnabledTrue(PATIENT_ID)).thenReturn(treatments);
        when(prestationInstanceRepository.findByPatientIdAndEnabledTrue(PATIENT_ID)).thenReturn(prestations);
        when(prestationTypeRepository.findTreatmentIdsLinkedToOriginRepairs()).thenReturn(List.of(9L, 10L));
        when(prosthesisDomainService.buildCurrentProstheses(same(treatments), same(prestations), eq(Set.of(9L, 10L))))
                .thenReturn(List.of());

        useCase.execute(PATIENT_ID);

        verify(prosthesisDomainService).buildCurrentProstheses(same(treatments), same(prestations), eq(Set.of(9L, 10L)));
        verify(treatmentInstanceRepository).findByPatientIdAndEnabledTrue(PATIENT_ID);
        verify(prestationInstanceRepository).findByPatientIdAndEnabledTrue(PATIENT_ID);
        verify(prestationTypeRepository).findTreatmentIdsLinkedToOriginRepairs();
        verifyNoMoreInteractions(treatmentInstanceRepository, prestationInstanceRepository, prestationTypeRepository);
    }

    /**
     * CASO: Bety recibe una prótesis con origen de id 5 y dos ubicaciones: 14/16/17 y 18.
     * Regla: D-06 — el DTO trae el id del origen y las ubicaciones en el orden del domain.
     * Validación: 1 elemento con originTreatmentInstanceId 5; scopes[0].tooth [14, 16, 17] y scopes[1].tooth [18]; en ambos
     * quadrant, maxillary, toothFace y fullMouth son null; success es true.
     */
    @Test
    void getProstheses_prosthesisWithRepair_buildsDtoWithOriginIdAndScopes() {
        Scope originScope = teeth(14, 16, 17);
        Scope additionScope = teeth(18);
        stubReadsAndDomain(List.of(new CurrentProsthesis(origin(5L), List.of(originScope, additionScope))));

        Response<List<ProsthesisResponseDTO>> response = useCase.execute(PATIENT_ID);

        assertThat(response.success()).isTrue();
        assertThat(response.data()).hasSize(1);
        ProsthesisResponseDTO dto = response.data().get(0);
        assertThat(dto.originTreatmentInstanceId()).isEqualTo(5L);
        assertThat(dto.scopes()).hasSize(2);
        assertThat(dto.scopes().get(0).tooth()).containsExactly(Tooth.fromCode(14), Tooth.fromCode(16), Tooth.fromCode(17));
        assertThat(dto.scopes().get(1).tooth()).containsExactly(Tooth.fromCode(18));
        dto.scopes().forEach(scope -> {
            assertThat(scope.quadrant()).isNull();
            assertThat(scope.maxillary()).isNull();
            assertThat(scope.toothFace()).isNull();
            assertThat(scope.fullMouth()).isNull();
        });
        verify(scopeMapper).toScopeDTO(same(originScope));
        verify(scopeMapper).toScopeDTO(same(additionScope));
    }

    /**
     * CASO: el domain devuelve dos prótesis con origen de id 3 y de id 1, en ese orden.
     * Regla: D-06 — el orden lo decide el domain.
     * Validación: data() tiene 2 elementos con originTreatmentInstanceId [3, 1].
     */
    @Test
    void getProstheses_severalProstheses_keepsDomainOrder() {
        stubReadsAndDomain(List.of(
                new CurrentProsthesis(origin(3L), List.of(teeth(11))),
                new CurrentProsthesis(origin(1L), List.of(teeth(21)))));

        Response<List<ProsthesisResponseDTO>> response = useCase.execute(PATIENT_ID);

        assertThat(response.data()).extracting(ProsthesisResponseDTO::originTreatmentInstanceId)
                .containsExactly(3L, 1L);
    }

    /**
     * CASO: un paciente sin prótesis vigentes.
     * Regla: D-06 — lista vacía con 200, no un error.
     * Validación: data() no es null y está vacía; success es true; no lanza excepción.
     */
    @Test
    void getProstheses_noProstheses_emptyList() {
        stubReadsAndDomain(List.of());

        Response<List<ProsthesisResponseDTO>> response = useCase.execute(PATIENT_ID);

        assertThat(response.data()).isNotNull().isEmpty();
        assertThat(response.success()).isTrue();
    }
}