package com.odontologiaintegralfm.feature.consultation.consultationinstance.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.prestation.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationTypePrice;
import com.odontologiaintegralfm.feature.consultation.prestation.service.PrestationStepService;
import com.odontologiaintegralfm.feature.consultation.prestation.service.PrestationTypePriceService;
import com.odontologiaintegralfm.feature.consultation.prestation.service.PrestationTypeService;
import com.odontologiaintegralfm.feature.consultation.promotion.model.Promotion;
import com.odontologiaintegralfm.feature.consultation.promotion.enums.DiscountType;
import com.odontologiaintegralfm.feature.consultation.promotion.service.PromotionQueryService;
import com.odontologiaintegralfm.feature.consultation.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.treatment.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.treatment.service.TreatmentService;
import com.odontologiaintegralfm.feature.consultation.consultation.enums.ConsultationStatusType;
import com.odontologiaintegralfm.feature.consultation.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.consultation.service.ChangeConsultationStatusUseCase;
import com.odontologiaintegralfm.feature.consultation.consultation.service.ConsultationQueryService;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.dto.ConsultationInstanceCreateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.repository.IConsultationInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.dto.PrestationInstanceItemDTO;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.dto.StepItemDTO;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationStepInstance;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationStepInstanceHistory;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.repository.IPrestationStepInstanceHistoryRepository;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.repository.IPrestationStepInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.service.PrestationInstanceDomainService;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.service.PrestationInstanceQueryService;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.service.PrestationStepInstanceQueryService;
import com.odontologiaintegralfm.feature.consultation.scope.dto.ScopeDTO;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.scope.mapper.ScopeMapper;
import com.odontologiaintegralfm.feature.consultation.scope.mapper.ScopeMapperImpl;
import com.odontologiaintegralfm.feature.consultation.scope.model.Scope;
import com.odontologiaintegralfm.feature.consultation.scope.service.ScopeDomainService;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.dto.TreatmentInstanceItemDTO;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.dto.TreatmentInstanceRefDTO;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.model.TreatmentInstance;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.model.TreatmentInstanceHistory;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.repository.ITreatmentInstanceHistoryRepository;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.repository.ITreatmentInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.service.TreatmentInstanceDomainService;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.service.TreatmentInstanceQueryService;
import com.odontologiaintegralfm.feature.dentist.service.DentistSpecialtyDomainService;
import com.odontologiaintegralfm.feature.dentist.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.model.DentistSpecialty;
import com.odontologiaintegralfm.feature.patient.model.Patient;
import com.odontologiaintegralfm.feature.person.person.model.Person;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.ForbiddenException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Suite del orquestador del create de ConsultationInstance (REQ-033).
 *
 * Convenciones (ADR-0031): las entidades que entran como dato (Consultation, Patient, Dentist, entidades existentes)
 * se mockean; TreatmentInstance, PrestationInstance y los historiales que el use case construye se arman reales con
 * build() y se capturan con ArgumentCaptor. Los domain services van mockeados: acá se verifica la orquestación.
 *
 * El fixture base (@BeforeEach) deja todo lo genérico en lenient: persona del odontólogo, consulta IN_CONSULTATION,
 * paciente 20, catálogo (tipo, steps 31 y 32, precio 1000.00) y los save que devuelven la entidad con id asignado.
 * Los stubs que definen cada caso son estrictos.
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CreateConsultationInstanceUseCaseTest {

    private static final Long CONSULTATION_ID = 1L;
    private static final Long PATIENT_ID = 20L;
    private static final Long DENTIST_PERSON_ID = 7L;
    private static final Long CONSULTATION_INSTANCE_ID = 55L;
    private static final Long TYPE_ID = 3L;
    private static final Long STEP_1 = 31L;
    private static final Long STEP_2 = 32L;

    @Mock private AuthenticatedUserService authenticatedUserService;
    @Mock private ConsultationQueryService consultationQueryService;
    @Mock private IConsultationInstanceRepository consultationInstanceRepository;
    @Mock private ITreatmentInstanceRepository treatmentInstanceRepository;
    @Mock private ITreatmentInstanceHistoryRepository treatmentInstanceHistoryRepository;
    @Mock private TreatmentInstanceQueryService treatmentInstanceQueryService;
    @Mock private TreatmentInstanceDomainService treatmentInstanceDomainService;
    @Mock private IPrestationInstanceRepository prestationInstanceRepository;
    @Mock private PrestationInstanceQueryService prestationInstanceQueryService;
    @Mock private PrestationInstanceDomainService prestationInstanceDomainService;
    @Mock private IPrestationStepInstanceRepository prestationStepInstanceRepository;
    @Mock private PrestationStepInstanceQueryService prestationStepInstanceQueryService;
    @Mock private IPrestationStepInstanceHistoryRepository prestationStepInstanceHistoryRepository;
    @Mock private PrestationStepService prestationStepService;
    @Mock private PrestationTypeService prestationTypeService;
    @Mock private PrestationTypePriceService prestationTypePriceService;
    @Mock private TreatmentService treatmentService;
    @Mock private PromotionQueryService promotionQueryService;
    @Mock private ScopeDomainService scopeDomainService;
    // Implementación real: con el mapper mockeado, los asserts sobre los dientes del Scope (scopeCaptor) serían tautológicos.
    @Spy private ScopeMapper scopeMapper = new ScopeMapperImpl();
    @Mock private DentistSpecialtyDomainService dentistSpecialtyDomainService;
    @Mock private ChangeConsultationStatusUseCase changeConsultationStatusUseCase;
    @Mock private MessageSource messageSource;

    @InjectMocks private CreateConsultationInstanceUseCase useCase;

    @Captor private ArgumentCaptor<ConsultationInstance> consultationInstanceCaptor;
    @Captor private ArgumentCaptor<TreatmentInstance> treatmentCaptor;
    @Captor private ArgumentCaptor<PrestationInstance> prestationCaptor;
    @Captor private ArgumentCaptor<TreatmentInstanceHistory> treatmentHistoryCaptor;
    @Captor private ArgumentCaptor<PrestationStepInstanceHistory> stepHistoryCaptor;
    @Captor private ArgumentCaptor<Scope> scopeCaptor;
    @Captor private ArgumentCaptor<TreatmentInstance> originCaptor;
    @Captor private ArgumentCaptor<List<TreatmentInstance>> explicitCaptor;
    @Captor private ArgumentCaptor<List<PrestationInstance>> replacedByTreatmentCaptor;
    @Captor private ArgumentCaptor<List<TreatmentInstance>> newTreatmentsCaptor;
    @Captor private ArgumentCaptor<List<PrestationInstance>> newPrestationsCaptor;
    @Captor private ArgumentCaptor<List<PrestationInstance>> replacedWithoutTreatmentCaptor;
    @Captor private ArgumentCaptor<List<PrestationInstance>> treatmentPrestationsCaptor;

    private final AtomicLong ids = new AtomicLong(1000);
    private final List<String> events = new ArrayList<>();
    /** Prestaciones reales que el use case guardó (la "base" simulada para findByTreatmentInstanceId). */
    private final List<PrestationInstance> savedPrestations = new ArrayList<>();

    private PrestationStep catalogStep1;
    private PrestationStep catalogStep2;
    private List<PrestationStep> catalog;
    private Consultation consultation;
    private Patient patient;
    private Person dentistPerson;
    private Person authPerson;
    private PrestationType type;
    private Treatment treatment;

    @BeforeEach
    void setUpFixture() {
        // Usuario autenticado y odontólogo de la consulta: la misma persona (id 7) salvo que el caso diga otra cosa.
        dentistPerson = mock(Person.class);
        authPerson = mock(Person.class);
        lenient().when(dentistPerson.getId()).thenReturn(DENTIST_PERSON_ID);
        lenient().when(authPerson.getId()).thenReturn(DENTIST_PERSON_ID);
        Dentist dentist = mock(Dentist.class);
        lenient().when(dentist.getPerson()).thenReturn(dentistPerson);
        UserSec authUser = mock(UserSec.class);
        lenient().when(authUser.getPerson()).thenReturn(authPerson);
        lenient().when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authUser);

        patient = mock(Patient.class);
        lenient().when(patient.getId()).thenReturn(PATIENT_ID);
        consultation = mock(Consultation.class);
        lenient().when(consultation.getId()).thenReturn(CONSULTATION_ID);
        lenient().when(consultation.getDentist()).thenReturn(dentist);
        lenient().when(consultation.getPatient()).thenReturn(patient);
        lenient().when(consultation.getStatus()).thenReturn(ConsultationStatusType.IN_CONSULTATION);
        lenient().when(consultationQueryService.findById(CONSULTATION_ID)).thenReturn(consultation);
        lenient().when(consultationInstanceRepository.findByConsultationId(CONSULTATION_ID)).thenReturn(null);
        lenient().when(consultationInstanceRepository.save(any(ConsultationInstance.class))).thenAnswer(inv -> {
            ConsultationInstance saved = inv.getArgument(0);
            saved.setId(CONSULTATION_INSTANCE_ID);
            return saved;
        });

        // Catálogo genérico.
        treatment = mock(Treatment.class);
        lenient().when(treatment.getDentistSpecialties()).thenReturn(Set.of());
        lenient().when(treatmentService.findById(anyLong())).thenReturn(treatment);
        type = mock(PrestationType.class);
        lenient().when(type.getId()).thenReturn(TYPE_ID);
        lenient().when(type.getDentistSpecialties()).thenReturn(Set.of());
        lenient().when(prestationTypeService.findById(anyLong())).thenReturn(type);
        PrestationTypePrice price = mock(PrestationTypePrice.class);
        lenient().when(price.getPrice()).thenReturn(new BigDecimal("1000.00"));
        lenient().when(prestationTypePriceService.currentPrice(anyLong())).thenReturn(price);
        catalogStep1 = catalogStep(STEP_1, 1);
        catalogStep2 = catalogStep(STEP_2, 2);
        catalog = List.of(catalogStep1, catalogStep2);
        lenient().when(prestationStepService.findAllPrestationStepsByPrestation(anyLong())).thenReturn(catalog);
        lenient().when(prestationStepService.findById(STEP_1)).thenReturn(catalogStep1);
        lenient().when(prestationStepService.findById(STEP_2)).thenReturn(catalogStep2);

        // Persistencia simulada: devuelve la entidad con id asignado y deja rastro del orden.
        lenient().when(treatmentInstanceRepository.save(any(TreatmentInstance.class))).thenAnswer(inv -> persisted(inv.getArgument(0)));
        lenient().when(prestationInstanceRepository.save(any(PrestationInstance.class))).thenAnswer(inv -> persisted(inv.getArgument(0)));
        lenient().when(prestationStepInstanceRepository.save(any(PrestationStepInstance.class))).thenAnswer(inv -> persisted(inv.getArgument(0)));
        lenient().when(treatmentInstanceHistoryRepository.save(any(TreatmentInstanceHistory.class))).thenAnswer(inv -> persisted(inv.getArgument(0)));
        lenient().when(prestationStepInstanceHistoryRepository.save(any(PrestationStepInstanceHistory.class))).thenAnswer(inv -> persisted(inv.getArgument(0)));
        lenient().doAnswer(inv -> {
            events.add("flush");
            return null;
        }).when(prestationInstanceRepository).flush();

        // Derivaciones neutras por defecto (los casos que las miden las pisan).
        lenient().when(prestationInstanceDomainService.calculatePrestationInstanceStatus(any(), any())).thenReturn(PrestationInstanceStatus.IN_PROGRESS);
        lenient().when(treatmentInstanceDomainService.calculateTreatmentCondition(any())).thenReturn(TreatmentConditionType.IN_PROGRESS);
        lenient().when(treatmentInstanceRepository.findActiveByPatientId(PATIENT_ID)).thenReturn(List.of());
        lenient().when(prestationInstanceRepository.findInProgressByPatientId(PATIENT_ID)).thenReturn(List.of());
        // La base simulada devuelve las prestaciones reales ya guardadas de un tratamiento (como haría JPA con autoflush).
        lenient().when(prestationInstanceRepository.findByTreatmentInstanceId(anyLong()))
                .thenAnswer(inv -> dbPrestationsOf(inv.getArgument(0)));
        lenient().doAnswer(inv -> {
            events.add("collision");
            return null;
        }).when(treatmentInstanceDomainService).validateLocationCollision(any(), any());
        lenient().doAnswer(inv -> {
            events.add("pending");
            return null;
        }).when(prestationInstanceDomainService).validatePrestationInstancePending(any(), any());
    }

    // ─── Ownership y precondiciones (D-62, D-20) ──────────────────────────────

    /**
     * CASO: El odontólogo de la consulta (persona 7) no es el usuario autenticado (persona 9), y además la consulta
     * está en WAITING_ROOM.
     * Regla: D-62 corre antes que D-20; una consulta ajena se ve igual que una inexistente.
     * Validación: NotFoundException (no ConflictException), sin ningún acceso a los repositorios de escritura.
     */
    @Test
    void execute_consultationOfAnotherDentist_throwsNotFoundException() {
        when(authPerson.getId()).thenReturn(9L);
        lenient().when(consultation.getStatus()).thenReturn(ConsultationStatusType.WAITING_ROOM);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)), List.of());

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(NotFoundException.class)
                .isNotInstanceOf(ConflictException.class);

        verifyNoInteractions(consultationInstanceRepository, treatmentInstanceRepository, treatmentInstanceHistoryRepository,
                prestationInstanceRepository, prestationStepInstanceRepository, prestationStepInstanceHistoryRepository);
    }

    /**
     * CASO: La consulta del odontólogo autenticado está en WAITING_ROOM.
     * Regla: D-20 — solo se crea la instancia con la consulta IN_CONSULTATION.
     * Validación: ConflictException con la clave statusConflict, sin tocar ningún repositorio de escritura.
     */
    @Test
    void execute_consultationNotInConsultation_throwsConflictException() {
        when(consultation.getStatus()).thenReturn(ConsultationStatusType.WAITING_ROOM);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)), List.of());

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(ConflictException.class)
                .satisfies(ex -> assertThat(((ConflictException) ex).getUserMessageKey()).contains("statusConflict"));

        verifyNoInteractions(consultationInstanceRepository, treatmentInstanceRepository, treatmentInstanceHistoryRepository,
                prestationInstanceRepository, prestationStepInstanceRepository, prestationStepInstanceHistoryRepository);
    }

    /**
     * CASO: La consulta ya tiene una ConsultationInstance (id 99).
     * Regla: D-20 — el create es de una sola vez por consulta.
     * Validación: ConflictException con la clave existingInstance y ninguna ConsultationInstance nueva guardada.
     */
    @Test
    void execute_consultationInstanceAlreadyExists_throwsConflictException() {
        ConsultationInstance existing = mock(ConsultationInstance.class);
        lenient().when(existing.getId()).thenReturn(99L);
        when(consultationInstanceRepository.findByConsultationId(CONSULTATION_ID)).thenReturn(existing);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)), List.of());

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(ConflictException.class)
                .satisfies(ex -> assertThat(((ConflictException) ex).getUserMessageKey()).contains("existingInstance"));

        verify(consultationInstanceRepository, never()).save(any());
    }

    // ─── Referencias por id (D-60) ────────────────────────────────────────────

    /**
     * CASO: treatmentInstance[{id: 145, REPLACED}] y el tratamiento 145 es de otro paciente.
     * Regla: D-60 — id inexistente o de otro paciente es 404.
     * Validación: la misma NotFoundException se propaga sin traducir; no se guarda nada y la consulta no se cierra.
     */
    @Test
    void execute_treatmentInstanceIdOfAnotherPatient_throwsNotFoundException() {
        NotFoundException notFound = notFound();
        when(treatmentInstanceQueryService.findByIdAndPatientId(145L, PATIENT_ID)).thenThrow(notFound);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(closeTreatment(145L, TreatmentConditionType.REPLACED)), List.of());

        assertThatThrownBy(() -> useCase.execute(dto)).isSameAs(notFound);

        verify(consultationInstanceRepository, never()).save(any());
        verify(treatmentInstanceRepository, never()).save(any());
        verifyNoInteractions(treatmentInstanceHistoryRepository);
        verify(changeConsultationStatusUseCase, never()).execute(any(), any());
    }

    /**
     * CASO: prestation[{id: 812}] — la prestación 812 es de Alan y la consulta es de Bety.
     * Regla: D-60 — la prestación existente tiene que ser del paciente de la consulta.
     * Validación: NotFoundException; no se crea ninguna ConsultationInstance ni historial.
     */
    @Test
    void execute_prestationIdBelongsToOtherPatient_throwsNotFoundException() {
        when(prestationInstanceQueryService.findByIdAndPatientId(812L, PATIENT_ID)).thenThrow(notFound());
        ConsultationInstanceCreateRequestDTO dto = request(List.of(),
                List.of(existingPrestation(812L, new StepItemDTO(9L, null, PrestationStepStatus.FINISHED))));

        assertThatThrownBy(() -> useCase.execute(dto)).isInstanceOf(NotFoundException.class);

        verify(consultationInstanceRepository, never()).save(any());
        verifyNoInteractions(treatmentInstanceHistoryRepository, prestationStepInstanceHistoryRepository);
    }

    /**
     * CASO: El step 9 existe y es del mismo paciente, pero pertenece a otra prestación que la 812.
     * Regla: D-60 — un StepItemDTO.id tiene que pertenecer a la PrestationInstance de su mismo ítem.
     * Validación: NotFoundException; applyStatus nunca se llama y no se escribe historial de step.
     */
    @Test
    void execute_stepIdOfAnotherPrestationOfSamePatient_throwsNotFoundException() {
        PrestationInstance prestation812 = existingPrestationMock(812L, PrestationInstanceStatus.IN_PROGRESS, null);
        when(prestationInstanceQueryService.findByIdAndPatientId(812L, PATIENT_ID)).thenReturn(prestation812);
        when(prestationStepInstanceQueryService.findByIdAndPrestationInstanceId(9L, 812L)).thenThrow(notFound());
        ConsultationInstanceCreateRequestDTO dto = request(List.of(),
                List.of(existingPrestation(812L, new StepItemDTO(9L, null, PrestationStepStatus.FINISHED))));

        assertThatThrownBy(() -> useCase.execute(dto)).isInstanceOf(NotFoundException.class);

        verify(prestation812, never()).applyStatus(any());
        verify(prestationStepInstanceRepository, never()).save(any());
        verifyNoInteractions(prestationStepInstanceHistoryRepository);
    }

    // ─── Validaciones previas a persistir ─────────────────────────────────────

    /**
     * CASO: Consulta + Radiografía del 24 de Bety en el mismo request; validatePrestationUnique lanza 409.
     * Regla: D-36 — la validación corre antes de crear nada.
     * Validación: ConflictException; ni la ConsultationInstance ni ninguna otra entidad se guardan.
     */
    @Test
    void execute_uniqueTypeValidationFails_nothingPersisted() {
        ConflictException conflict = conflict("exception.prestationInstanceDomainService.validatePrestationUnique.user");
        doThrow(conflict).when(prestationInstanceDomainService).validatePrestationUnique(anyList());
        ConsultationInstanceCreateRequestDTO dto = request(List.of(), List.of(
                newPrestation(TYPE_ID, null, scope(Tooth.T24), null, null, new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS)),
                newPrestation(4L, null, scope(Tooth.T24), null, null, new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        assertThatThrownBy(() -> useCase.execute(dto)).isSameAs(conflict);

        verify(consultationInstanceRepository, never()).save(any());
        verifyNoInteractions(treatmentInstanceRepository, treatmentInstanceHistoryRepository, prestationInstanceRepository,
                prestationStepInstanceRepository, prestationStepInstanceHistoryRepository);
    }

    // ─── Especialidad (D-61) ──────────────────────────────────────────────────

    /**
     * CASO: Tratamiento nuevo cuyo catálogo no incluye la especialidad del odontólogo.
     * Regla: D-61 — al nacer un TreatmentInstance se valida la especialidad contra el catálogo del Treatment.
     * Validación: ForbiddenException (403); no se guarda el TreatmentInstance ni su historial. El stub solo lanza con el
     * odontólogo de la consulta, las especialidades del Treatment y su label: si el use case pasa otros argumentos, el
     * stub no coincide y el test falla.
     */
    @Test
    void execute_treatmentSpecialtyNotAllowedForDentist_throwsForbiddenException() {
        Set<DentistSpecialty> treatmentSpecialties =
                Set.of(mock(DentistSpecialty.class));
        when(treatment.getDentistSpecialties()).thenReturn(treatmentSpecialties);
        when(treatment.getLabel()).thenReturn("Caries");
        Dentist dentist = consultation.getDentist();
        ForbiddenException forbidden = forbidden();
        doThrow(forbidden).when(dentistSpecialtyDomainService)
                .validateAllowed(dentist, treatmentSpecialties, "Caries");
        ConsultationInstanceCreateRequestDTO dto = request(List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)), List.of());

        assertThatThrownBy(() -> useCase.execute(dto)).isSameAs(forbidden);

        verify(treatmentInstanceRepository, never()).save(any());
        verify(treatmentInstanceHistoryRepository, never()).save(any());
    }

    /**
     * CASO: Prestación nueva de Ortodoncia pedida por un odontólogo endodoncista (la validación mockeada lanza 403).
     * Regla: D-61 — al nacer una PrestationInstance se valida contra las especialidades del PrestationType.
     * Validación: ForbiddenException (403); la prestación nueva no se guarda. El stub solo lanza con el odontólogo de la
     * consulta, las especialidades del PrestationType y su label: si el use case pasa otros argumentos, el stub no
     * coincide y el test falla.
     */
    @Test
    void execute_prestationTypeSpecialtyNotAllowedForDentist_throwsForbiddenException() {
        Set<DentistSpecialty> typeSpecialties =
                Set.of(mock(DentistSpecialty.class));
        when(type.getDentistSpecialties()).thenReturn(typeSpecialties);
        when(type.getLabel()).thenReturn("Ortodoncia");
        Dentist dentist = consultation.getDentist();
        ForbiddenException forbidden = forbidden();
        doThrow(forbidden).when(dentistSpecialtyDomainService)
                .validateAllowed(dentist, typeSpecialties, "Ortodoncia");
        ConsultationInstanceCreateRequestDTO dto = request(List.of(), List.of(
                newPrestation(TYPE_ID, null, scope(Tooth.T16), null, null, new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        assertThatThrownBy(() -> useCase.execute(dto)).isSameAs(forbidden);

        verify(prestationInstanceRepository, never()).save(any());
    }

    // ─── Cierre explícito de tratamientos existentes (D-26, D-33, D-39, D-40) ─

    /**
     * CASO: treatmentInstance[{id: 145, CANCELED}] con el tratamiento persistido en REQUIRED y ninguna entidad nueva.
     * Regla: D-26 — CANCELED nunca dispara trazabilidad de reemplazo.
     * Validación: termina sin excepción; applyCondition(CANCELED); una fila de TreatmentInstanceHistory con CANCELED y
     * la ConsultationInstance creada; la lista de reemplazados explícitos que recibe la trazabilidad está vacía.
     */
    @Test
    void execute_cancelRequiredTreatmentWithoutReplacement_isAccepted() {
        TreatmentInstance existing = existingTreatment(145L, TreatmentConditionType.REQUIRED);
        when(treatmentInstanceQueryService.findByIdAndPatientId(145L, PATIENT_ID)).thenReturn(existing);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(closeTreatment(145L, TreatmentConditionType.CANCELED)), List.of());

        Response<Long> response = useCase.execute(dto);

        assertThat(response.success()).isTrue();
        verify(treatmentInstanceDomainService).validateExplicitCloseAllowed(existing);
        verify(existing).applyCondition(TreatmentConditionType.CANCELED);
        verify(consultationInstanceRepository).save(consultationInstanceCaptor.capture());
        verify(treatmentInstanceHistoryRepository, times(1)).save(treatmentHistoryCaptor.capture());
        TreatmentInstanceHistory history = treatmentHistoryCaptor.getValue();
        assertThat(history.getTreatmentCondition()).isEqualTo(TreatmentConditionType.CANCELED);
        assertThat(history.getTreatmentInstance()).isSameAs(existing);
        assertThat(history.getConsultationInstance()).isSameAs(consultationInstanceCaptor.getValue());
        verify(treatmentInstanceDomainService).validateReplacementTraceability(explicitCaptor.capture(), any(), any(), any());
        assertThat(explicitCaptor.getValue()).isEmpty();
    }

    /**
     * CASO: Igual que el cancelar, pero con REPLACED sobre el tratamiento persistido en REQUIRED.
     * Regla: D-40 Caso A — el REPLACED explícito entra a la trazabilidad de reemplazo.
     * Validación: la lista treatmentInstancesReplacedExplicit contiene exactamente ese tratamiento y la fila de
     * historial trae REPLACED.
     */
    @Test
    void execute_replaceRequiredTreatment_passesItToReplacementTraceability() {
        TreatmentInstance existing = existingTreatment(145L, TreatmentConditionType.REQUIRED);
        when(treatmentInstanceQueryService.findByIdAndPatientId(145L, PATIENT_ID)).thenReturn(existing);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(closeTreatment(145L, TreatmentConditionType.REPLACED)), List.of());

        useCase.execute(dto);

        verify(treatmentInstanceDomainService).validateReplacementTraceability(explicitCaptor.capture(), any(), any(), any());
        assertThat(explicitCaptor.getValue()).containsExactly(existing);
        verify(treatmentInstanceHistoryRepository, times(1)).save(treatmentHistoryCaptor.capture());
        assertThat(treatmentHistoryCaptor.getValue().getTreatmentCondition()).isEqualTo(TreatmentConditionType.REPLACED);
    }

    // ─── Nacimiento (D-12, D-13, D-14, D-19, D-23, D-51) ──────────────────────

    /**
     * CASO: TreatmentInstance nuevo (diente 16) + PrestationInstance nueva con 2 steps sobre él por tempId;
     * calculateTreatmentCondition devuelve IN_PROGRESS.
     * Regla: D-12, D-13 y Decisión 14 — una fila de historial por evento mecánico.
     * Validación: 2 filas de TreatmentInstanceHistory (alta REQUIRED y derivación IN_PROGRESS) y 2 de
     * PrestationStepInstanceHistory (una por step, con el estado de cada uno), las 4 con la misma ConsultationInstance.
     * Sobre las entidades capturadas: el TreatmentInstance trae el paciente de la consulta, el Treatment pedido y la
     * condición IN_PROGRESS aplicada; la PrestationInstance trae paciente, tipo, el estado que devolvió
     * calculatePrestationInstanceStatus y scope vacío; cada historial de step apunta a un step de catálogo distinto.
     * calculateTreatmentCondition es un thenAnswer que devuelve IN_PROGRESS solo si la lista recibida trae la prestación
     * nueva (si no, REQUIRED): un use case que deriva antes de guardarla o con la lista incompleta falla.
     * El catálogo se resuelve con stubs estrictos por id (Treatment 5 y PrestationType 3), y la ConsultationInstance
     * capturada trae la consulta y la observación del request.
     */
    @Test
    void execute_birth_writesBirthEventInBothHistories() {
        when(treatmentService.findById(5L)).thenReturn(treatment);
        when(prestationTypeService.findById(TYPE_ID)).thenReturn(type);
        when(prestationInstanceDomainService.calculatePrestationInstanceStatus(any(), any())).thenReturn(PrestationInstanceStatus.IN_PROGRESS);
        when(treatmentInstanceDomainService.calculateTreatmentCondition(anyList())).thenAnswer(inv ->
                containsNewPrestation(inv.getArgument(0)) ? TreatmentConditionType.IN_PROGRESS : TreatmentConditionType.REQUIRED);
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)),
                List.of(newPrestation(TYPE_ID, ref("tmp-1"), null, null, null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.FINISHED),
                        new StepItemDTO(null, STEP_2, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(consultationInstanceRepository).save(consultationInstanceCaptor.capture());
        ConsultationInstance created = consultationInstanceCaptor.getValue();
        assertThat(created.getConsultation()).isSameAs(consultation);
        assertThat(created.getObservation()).isEqualTo("observación");
        verify(treatmentInstanceRepository, atLeastOnceSave()).save(treatmentCaptor.capture());
        TreatmentInstance savedTreatment = lastOf(treatmentCaptor);
        verify(treatmentInstanceHistoryRepository, times(2)).save(treatmentHistoryCaptor.capture());
        List<TreatmentInstanceHistory> treatmentHistories = treatmentHistoryCaptor.getAllValues();
        assertThat(treatmentHistories).extracting(TreatmentInstanceHistory::getTreatmentCondition)
                .containsExactly(TreatmentConditionType.REQUIRED, TreatmentConditionType.IN_PROGRESS);
        assertThat(treatmentHistories).allSatisfy(h -> {
            assertThat(h.getConsultationInstance()).isSameAs(created);
            assertThat(h.getTreatmentInstance()).isSameAs(savedTreatment);
        });
        assertThat(savedTreatment.getPatient()).isSameAs(patient);
        assertThat(savedTreatment.getTreatment()).isSameAs(treatment);
        assertThat(savedTreatment.getTreatmentCondition()).isEqualTo(TreatmentConditionType.IN_PROGRESS);
        verify(prestationInstanceRepository, atLeastOnceSave()).save(prestationCaptor.capture());
        PrestationInstance savedPrestation = lastOf(prestationCaptor);
        assertThat(savedPrestation.getPatient()).isSameAs(patient);
        assertThat(savedPrestation.getType()).isSameAs(type);
        assertThat(savedPrestation.getStatus()).isEqualTo(PrestationInstanceStatus.IN_PROGRESS);
        assertThat(savedPrestation.getScope().isEmpty()).isTrue();
        verify(prestationStepInstanceHistoryRepository, times(2)).save(stepHistoryCaptor.capture());
        List<PrestationStepInstanceHistory> stepHistories = stepHistoryCaptor.getAllValues();
        assertThat(stepHistories).extracting(PrestationStepInstanceHistory::getStatus)
                .containsExactly(PrestationStepStatus.FINISHED, PrestationStepStatus.IN_PROGRESS);
        assertThat(stepHistories).allSatisfy(h -> assertThat(h.getConsultationInstance()).isSameAs(created));
        assertThat(stepHistories.stream().map(h -> h.getPrestationStepInstance().getStep()).toList())
                .containsExactly(catalogStep1, catalogStep2);
    }

    /**
     * CASO: treatmentInstance[{tempId: null, tooth [12,13,14], PREEXISTING}] y prestation[] vacía.
     * Regla: D-51 — un PREEXISTING solo no tiene prestación de la cual derivar nada (D-28).
     * Validación: el TreatmentInstance guardado queda PREEXISTING, una sola fila de historial y
     * calculateTreatmentCondition nunca se invoca.
     */
    @Test
    void execute_birthPreexistingTreatmentAlone_persistsPreexistingWithoutDerivation() {
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment(null, scope(Tooth.T12, Tooth.T13, Tooth.T14), 5L, TreatmentConditionType.PREEXISTING)),
                List.of());

        useCase.execute(dto);

        verify(treatmentInstanceRepository, atLeastOnceSave()).save(treatmentCaptor.capture());
        assertThat(lastOf(treatmentCaptor).getTreatmentCondition()).isEqualTo(TreatmentConditionType.PREEXISTING);
        verify(treatmentInstanceHistoryRepository, times(1)).save(treatmentHistoryCaptor.capture());
        assertThat(treatmentHistoryCaptor.getValue().getTreatmentCondition()).isEqualTo(TreatmentConditionType.PREEXISTING);
        verify(treatmentInstanceDomainService, never()).calculateTreatmentCondition(any());
    }

    /**
     * CASO: Tratamiento nuevo en REQUIRED + prestación nueva sobre él; calculateTreatmentCondition devuelve IN_PROGRESS.
     * Regla: Decisión 14 — el alta y la derivación son dos eventos mecánicos distintos.
     * Validación: 2 filas de historial del tratamiento, en orden REQUIRED y después IN_PROGRESS, ambas con la misma
     * ConsultationInstance; el TreatmentInstance guardado queda con la condición IN_PROGRESS aplicada (no solo el
     * historial). calculateTreatmentCondition es un thenAnswer que devuelve IN_PROGRESS solo si la lista recibida trae la
     * prestación nueva: un use case que deriva con la lista incompleta recibe REQUIRED y el test falla.
     */
    @Test
    void execute_treatmentWithPrestationInSameRequest_writesBirthAndDerivationEvents() {
        when(treatmentInstanceDomainService.calculateTreatmentCondition(anyList())).thenAnswer(inv ->
                containsNewPrestation(inv.getArgument(0)) ? TreatmentConditionType.IN_PROGRESS : TreatmentConditionType.REQUIRED);
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, TreatmentConditionType.REQUIRED)),
                List.of(newPrestation(TYPE_ID, ref("tmp-1"), null, null, null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(consultationInstanceRepository).save(consultationInstanceCaptor.capture());
        verify(treatmentInstanceHistoryRepository, times(2)).save(treatmentHistoryCaptor.capture());
        List<TreatmentInstanceHistory> histories = treatmentHistoryCaptor.getAllValues();
        assertThat(histories).extracting(TreatmentInstanceHistory::getTreatmentCondition)
                .containsExactly(TreatmentConditionType.REQUIRED, TreatmentConditionType.IN_PROGRESS);
        assertThat(histories).allSatisfy(h -> assertThat(h.getConsultationInstance()).isSameAs(consultationInstanceCaptor.getValue()));
        verify(treatmentInstanceRepository, atLeastOnceSave()).save(treatmentCaptor.capture());
        assertThat(lastOf(treatmentCaptor).getTreatmentCondition()).isEqualTo(TreatmentConditionType.IN_PROGRESS);
    }

    // ─── Referencias por tempId (D-23, D-38) ──────────────────────────────────

    /**
     * CASO: treatmentInstance[{tempId: "tmp-1", tooth [16]}] y prestation[{treatmentInstanceRef: {tempId: "tmp-1"}}].
     * Regla: D-23 — la prestación resuelve su tratamiento por el mapa de tempId, nunca comparando dientes.
     * Validación: getTreatmentInstance() de la PrestationInstance guardada es el mismo objeto guardado para tmp-1 y su
     * scope propio es vacío y el paciente de la prestación y del tratamiento guardados es el de la consulta.
     */
    @Test
    void execute_prestationReferencesTempIdBornInSameRequest_resolvesCorrectly() {
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)),
                List.of(newPrestation(TYPE_ID, ref("tmp-1"), null, null, null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(treatmentInstanceRepository, atLeastOnceSave()).save(treatmentCaptor.capture());
        TreatmentInstance savedTreatment = lastOf(treatmentCaptor);
        verify(prestationInstanceRepository, atLeastOnceSave()).save(prestationCaptor.capture());
        PrestationInstance savedPrestation = lastOf(prestationCaptor);
        assertThat(savedPrestation.getTreatmentInstance()).isSameAs(savedTreatment);
        assertThat(savedPrestation.getScope().isEmpty()).isTrue();
        assertThat(savedPrestation.getPatient()).isSameAs(patient);
        assertThat(savedTreatment.getPatient()).isSameAs(patient);
    }

    /**
     * CASO: prestation[{treatmentInstanceRef: {tempId: "tmp-9"}}] sin ningún tratamiento con ese tempId.
     * Regla: D-38 — un tempId que no está en el request es 400.
     * Validación: BadRequestException; la prestación no se guarda.
     */
    @Test
    void execute_tempIdWithoutMatch_throwsBadRequestException() {
        ConsultationInstanceCreateRequestDTO dto = request(List.of(), List.of(
                newPrestation(TYPE_ID, ref("tmp-9"), null, null, null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        assertThatThrownBy(() -> useCase.execute(dto)).isInstanceOf(BadRequestException.class);

        verify(prestationInstanceRepository, never()).save(any());
    }

    /**
     * CASO: treatmentInstanceRef.id apunta a un tratamiento FINISHED; validateReferenceNotTerminal lanza 409.
     * Regla: D-38 — nada nuevo cuelga de un tratamiento ya cerrado.
     * Validación: ConflictException; la prestación no se guarda.
     */
    @Test
    void execute_treatmentInstanceRefPointsToTerminalTreatment_throwsConflictException() {
        TreatmentInstance finished = existingTreatment(145L, TreatmentConditionType.FINISHED);
        when(treatmentInstanceQueryService.findByIdAndPatientId(145L, PATIENT_ID)).thenReturn(finished);
        ConflictException conflict = conflict("exception.treatmentInstanceDomainService.validateReferenceNotTerminal.user");
        doThrow(conflict).when(treatmentInstanceDomainService).validateReferenceNotTerminal(finished);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(), List.of(
                newPrestation(TYPE_ID, refId(145L), null, null, null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        assertThatThrownBy(() -> useCase.execute(dto)).isSameAs(conflict);

        verify(prestationInstanceRepository, never()).save(any());
    }

    // ─── Avance de steps (D-19, D-27, D-28, D-39) ─────────────────────────────

    /**
     * CASO: Prestación existente con un step IN_PROGRESS; el request lo avanza a FINISHED.
     * Regla: D-19 — avanzar siempre escribe un evento nuevo, nunca modifica uno existente.
     * Validación: applyStatus(FINISHED) sobre el step; un único save de PrestationStepInstanceHistory con una fila
     * nueva (FINISHED, del step y de la ConsultationInstance creada).
     */
    @Test
    void execute_advanceExistingStep_writesNewEventWithoutOverwritingPrevious() {
        PrestationInstance prestation = existingPrestationMock(812L, PrestationInstanceStatus.IN_PROGRESS, null);
        PrestationStepInstance step = existingStep(9L, PrestationStepStatus.IN_PROGRESS);
        when(prestationInstanceQueryService.findByIdAndPatientId(812L, PATIENT_ID)).thenReturn(prestation);
        when(prestationStepInstanceQueryService.findByIdAndPrestationInstanceId(9L, 812L)).thenReturn(step);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(),
                List.of(existingPrestation(812L, new StepItemDTO(9L, null, PrestationStepStatus.FINISHED))));

        useCase.execute(dto);

        verify(step).applyStatus(PrestationStepStatus.FINISHED);
        verify(consultationInstanceRepository).save(consultationInstanceCaptor.capture());
        verify(prestationStepInstanceHistoryRepository, times(1)).save(stepHistoryCaptor.capture());
        PrestationStepInstanceHistory history = stepHistoryCaptor.getValue();
        assertThat(history.getStatus()).isEqualTo(PrestationStepStatus.FINISHED);
        assertThat(history.getPrestationStepInstance()).isSameAs(step);
        assertThat(history.getConsultationInstance()).isSameAs(consultationInstanceCaptor.getValue());
    }

    /**
     * CASO: Prestación existente que termina FINISHED; el tratamiento estaba IN_PROGRESS y calculateTreatmentCondition
     * devuelve FINISHED.
     * Regla: D-28 — si la condición derivada cambió, se aplica y se escribe el evento.
     * Validación: applyCondition(FINISHED) y 1 fila de historial del tratamiento con FINISHED.
     */
    @Test
    void execute_advanceExistingPrestation_treatmentConditionChanged_writesHistoryEvent() {
        TreatmentInstance treatmentInstance = existingTreatment(100L, TreatmentConditionType.IN_PROGRESS);
        PrestationInstance prestation = existingPrestationMock(812L, PrestationInstanceStatus.IN_PROGRESS, treatmentInstance);
        stubExistingStepAdvance(prestation, 9L, PrestationStepStatus.IN_PROGRESS);
        when(prestationInstanceDomainService.calculatePrestationInstanceStatus(any(), any())).thenReturn(PrestationInstanceStatus.FINISHED);
        when(prestationInstanceRepository.findByTreatmentInstanceId(100L)).thenReturn(List.of(prestation));
        when(treatmentInstanceDomainService.calculateTreatmentCondition(anyList())).thenReturn(TreatmentConditionType.FINISHED);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(),
                List.of(existingPrestation(812L, new StepItemDTO(9L, null, PrestationStepStatus.FINISHED))));

        useCase.execute(dto);

        verify(treatmentInstance).applyCondition(TreatmentConditionType.FINISHED);
        verify(treatmentInstanceHistoryRepository, times(1)).save(treatmentHistoryCaptor.capture());
        assertThat(treatmentHistoryCaptor.getValue().getTreatmentCondition()).isEqualTo(TreatmentConditionType.FINISHED);
        assertThat(treatmentHistoryCaptor.getValue().getTreatmentInstance()).isSameAs(treatmentInstance);
    }

    /**
     * CASO: La prestación avanza pero el tratamiento sigue IN_PROGRESS (la derivación devuelve IN_PROGRESS).
     * Regla: D-28 / D-19 — sin cambio de condición no hay evento mecánico.
     * Validación: no se llama a applyCondition ni se guarda historial del tratamiento.
     */
    @Test
    void execute_advanceExistingPrestation_treatmentConditionUnchanged_writesNoEvent() {
        TreatmentInstance treatmentInstance = existingTreatment(100L, TreatmentConditionType.IN_PROGRESS);
        PrestationInstance prestation = existingPrestationMock(812L, PrestationInstanceStatus.IN_PROGRESS, treatmentInstance);
        stubExistingStepAdvance(prestation, 9L, PrestationStepStatus.IN_PROGRESS);
        when(prestationInstanceRepository.findByTreatmentInstanceId(100L)).thenReturn(List.of(prestation));
        ConsultationInstanceCreateRequestDTO dto = request(List.of(),
                List.of(existingPrestation(812L, new StepItemDTO(9L, null, PrestationStepStatus.FINISHED))));

        useCase.execute(dto);

        verify(treatmentInstance, never()).applyCondition(any());
        verifyNoInteractions(treatmentInstanceHistoryRepository);
    }

    /**
     * CASO: Prestación existente con steps pendientes enviados REPLACED; calculatePrestationInstanceStatus devuelve
     * REPLACED y calculateTreatmentCondition devuelve REPLACED (única prestación viva del tratamiento).
     * Regla: D-39 Caso B — el tratamiento se cierra recién cuando no queda ninguna prestación viva.
     * Validación: applyStatus(REPLACED) en la prestación, applyCondition(REPLACED) en el tratamiento con su fila de
     * historial; la prestación entra a prestationInstancesReplacedByTreatment de la trazabilidad y
     * prestationInstancesReplacedWithoutTreatment llega vacía. Sobre las entidades: la prestación queda con getStatus() =
     * REPLACED, el tratamiento con getTreatmentCondition() = REPLACED, y la fila de historial apunta a ese tratamiento y
     * a la ConsultationInstance creada. calculateTreatmentCondition es un thenAnswer que devuelve REPLACED solo si
     * ninguna prestación de la lista recibida está IN_PROGRESS (el use case tiene que haber aplicado antes REPLACED a la
     * prestación).
     */
    @Test
    void execute_replaceSingleLivePrestation_closesTreatmentInstanceAsReplaced() {
        TreatmentInstance t100 = existingTreatment(100L, TreatmentConditionType.IN_PROGRESS);
        PrestationInstance prestation = existingPrestationMock(812L, PrestationInstanceStatus.IN_PROGRESS, t100);
        stubExistingStepAdvance(prestation, 9L, PrestationStepStatus.IN_PROGRESS);
        when(prestationInstanceDomainService.calculatePrestationInstanceStatus(any(), any())).thenReturn(PrestationInstanceStatus.REPLACED);
        when(prestationInstanceRepository.findByTreatmentInstanceId(100L)).thenReturn(List.of(prestation));
        when(treatmentInstanceDomainService.calculateTreatmentCondition(anyList())).thenAnswer(inv -> {
            List<PrestationInstance> received = inv.getArgument(0);
            return received.stream().anyMatch(p -> p.getStatus() == PrestationInstanceStatus.IN_PROGRESS)
                    ? TreatmentConditionType.IN_PROGRESS
                    : TreatmentConditionType.REPLACED;
        });
        ConsultationInstanceCreateRequestDTO dto = request(List.of(),
                List.of(existingPrestation(812L, new StepItemDTO(9L, null, PrestationStepStatus.REPLACED))));

        useCase.execute(dto);

        verify(prestation).applyStatus(PrestationInstanceStatus.REPLACED);
        verify(t100).applyCondition(TreatmentConditionType.REPLACED);
        assertThat(prestation.getStatus()).isEqualTo(PrestationInstanceStatus.REPLACED);
        assertThat(t100.getTreatmentCondition()).isEqualTo(TreatmentConditionType.REPLACED);
        verify(consultationInstanceRepository).save(consultationInstanceCaptor.capture());
        verify(treatmentInstanceHistoryRepository, times(1)).save(treatmentHistoryCaptor.capture());
        TreatmentInstanceHistory history = treatmentHistoryCaptor.getValue();
        assertThat(history.getTreatmentCondition()).isEqualTo(TreatmentConditionType.REPLACED);
        assertThat(history.getTreatmentInstance()).isSameAs(t100);
        assertThat(history.getConsultationInstance()).isSameAs(consultationInstanceCaptor.getValue());
        verify(treatmentInstanceDomainService).validateReplacementTraceability(any(), replacedByTreatmentCaptor.capture(), any(), any());
        assertThat(replacedByTreatmentCaptor.getValue()).containsExactly(prestation);
        verify(prestationInstanceDomainService).validatePrestationInstanceReplacementTraceability(
                replacedWithoutTreatmentCaptor.capture(), any(), any());
        assertThat(replacedWithoutTreatmentCaptor.getValue()).isEmpty();
    }

    /**
     * CASO: Steps de la Restauración de resina de Bety enviados REPLACED y una prestación nueva (Corona) con
     * treatmentInstanceRef.id del mismo tratamiento; calculateTreatmentCondition devuelve IN_PROGRESS.
     * Regla: D-40 Caso B (a) — el tratamiento sigue vivo y solo cambia la prestación.
     * Validación: el tratamiento no cambia de condición (sin applyCondition ni evento); la prestación nueva se guarda
     * con ese TreatmentInstance; la trazabilidad recibe la prestación nueva en prestationInstancesNuevas y ningún
     * tratamiento nuevo (tercer argumento vacío).
     * Sin stubs lenient ni thenReturn por orden: calculatePrestationInstanceStatus responde según los steps recibidos
     * (REPLACED si ninguno está IN_PROGRESS) y calculateTreatmentCondition devuelve IN_PROGRESS solo si la lista recibida
     * contiene la Corona nueva (si la omite devuelve REPLACED, el use case aplica la condición y el test falla). La lectura
     * de la base devuelve la Restauración existente más lo guardado en el request.
     */
    @Test
    void execute_replaceWithNewPrestationOnSameTreatment_treatmentInstanceStaysInProgress() {
        TreatmentInstance t100 = existingTreatment(100L, TreatmentConditionType.IN_PROGRESS);
        PrestationInstance restoration = existingPrestationMock(812L, PrestationInstanceStatus.IN_PROGRESS, t100);
        stubExistingStepAdvance(restoration, 9L, PrestationStepStatus.IN_PROGRESS);
        when(treatmentInstanceQueryService.findByIdAndPatientId(100L, PATIENT_ID)).thenReturn(t100);
        when(prestationInstanceDomainService.calculatePrestationInstanceStatus(any(), any())).thenAnswer(inv -> {
            List<PrestationStepInstance> stepInstances = inv.getArgument(1);
            return stepInstances.stream().anyMatch(s -> s.getStatus() == PrestationStepStatus.IN_PROGRESS)
                    ? PrestationInstanceStatus.IN_PROGRESS
                    : PrestationInstanceStatus.REPLACED;
        });
        when(prestationInstanceRepository.findByTreatmentInstanceId(100L)).thenAnswer(inv -> dbPrestationsOf(100L, restoration));
        when(treatmentInstanceDomainService.calculateTreatmentCondition(anyList())).thenAnswer(inv ->
                containsNewPrestation(inv.getArgument(0)) ? TreatmentConditionType.IN_PROGRESS : TreatmentConditionType.REPLACED);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(), List.of(
                existingPrestation(812L, new StepItemDTO(9L, null, PrestationStepStatus.REPLACED)),
                newPrestation(4L, refId(100L), null, null, null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(t100, never()).applyCondition(any());
        verifyNoInteractions(treatmentInstanceHistoryRepository);
        verify(prestationInstanceRepository, atLeastOnceSave()).save(prestationCaptor.capture());
        PrestationInstance corona = lastOf(prestationCaptor);
        assertThat(corona.getTreatmentInstance()).isSameAs(t100);
        assertThat(corona.getStatus()).isEqualTo(PrestationInstanceStatus.IN_PROGRESS);
        assertThat(restoration.getStatus()).isEqualTo(PrestationInstanceStatus.REPLACED);
        verify(treatmentInstanceDomainService).validateReplacementTraceability(
                any(), any(), newTreatmentsCaptor.capture(), newPrestationsCaptor.capture());
        assertThat(newTreatmentsCaptor.getValue()).isEmpty();
        assertThat(newPrestationsCaptor.getValue()).hasSize(1);
        assertThat(newPrestationsCaptor.getValue().get(0)).isSameAs(corona);
        assertThat(newPrestationsCaptor.getValue().get(0).getTreatmentInstance()).isSameAs(t100);
    }

    // ─── Validaciones cruzadas al final (D-53, D-57, ADR-0033) ────────────────

    /**
     * CASO (Alan): prótesis PREEXISTING 12/13/14 (tmp-1), extracción del 11 (tmp-2) que nace y cierra FINISHED, y un
     * Agregado que nace como tratamiento nuevo sobre el 11 (tmp-3) con la prótesis como origen.
     * Regla: D-53 — la colisión se evalúa una única vez, con todo persistido y flusheado.
     * Validación: todos los save (tratamientos, prestaciones, historiales) ocurren antes del flush(); después va una
     * única llamada a validateLocationCollision (times(1)) y una a validatePrestationInstancePending, en ese orden;
     * nunca una validación de colisión entre dos save. El primer argumento de cada validación trae exactamente lo que
     * nació: los tres TreatmentInstance guardados (la prótesis PREEXISTING, la extracción del 11 y el tratamiento del
     * Agregado, también sobre el 11) y las dos PrestationInstance guardadas (extracción y Agregado). La extracción queda
     * con getStatus() = FINISHED, o sea con el estado derivado ya aplicado antes de validar; sin eso el Agregado
     * chocaría con la extracción todavía viva.
     * El estado derivado ya está aplicado en el momento de cada validación (foto tomada dentro de la llamada): al validar
     * la colisión las condiciones son [PREEXISTING, FINISHED, IN_PROGRESS] y al validar pendientes los estados son
     * [FINISHED, IN_PROGRESS]; un use case que deriva después de las validaciones cruzadas falla.
     * Los estados derivados no dependen del orden de las llamadas: la prestación deriva FINISHED solo si todos sus
     * steps recibidos están FINISHED, y el tratamiento deriva FINISHED solo si todas las prestaciones recibidas lo están.
     */
    @Test
    void execute_crossValidations_runOnceAfterAllSavesAndFlush() {
        when(prestationInstanceDomainService.calculatePrestationInstanceStatus(any(), any())).thenAnswer(inv -> {
            List<PrestationStepInstance> stepInstances = inv.getArgument(1);
            return stepInstances.stream().allMatch(s -> s.getStatus() == PrestationStepStatus.FINISHED)
                    ? PrestationInstanceStatus.FINISHED
                    : PrestationInstanceStatus.IN_PROGRESS;
        });
        when(treatmentInstanceDomainService.calculateTreatmentCondition(anyList())).thenAnswer(inv -> {
            List<PrestationInstance> received = inv.getArgument(0);
            return !received.isEmpty() && received.stream().allMatch(p -> p.getStatus() == PrestationInstanceStatus.FINISHED)
                    ? TreatmentConditionType.FINISHED
                    : TreatmentConditionType.IN_PROGRESS;
        });
        // Foto del estado en el momento exacto de cada validación cruzada: lo que leería el domain service real.
        AtomicReference<List<TreatmentConditionType>> conditionsAtCollision = new AtomicReference<>();
        AtomicReference<List<PrestationInstanceStatus>> statusesAtPending = new AtomicReference<>();
        doAnswer(inv -> {
            List<TreatmentInstance> received = inv.getArgument(0);
            conditionsAtCollision.set(received.stream().map(TreatmentInstance::getTreatmentCondition).toList());
            events.add("collision");
            return null;
        }).when(treatmentInstanceDomainService).validateLocationCollision(any(), any());
        doAnswer(inv -> {
            List<PrestationInstance> received = inv.getArgument(0);
            statusesAtPending.set(received.stream().map(PrestationInstance::getStatus).toList());
            events.add("pending");
            return null;
        }).when(prestationInstanceDomainService).validatePrestationInstancePending(any(), any());
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1", scope(Tooth.T12, Tooth.T13, Tooth.T14), 5L, TreatmentConditionType.PREEXISTING),
                        newTreatment("tmp-2", scope(Tooth.T11), 5L, null),
                        newTreatment("tmp-3", scope(Tooth.T11), 5L, null)),
                List.of(newPrestation(TYPE_ID, ref("tmp-2"), null, null, null,
                                new StepItemDTO(null, STEP_1, PrestationStepStatus.FINISHED),
                                new StepItemDTO(null, STEP_2, PrestationStepStatus.FINISHED)),
                        newPrestation(4L, ref("tmp-3"), null, ref("tmp-1"), null,
                                new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        InOrder inOrder = inOrder(prestationInstanceRepository, treatmentInstanceDomainService, prestationInstanceDomainService);
        inOrder.verify(prestationInstanceRepository).flush();
        inOrder.verify(treatmentInstanceDomainService).validateLocationCollision(any(), any());
        inOrder.verify(prestationInstanceDomainService).validatePrestationInstancePending(any(), any());
        verify(treatmentInstanceDomainService, times(1)).validateLocationCollision(newTreatmentsCaptor.capture(), any());
        verify(prestationInstanceDomainService, times(1)).validatePrestationInstancePending(newPrestationsCaptor.capture(), any());
        verify(treatmentInstanceRepository, atLeastOnceSave()).save(treatmentCaptor.capture());
        verify(prestationInstanceRepository, atLeastOnceSave()).save(prestationCaptor.capture());
        List<PrestationInstance> savedPrestationsInOrder = new ArrayList<>(new LinkedHashSet<>(prestationCaptor.getAllValues()));
        assertThat(savedPrestationsInOrder).hasSize(2);
        List<TreatmentInstance> savedTreatmentsInOrder = new ArrayList<>(new LinkedHashSet<>(treatmentCaptor.getAllValues()));
        assertThat(savedTreatmentsInOrder).hasSize(3);
        assertThat(savedTreatmentsInOrder.get(0).getScope().getTooth()).containsExactly(Tooth.T12, Tooth.T13, Tooth.T14);
        assertThat(savedTreatmentsInOrder.get(1).getScope().getTooth()).containsExactly(Tooth.T11);
        assertThat(savedTreatmentsInOrder.get(2).getScope().getTooth()).containsExactly(Tooth.T11);
        assertThat(newTreatmentsCaptor.getValue()).containsExactlyElementsOf(savedTreatmentsInOrder);
        assertThat(savedPrestationsInOrder.get(0).getTreatmentInstance()).isSameAs(savedTreatmentsInOrder.get(1));
        assertThat(savedPrestationsInOrder.get(1).getTreatmentInstance()).isSameAs(savedTreatmentsInOrder.get(2));
        assertThat(savedPrestationsInOrder.get(1).getOriginTreatmentInstance()).isSameAs(savedTreatmentsInOrder.get(0));
        assertThat(newPrestationsCaptor.getValue()).containsExactlyElementsOf(savedPrestationsInOrder);
        assertThat(savedPrestationsInOrder.get(0).getStatus()).isEqualTo(PrestationInstanceStatus.FINISHED);
        assertThat(savedPrestationsInOrder.get(1).getStatus()).isEqualTo(PrestationInstanceStatus.IN_PROGRESS);
        assertThat(conditionsAtCollision.get()).containsExactly(
                TreatmentConditionType.PREEXISTING, TreatmentConditionType.FINISHED, TreatmentConditionType.IN_PROGRESS);
        assertThat(statusesAtPending.get()).containsExactly(PrestationInstanceStatus.FINISHED, PrestationInstanceStatus.IN_PROGRESS);
        assertThat(events).contains("save", "flush", "collision", "pending");
        assertThat(events.lastIndexOf("save")).isLessThan(events.indexOf("flush"));
        assertThat(events.indexOf("collision")).isGreaterThan(events.indexOf("flush"));
        assertThat(events.indexOf("pending")).isGreaterThan(events.indexOf("collision"));
    }

    /**
     * CASO: findActiveByPatientId devuelve una lista de tratamientos activos y findInProgressByPatientId una de
     * prestaciones IN_PROGRESS (listas distintas de las del request); request con un tratamiento y una prestación nuevos.
     * Regla: § 7 paso 9 (D-35, D-37) — las validaciones cruzadas reciben lo ya persistido, leído de los repositorios
     * después del flush.
     * Validación: validateLocationCollision recibe como segundo argumento exactamente la lista de findActiveByPatientId y
     * validatePrestationInstancePending la de findInProgressByPatientId; ambas lecturas ocurren después del flush (InOrder).
     * El primer argumento de cada validación se captura y trae exactamente el TreatmentInstance y la PrestationInstance
     * nuevos guardados (ni la lista del repositorio ni una vacía).
     */
    @Test
    void execute_crossValidations_receiveListsReadFromRepositoriesAfterFlush() {
        List<TreatmentInstance> active = List.of(existingTreatment(900L, TreatmentConditionType.IN_PROGRESS));
        List<PrestationInstance> inProgress = List.of(existingPrestationMock(901L, PrestationInstanceStatus.IN_PROGRESS, null));
        when(treatmentInstanceRepository.findActiveByPatientId(PATIENT_ID)).thenReturn(active);
        when(prestationInstanceRepository.findInProgressByPatientId(PATIENT_ID)).thenReturn(inProgress);
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)),
                List.of(newPrestation(TYPE_ID, ref("tmp-1"), null, null, null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(treatmentInstanceDomainService).validateLocationCollision(newTreatmentsCaptor.capture(), eq(active));
        verify(prestationInstanceDomainService).validatePrestationInstancePending(newPrestationsCaptor.capture(), eq(inProgress));
        verify(treatmentInstanceRepository, atLeastOnceSave()).save(treatmentCaptor.capture());
        verify(prestationInstanceRepository, atLeastOnceSave()).save(prestationCaptor.capture());
        assertThat(newTreatmentsCaptor.getValue()).containsExactly(lastOf(treatmentCaptor));
        assertThat(newPrestationsCaptor.getValue()).containsExactly(lastOf(prestationCaptor));
        InOrder collision = inOrder(prestationInstanceRepository, treatmentInstanceRepository, treatmentInstanceDomainService);
        collision.verify(prestationInstanceRepository).flush();
        collision.verify(treatmentInstanceRepository).findActiveByPatientId(PATIENT_ID);
        collision.verify(treatmentInstanceDomainService).validateLocationCollision(any(), eq(active));
        InOrder pending = inOrder(prestationInstanceRepository, prestationInstanceDomainService);
        pending.verify(prestationInstanceRepository).flush();
        pending.verify(prestationInstanceRepository).findInProgressByPatientId(PATIENT_ID);
        pending.verify(prestationInstanceDomainService).validatePrestationInstancePending(any(), eq(inProgress));
    }

    /**
     * CASO: Colocación nueva con todos sus steps resueltos y dos Agregados que la referencian por tempId como origen.
     * Regla: D-38 / D-53 — el estado del origen se valida con el estado ya derivado.
     * Validación: calculateTreatmentCondition antes de validateOriginState, y validateOriginState recibe el origen una
     * sola vez (aunque dos prestaciones lo referencien).
     */
    @Test
    void execute_originStateValidatedAfterDerivation_usesDerivedState() {
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null),
                        newTreatment("tmp-2", scope(Tooth.T17), 5L, null),
                        newTreatment("tmp-3", scope(Tooth.T18), 5L, null)),
                List.of(newPrestation(TYPE_ID, ref("tmp-1"), null, null, null,
                                new StepItemDTO(null, STEP_1, PrestationStepStatus.FINISHED),
                                new StepItemDTO(null, STEP_2, PrestationStepStatus.FINISHED)),
                        newPrestation(4L, ref("tmp-2"), null, ref("tmp-1"), null,
                                new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS)),
                        newPrestation(4L, ref("tmp-3"), null, ref("tmp-1"), null,
                                new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        InOrder inOrder = inOrder(treatmentInstanceDomainService);
        inOrder.verify(treatmentInstanceDomainService, org.mockito.Mockito.atLeastOnce()).calculateTreatmentCondition(any());
        inOrder.verify(treatmentInstanceDomainService).validateOriginState(any());
        verify(treatmentInstanceDomainService, times(1)).validateOriginState(originCaptor.capture());
        verify(treatmentInstanceRepository, atLeastOnceSave()).save(treatmentCaptor.capture());
        assertThat(originCaptor.getValue()).isSameAs(savedTreatmentOnTooth(Tooth.T16));
    }

    /**
     * CASO: Todas las escrituras se hacen y validateLocationCollision lanza ConflictException.
     * Regla: D-57 — el @Transactional revierte lo escrito; el use case solo tiene que propagar y no cerrar la consulta.
     * Validación: la excepción se propaga tal cual; changeConsultationStatusUseCase.execute nunca se invoca y la
     * respuesta no se arma (messageSource sin uso).
     */
    @Test
    void execute_crossValidationFailsMidBatch_propagatesExceptionAndDoesNotCloseConsultation() {
        ConflictException conflict = conflict("exception.treatmentInstanceDomainService.validateLocationCollision.user");
        doThrow(conflict).when(treatmentInstanceDomainService).validateLocationCollision(any(), any());
        ConsultationInstanceCreateRequestDTO dto = request(List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)), List.of());

        assertThatThrownBy(() -> useCase.execute(dto)).isSameAs(conflict);

        verify(treatmentInstanceRepository, atLeastOnceSave()).save(any(TreatmentInstance.class));
        verify(changeConsultationStatusUseCase, never()).execute(any(), any());
        verifyNoInteractions(messageSource);
    }

    /**
     * CASO: Reflexión sobre execute.
     * Regla: D-57 — la atomicidad depende de @Transactional (las excepciones de negocio son runtime y revierten por
     * defecto).
     * Validación: el método execute lleva @Transactional.
     */
    @Test
    void execute_isTransactional_methodAnnotatedWithTransactional() throws NoSuchMethodException {
        assertThat(CreateConsultationInstanceUseCase.class
                .getMethod("execute", ConsultationInstanceCreateRequestDTO.class)
                .isAnnotationPresent(Transactional.class)).isTrue();
    }

    /**
     * CASO: Request mínimo válido; la ConsultationInstance se guarda con id 55.
     * Regla: § 7 paso 10 — el cierre de la consulta es lo último del flujo.
     * Validación: Response<Long> con success=true y data=55; changeConsultationStatusUseCase.execute(consultation,
     * FINISHED) se invoca una vez, después de las validaciones cruzadas (InOrder).
     */
    @Test
    void execute_success_closesConsultationAndReturnsInstanceId() {
        ConsultationInstanceCreateRequestDTO dto = request(List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)), List.of());

        Response<Long> response = useCase.execute(dto);

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEqualTo(55L);
        InOrder inOrder = inOrder(treatmentInstanceDomainService, prestationInstanceDomainService, changeConsultationStatusUseCase);
        inOrder.verify(treatmentInstanceDomainService).validateLocationCollision(any(), any());
        inOrder.verify(prestationInstanceDomainService).validatePrestationInstancePending(any(), any());
        inOrder.verify(changeConsultationStatusUseCase).execute(consultation, ConsultationStatusType.FINISHED);
        verify(changeConsultationStatusUseCase, times(1)).execute(any(), any());
    }

    // ─── Precio y promoción (ADR-0023) ────────────────────────────────────────

    /**
     * CASO: Prestación nueva con promotionId 9; promotion.calculateAmount(price) devuelve 150.00.
     * Regla: ADR-0023 — el use case calcula el monto de la promoción y build lo recibe ya calculado.
     * Validación: la PrestationInstance guardada trae promotionAmount = 150.00, price = el de currentPrice (1000.00) y
     * la promoción; calculateAmount se invocó con ese precio.
     */
    @Test
    void execute_promotionAmountCalculatedByUseCase_passedAlreadyCalculatedToBuild() {
        Promotion promotion = mock(Promotion.class);
        when(promotion.calculateAmount(new BigDecimal("1000.00"))).thenReturn(new BigDecimal("150.00"));
        when(promotionQueryService.findById(9L)).thenReturn(promotion);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(), List.of(
                newPrestation(TYPE_ID, null, scope(Tooth.T16), null, 9L,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(prestationInstanceRepository, atLeastOnceSave()).save(prestationCaptor.capture());
        PrestationInstance saved = lastOf(prestationCaptor);
        assertThat(saved.getPromotionAmount()).isEqualByComparingTo("150.00");
        assertThat(saved.getPrice()).isEqualByComparingTo("1000.00");
        assertThat(saved.getPromotion()).isSameAs(promotion);
    }

    // ─── Caso C de D-40 y variantes de referencia (D-60, D-38, D-61) ──────────

    /**
     * CASO: T100 (diente 16) con dos prestaciones vivas A y B; el request manda los steps pendientes de A como REPLACED;
     * calculatePrestationInstanceStatus devuelve REPLACED y calculateTreatmentCondition (sobre A y B) devuelve
     * IN_PROGRESS.
     * Regla: D-39 / D-40 Caso C — si queda una prestación viva, el tratamiento no se cierra.
     * Validación: A.applyStatus(REPLACED); sin applyCondition ni fila de historial del tratamiento; A entra a
     * prestationInstancesReplacedByTreatment, prestationInstancesReplacedWithoutTreatment llega vacía y la derivación se
     * calculó sobre A y B. calculateTreatmentCondition es un thenAnswer que devuelve IN_PROGRESS solo si la lista
     * recibida contiene a B (si B queda afuera devuelve REPLACED, el use case cierra el tratamiento y el test falla).
     */
    @Test
    void execute_replaceOneOfTwoLivePrestations_treatmentInstanceStaysInProgress() {
        TreatmentInstance t100 = existingTreatment(100L, TreatmentConditionType.IN_PROGRESS);
        PrestationInstance prestationA = existingPrestationMock(812L, PrestationInstanceStatus.IN_PROGRESS, t100);
        PrestationInstance prestationB = existingPrestationMock(813L, PrestationInstanceStatus.IN_PROGRESS, t100);
        stubExistingStepAdvance(prestationA, 9L, PrestationStepStatus.IN_PROGRESS);
        when(prestationInstanceDomainService.calculatePrestationInstanceStatus(any(), any())).thenReturn(PrestationInstanceStatus.REPLACED);
        when(prestationInstanceRepository.findByTreatmentInstanceId(100L)).thenReturn(List.of(prestationA, prestationB));
        when(treatmentInstanceDomainService.calculateTreatmentCondition(anyList())).thenAnswer(inv -> {
            List<PrestationInstance> received = inv.getArgument(0);
            return received.contains(prestationB) ? TreatmentConditionType.IN_PROGRESS : TreatmentConditionType.REPLACED;
        });
        ConsultationInstanceCreateRequestDTO dto = request(List.of(),
                List.of(existingPrestation(812L, new StepItemDTO(9L, null, PrestationStepStatus.REPLACED))));

        useCase.execute(dto);

        verify(prestationA).applyStatus(PrestationInstanceStatus.REPLACED);
        verify(t100, never()).applyCondition(any());
        verifyNoInteractions(treatmentInstanceHistoryRepository);
        verify(treatmentInstanceDomainService).calculateTreatmentCondition(treatmentPrestationsCaptor.capture());
        assertThat(treatmentPrestationsCaptor.getValue()).containsExactlyInAnyOrder(prestationA, prestationB);
        verify(treatmentInstanceDomainService).validateReplacementTraceability(any(), replacedByTreatmentCaptor.capture(), any(), any());
        assertThat(replacedByTreatmentCaptor.getValue()).containsExactly(prestationA);
        verify(prestationInstanceDomainService).validatePrestationInstanceReplacementTraceability(
                replacedWithoutTreatmentCaptor.capture(), any(), any());
        assertThat(replacedWithoutTreatmentCaptor.getValue()).isEmpty();
    }

    /**
     * CASO: prestation[{treatmentInstanceRef: {id: 145}}] y el tratamiento es de otro paciente.
     * Regla: D-60 — treatmentInstanceRef.id inexistente o de otro paciente es 404.
     * Validación: NotFoundException; la prestación no se guarda.
     */
    @Test
    void execute_treatmentInstanceRefIdOfAnotherPatient_throwsNotFoundException() {
        when(treatmentInstanceQueryService.findByIdAndPatientId(145L, PATIENT_ID)).thenThrow(notFound());
        ConsultationInstanceCreateRequestDTO dto = request(List.of(), List.of(
                newPrestation(TYPE_ID, refId(145L), null, null, null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        assertThatThrownBy(() -> useCase.execute(dto)).isInstanceOf(NotFoundException.class);

        verify(prestationInstanceRepository, never()).save(any());
    }

    /**
     * CASO: originTreatmentInstanceRef: {id: 145} y el tratamiento de origen es de otro paciente.
     * Regla: D-60 — originTreatmentInstanceRef.id inexistente o de otro paciente es 404.
     * Validación: NotFoundException; la prestación no se guarda.
     */
    @Test
    void execute_originTreatmentInstanceRefIdOfAnotherPatient_throwsNotFoundException() {
        when(treatmentInstanceQueryService.findByIdAndPatientId(145L, PATIENT_ID)).thenThrow(notFound());
        ConsultationInstanceCreateRequestDTO dto = request(List.of(), List.of(
                newPrestation(TYPE_ID, null, null, refId(145L), null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        assertThatThrownBy(() -> useCase.execute(dto)).isInstanceOf(NotFoundException.class);

        verify(prestationInstanceRepository, never()).save(any());
    }

    /**
     * CASO: originTreatmentInstanceRef: {tempId: "tmp-9"} sin tratamiento nuevo con ese tempId.
     * Regla: D-38 — un tempId de origen que no está en el request es 400.
     * Validación: BadRequestException; la prestación no se guarda.
     */
    @Test
    void execute_originTempIdWithoutMatch_throwsBadRequestException() {
        ConsultationInstanceCreateRequestDTO dto = request(List.of(), List.of(
                newPrestation(TYPE_ID, null, null, ref("tmp-9"), null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        assertThatThrownBy(() -> useCase.execute(dto)).isInstanceOf(BadRequestException.class);

        verify(prestationInstanceRepository, never()).save(any());
    }

    /**
     * CASO: Request solo con avance de steps de una prestación existente y un TreatmentInstance existente cerrado
     * CANCELED.
     * Regla: D-61 — la especialidad se valida solo al nacer, no al avanzar ni al cerrar lo existente.
     * Validación: dentistSpecialtyDomainService.validateAllowed nunca se invoca.
     */
    @Test
    void execute_advanceStepsAndCloseExistingTreatment_doesNotValidateSpecialty() {
        TreatmentInstance toClose = existingTreatment(145L, TreatmentConditionType.REQUIRED);
        when(treatmentInstanceQueryService.findByIdAndPatientId(145L, PATIENT_ID)).thenReturn(toClose);
        PrestationInstance prestation = existingPrestationMock(812L, PrestationInstanceStatus.IN_PROGRESS, null);
        stubExistingStepAdvance(prestation, 9L, PrestationStepStatus.IN_PROGRESS);
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(closeTreatment(145L, TreatmentConditionType.CANCELED)),
                List.of(existingPrestation(812L, new StepItemDTO(9L, null, PrestationStepStatus.FINISHED))));

        useCase.execute(dto);

        verifyNoInteractions(dentistSpecialtyDomainService);
    }

    /**
     * CASO: Prestación existente de scope propio (sin tratamiento) cuyos steps se mandan REPLACED.
     * Regla: D-40 Caso D — el sujeto que deriva a REPLACED es la propia prestación.
     * Validación: validatePrestationInstanceReplacementTraceability recibe esa prestación en
     * prestationInstancesReplacedWithoutTreatment y prestationInstancesReplacedByTreatment queda vacía. Los otros dos
     * argumentos (tratamientos nuevos y prestaciones nuevas) traen exactamente lo que nació en el request: el request
     * suma un tratamiento nuevo (diente 16) y una prestación nueva de scope propio (diente 24), que sirven de reemplazo.
     * calculatePrestationInstanceStatus responde según los steps recibidos (REPLACED si ninguno está IN_PROGRESS), así la
     * prestación nueva no queda REPLACED por un stub fijo.
     */
    @Test
    void execute_replaceOwnScopePrestation_passesItToPrestationReplacementTraceability() {
        PrestationInstance ownScope = existingPrestationMock(812L, PrestationInstanceStatus.IN_PROGRESS, null);
        stubExistingStepAdvance(ownScope, 9L, PrestationStepStatus.IN_PROGRESS);
        when(prestationInstanceDomainService.calculatePrestationInstanceStatus(any(), any())).thenAnswer(inv -> {
            List<PrestationStepInstance> stepInstances = inv.getArgument(1);
            return stepInstances.stream().anyMatch(s -> s.getStatus() == PrestationStepStatus.IN_PROGRESS)
                    ? PrestationInstanceStatus.IN_PROGRESS
                    : PrestationInstanceStatus.REPLACED;
        });
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)),
                List.of(existingPrestation(812L, new StepItemDTO(9L, null, PrestationStepStatus.REPLACED)),
                        newPrestation(TYPE_ID, null, scope(Tooth.T24), null, null,
                                new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(prestationInstanceDomainService).validatePrestationInstanceReplacementTraceability(
                replacedWithoutTreatmentCaptor.capture(), newTreatmentsCaptor.capture(), newPrestationsCaptor.capture());
        assertThat(replacedWithoutTreatmentCaptor.getValue()).containsExactly(ownScope);
        verify(treatmentInstanceRepository, atLeastOnceSave()).save(treatmentCaptor.capture());
        verify(prestationInstanceRepository, atLeastOnceSave()).save(prestationCaptor.capture());
        assertThat(newTreatmentsCaptor.getValue()).containsExactly(lastOf(treatmentCaptor));
        assertThat(newPrestationsCaptor.getValue()).containsExactly(lastOf(prestationCaptor));
        assertThat(lastOf(prestationCaptor).getStatus()).isEqualTo(PrestationInstanceStatus.IN_PROGRESS);
        verify(treatmentInstanceDomainService).validateReplacementTraceability(any(), replacedByTreatmentCaptor.capture(), any(), any());
        assertThat(replacedByTreatmentCaptor.getValue()).isEmpty();
    }

    /**
     * CASO: Un TreatmentInstance nuevo con tooth [16], una prestación nueva con scope propio tooth [24] y otra que
     * solo referencia el tratamiento por tempId.
     * Regla: D-08 — se valida cada scope construido, no las referencias.
     * Validación: validateToothListDoesNotCompleteLevel se invoca exactamente 2 veces (scope del tratamiento y scope
     * propio) con esos dos scopes, y no para la prestación que solo referencia.
     */
    @Test
    void execute_newScopes_validateToothListDoesNotCompleteLevelForEachOwnScope() {
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)),
                List.of(newPrestation(TYPE_ID, null, scope(Tooth.T24), null, null,
                                new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS)),
                        newPrestation(4L, ref("tmp-1"), null, null, null,
                                new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(scopeDomainService, times(2)).validateToothListDoesNotCompleteLevel(scopeCaptor.capture());
        assertThat(scopeCaptor.getAllValues()).extracting(Scope::getTooth)
                .containsExactlyInAnyOrder(List.of(Tooth.T16), List.of(Tooth.T24));
    }

    // ─── Origen y referencias compartidas (D-23, D-38, D-54) ──────────────────

    /**
     * CASO (Alan): tmp-1 PREEXISTING [12,13,14] y una prestación sobre un tratamiento nuevo tmp-2 (diente 11) con
     * originTreatmentInstanceRef: {tempId: "tmp-1"}.
     * Regla: D-54 — el origen nace en el mismo request y se referencia por tempId; convive con treatmentInstanceRef.
     * Validación: getOriginTreatmentInstance() de la PrestationInstance guardada es el mismo objeto guardado para tmp-1
     * y getTreatmentInstance() el de tmp-2; el origen entra a validateOriginState. validateReferenceNotTerminal se invoca
     * una vez, con tmp-2, y nunca con tmp-1: un origen PREEXISTING es válido y no pasa por el chequeo de referencia no terminal
     * (con el domain service mockeado, un llamado equivocado no lanzaría; solo el verify lo detecta).
     */
    @Test
    void execute_originReferencesTempIdBornInSameRequest_resolvesCorrectly() {
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1", scope(Tooth.T12, Tooth.T13, Tooth.T14), 5L, TreatmentConditionType.PREEXISTING),
                        newTreatment("tmp-2", scope(Tooth.T11), 5L, null)),
                List.of(newPrestation(TYPE_ID, ref("tmp-2"), null, ref("tmp-1"), null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(treatmentInstanceRepository, atLeastOnceSave()).save(treatmentCaptor.capture());
        TreatmentInstance tmp1 = savedTreatmentOnTooth(Tooth.T12);
        TreatmentInstance tmp2 = savedTreatmentOnTooth(Tooth.T11);
        verify(prestationInstanceRepository, atLeastOnceSave()).save(prestationCaptor.capture());
        PrestationInstance saved = lastOf(prestationCaptor);
        assertThat(saved.getOriginTreatmentInstance()).isSameAs(tmp1);
        assertThat(saved.getTreatmentInstance()).isSameAs(tmp2);
        verify(treatmentInstanceDomainService).validateOriginState(originCaptor.capture());
        assertThat(originCaptor.getValue()).isSameAs(tmp1);
        verify(treatmentInstanceDomainService, times(1)).validateReferenceNotTerminal(tmp2);
        verify(treatmentInstanceDomainService, never()).validateReferenceNotTerminal(tmp1);
    }

    /**
     * CASO: tmp-1 [16] y dos prestaciones nuevas (Restauración y Sellador) con treatmentInstanceRef: {tempId: "tmp-1"}.
     * Regla: D-23 — varias prestaciones pueden referenciar el mismo tempId.
     * Validación: las dos PrestationInstance guardadas tienen getTreatmentInstance() igual al TreatmentInstance guardado
     * para tmp-1; no lanza.
     */
    @Test
    void execute_twoNewPrestationsReferenceSameTempId_bothResolveToSameTreatment() {
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)),
                List.of(newPrestation(TYPE_ID, ref("tmp-1"), null, null, null,
                                new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS)),
                        newPrestation(4L, ref("tmp-1"), null, null, null,
                                new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        assertThatCode(() -> useCase.execute(dto)).doesNotThrowAnyException();

        verify(treatmentInstanceRepository, atLeastOnceSave()).save(treatmentCaptor.capture());
        TreatmentInstance tmp1 = lastOf(treatmentCaptor);
        verify(prestationInstanceRepository, atLeastOnceSave()).save(prestationCaptor.capture());
        List<PrestationInstance> saved = new ArrayList<>(new LinkedHashSet<>(prestationCaptor.getAllValues()));
        assertThat(saved).hasSize(2);
        assertThat(saved).allSatisfy(p -> assertThat(p.getTreatmentInstance()).isSameAs(tmp1));
    }

    /**
     * CASO: Prestación existente con un step nuevo {stepId: 999} que no existe en el catálogo.
     * Regla: § 7 paso 6.1 — primero se resuelven todos los steps (404), recién después se valida el avance.
     * Validación: NotFoundException (404); validateStepAdvancement nunca se invoca.
     */
    @Test
    void execute_unknownStepIdInExistingPrestation_throwsNotFoundBeforeAdvancementValidation() {
        PrestationInstance prestation = existingPrestationMock(812L, PrestationInstanceStatus.IN_PROGRESS, null);
        when(prestationInstanceQueryService.findByIdAndPatientId(812L, PATIENT_ID)).thenReturn(prestation);
        when(prestationStepService.findById(999L)).thenThrow(notFound());
        ConsultationInstanceCreateRequestDTO dto = request(List.of(),
                List.of(existingPrestation(812L, new StepItemDTO(null, 999L, PrestationStepStatus.IN_PROGRESS))));

        assertThatThrownBy(() -> useCase.execute(dto)).isInstanceOf(NotFoundException.class);

        verify(prestationInstanceDomainService, never()).validateStepAdvancement(any(), any(), any(), any());
    }

    /**
     * CASO: Bety solo con una Consulta (treatmentInstance = null y una prestación nueva con scope propio), y el caso inverso
     * (prestation = null con un tratamiento nuevo en el diente 16).
     * Regla: D-31 — una de las dos listas puede venir null y se trata como vacía; es el request más común.
     * Validación: en los dos requests no lanza (ni NullPointerException) y devuelve Response<Long> con el id de la
     * ConsultationInstance guardada (55).
     */
    @Test
    void execute_nullTreatmentInstanceOrPrestationList_isTreatedAsEmpty() {
        ConsultationInstanceCreateRequestDTO withoutTreatments = request(null,
                List.of(newPrestation(TYPE_ID, null, scope(Tooth.T24), null, null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));
        assertThat(useCase.execute(withoutTreatments).data()).isEqualTo(CONSULTATION_INSTANCE_ID);

        ConsultationInstanceCreateRequestDTO withoutPrestations = request(
                List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)), null);
        assertThat(useCase.execute(withoutPrestations).data()).isEqualTo(CONSULTATION_INSTANCE_ID);
    }

    // ─── Orquestación de las prestaciones (§ 7 pasos 6 y 7) ───────────────────

    /**
     * CASO: Bety, Restauración nueva (tipo con allowedScopes = {TOOTH}) sobre el tratamiento nuevo tmp-1 del diente 16;
     * resolveLocation devuelve la ubicación efectiva.
     * Regla: § 7 pasos 7.4 y 7.5 — si el tipo admite ubicación, el use case calcula la ubicación efectiva y la pasa a las
     * reglas de ubicación del tipo.
     * Validación: validateScopeAllowedForType, validateMaxTooth, validateSameMaxillary y validateSplintingContiguity se
     * invocan una vez cada uno con el tipo y ese mismo Scope; validatePrestationWithoutOwnLocation y
     * validateOriginRequired también se invocan una vez.
     */
    @Test
    void execute_newPrestationWithLocation_validatesLocationRulesWithEffectiveLocation() {
        Scope effective = Scope.build(List.of(Tooth.T16), null, null, null, false);
        when(type.getAllowedScopes()).thenReturn(Set.of(PrestationScopeType.TOOTH));
        when(prestationInstanceDomainService.resolveLocation(any(), any())).thenReturn(effective);
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)),
                List.of(newPrestation(TYPE_ID, ref("tmp-1"), null, null, null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(prestationInstanceDomainService, times(1)).validateScopeAllowedForType(type, effective);
        verify(prestationInstanceDomainService, times(1)).validateMaxTooth(type, effective);
        verify(prestationInstanceDomainService, times(1)).validateSameMaxillary(type, effective);
        verify(prestationInstanceDomainService, times(1)).validateSplintingContiguity(type, effective);
        verify(prestationInstanceDomainService, times(1)).validatePrestationWithoutOwnLocation(eq(type), any(), any(), any());
        verify(prestationInstanceDomainService, times(1)).validateOriginRequired(eq(type), any());
    }

    /**
     * CASO: Rebasado nuevo (tipo con allowedScopes vacío) con originTreatmentInstanceRef {id: 145}, tratamiento FINISHED
     * del paciente.
     * Regla: § 7 pasos 7.4 y 7.5 — un tipo sin ubicación propia no calcula ubicación efectiva ni pasa por las reglas de
     * ubicación; sí pasa siempre por la exigencia de origen y por el rechazo de ubicación propia.
     * Validación: resolveLocation, validateScopeAllowedForType, validateMaxTooth, validateSameMaxillary y
     * validateSplintingContiguity nunca se invocan; validatePrestationWithoutOwnLocation y validateOriginRequired se
     * invocan una vez cada uno con el tipo y el origen.
     */
    @Test
    void execute_newPrestationWithoutOwnLocationType_skipsLocationRules() {
        TreatmentInstance origin = existingTreatment(145L, TreatmentConditionType.FINISHED);
        when(treatmentInstanceQueryService.findByIdAndPatientId(145L, PATIENT_ID)).thenReturn(origin);
        when(type.getAllowedScopes()).thenReturn(Set.of());
        TreatmentInstanceRefDTO originRef = refId(145L);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(),
                List.of(newPrestation(TYPE_ID, null, null, originRef, null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(prestationInstanceDomainService, never()).resolveLocation(any(), any());
        verify(prestationInstanceDomainService, never()).validateScopeAllowedForType(any(), any());
        verify(prestationInstanceDomainService, never()).validateMaxTooth(any(), any());
        verify(prestationInstanceDomainService, never()).validateSameMaxillary(any(), any());
        verify(prestationInstanceDomainService, never()).validateSplintingContiguity(any(), any());
        verify(prestationInstanceDomainService, times(1)).validatePrestationWithoutOwnLocation(eq(type), any(), any(), eq(originRef));
        verify(prestationInstanceDomainService, times(1)).validateOriginRequired(type, origin);
    }

    /**
     * CASO: Prestación nueva con dos steps {stepId: 1} y {stepId: 2}.
     * Regla: § 7 paso 7.6 — los steps se resuelven y se valida su avance antes de crear nada de la prestación.
     * Validación: validatePrestationStep(tipo, stepId) se invoca una vez por cada step; validateStepAdvancement con
     * prestationInstance null (no hay estado previo), el catálogo exacto que devuelve findAllPrestationStepsByPrestation,
     * sin steps persistidos y con exactamente los dos StepItemDTO enviados, se invoca antes de cualquier save de la
     * PrestationInstance (InOrder). Un use case que pase List.of() como catálogo o como steps del request falla.
     */
    @Test
    void execute_newPrestation_resolvesAndValidatesStepsBeforeCreatingAnything() {
        StepItemDTO firstStep = new StepItemDTO(null, STEP_1, PrestationStepStatus.FINISHED);
        StepItemDTO secondStep = new StepItemDTO(null, STEP_2, PrestationStepStatus.IN_PROGRESS);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(),
                List.of(newPrestation(TYPE_ID, null, scope(Tooth.T16), null, null, firstStep, secondStep)));

        useCase.execute(dto);

        verify(prestationInstanceDomainService, times(1)).validatePrestationStep(TYPE_ID, STEP_1);
        verify(prestationInstanceDomainService, times(1)).validatePrestationStep(TYPE_ID, STEP_2);
        InOrder inOrder = inOrder(prestationInstanceDomainService, prestationInstanceRepository);
        inOrder.verify(prestationInstanceDomainService, times(1))
                .validateStepAdvancement(isNull(), eq(catalog), eq(List.of()), eq(List.of(firstStep, secondStep)));
        inOrder.verify(prestationInstanceRepository, atLeastOnceSave()).save(any(PrestationInstance.class));
    }

    /**
     * CASO: Prestación existente 812 con el step 9 IN_PROGRESS persistido; el request lo avanza a FINISHED.
     * Regla: § 7 pasos 6.1 a 6.3 — el avance se valida con lo persistido antes de aplicarlo, y el estado de la prestación
     * se deriva después.
     * Validación: validateStepAdvancement se invoca una vez con la prestación existente, el catálogo, los steps
     * persistidos y los steps del request, antes de calculatePrestationInstanceStatus y de cualquier save del historial
     * del step, y antes de step.applyStatus(FINISHED) (InOrder: se valida antes de aplicar). Catálogo, steps persistidos y steps del request se afirman con su contenido exacto; un use case
     * que pase una lista vacía en cualquiera de los tres falla.
     */
    @Test
    void execute_advanceExistingPrestation_validatesAdvancementWithPersistedSteps() {
        PrestationInstance prestation = existingPrestationMock(812L, PrestationInstanceStatus.IN_PROGRESS, null);
        PrestationStepInstance step = existingStep(9L, PrestationStepStatus.IN_PROGRESS);
        when(prestationInstanceQueryService.findByIdAndPatientId(812L, PATIENT_ID)).thenReturn(prestation);
        when(prestationStepInstanceQueryService.findByIdAndPrestationInstanceId(9L, 812L)).thenReturn(step);
        // lenient: el design no fija cómo se leen los steps persistidos; el contenido lo ata el eq(List.of(step)) de abajo.
        lenient().when(prestationStepInstanceRepository.findByPrestationInstance(prestation)).thenReturn(List.of(step));
        StepItemDTO advance = new StepItemDTO(9L, null, PrestationStepStatus.FINISHED);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(), List.of(existingPrestation(812L, advance)));

        useCase.execute(dto);

        verify(prestationInstanceDomainService, times(1))
                .validateStepAdvancement(eq(prestation), eq(catalog), eq(List.of(step)), eq(List.of(advance)));
        InOrder derivation = inOrder(prestationInstanceDomainService);
        derivation.verify(prestationInstanceDomainService)
                .validateStepAdvancement(eq(prestation), eq(catalog), eq(List.of(step)), eq(List.of(advance)));
        derivation.verify(prestationInstanceDomainService).calculatePrestationInstanceStatus(any(), any());
        InOrder beforeApplying = inOrder(prestationInstanceDomainService, step);
        beforeApplying.verify(prestationInstanceDomainService)
                .validateStepAdvancement(eq(prestation), eq(catalog), eq(List.of(step)), eq(List.of(advance)));
        beforeApplying.verify(step).applyStatus(PrestationStepStatus.FINISHED);
        InOrder history = inOrder(prestationInstanceDomainService, prestationStepInstanceHistoryRepository);
        history.verify(prestationInstanceDomainService)
                .validateStepAdvancement(eq(prestation), eq(catalog), eq(List.of(step)), eq(List.of(advance)));
        history.verify(prestationStepInstanceHistoryRepository).save(any(PrestationStepInstanceHistory.class));
    }

    /**
     * CASO: Prestación nueva con descuento manual PERCENTAGE de 10 y sin promoción; precio vigente 1000.00.
     * Regla: ADR-0012 / ADR-0023 — el descuento manual viaja del request a la entidad, que calcula el monto.
     * Validación: la PrestationInstance guardada trae discountType PERCENTAGE, discountValue 10, discountAmount 100.00 y
     * finalAmount 900.00, y ninguna promoción.
     */
    @Test
    void execute_newPrestationWithManualDiscount_passesDiscountToBuild() {
        PrestationInstanceItemDTO withDiscount = new PrestationInstanceItemDTO(null, TYPE_ID, null, scope(Tooth.T16), null,
                null, DiscountType.PERCENTAGE,
                BigDecimal.TEN, List.of(new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS)));
        ConsultationInstanceCreateRequestDTO dto = request(List.of(), List.of(withDiscount));

        useCase.execute(dto);

        verify(prestationInstanceRepository, atLeastOnceSave()).save(prestationCaptor.capture());
        PrestationInstance saved = lastOf(prestationCaptor);
        assertThat(saved.getDiscountType()).isEqualTo(DiscountType.PERCENTAGE);
        assertThat(saved.getDiscountValue()).isEqualByComparingTo("10");
        assertThat(saved.getDiscountAmount()).isEqualByComparingTo("100.00");
        assertThat(saved.getFinalAmount()).isEqualByComparingTo("900.00");
        assertThat(saved.getPromotion()).isNull();
    }

    /**
     * CASO: Un tratamiento nuevo (Treatment con especialidades {Ortodoncia}, label "Caries") y una prestación nueva
     * (PrestationType con especialidades {Endodoncia}, label "Restauración") en el mismo request.
     * Regla: D-61 — cada ítem que nace se valida contra su propio catálogo y con el odontólogo de la consulta.
     * Validación: validateAllowed se invoca exactamente dos veces, una con (odontólogo, {Ortodoncia}, "Caries") y otra
     * con (odontólogo, {Endodoncia}, "Restauración"). Un cruce de especialidades, de labels o de odontólogo hace fallar
     * el verify; en un request con una sola de las dos cosas ese cruce no se podría observar.
     */
    @Test
    void execute_birthOfTreatmentAndPrestation_validatesSpecialtyWithEachCatalog() {
        Set<DentistSpecialty> treatmentSpecialties =
                Set.of(mock(DentistSpecialty.class));
        Set<DentistSpecialty> typeSpecialties =
                Set.of(mock(DentistSpecialty.class));
        when(treatment.getDentistSpecialties()).thenReturn(treatmentSpecialties);
        when(treatment.getLabel()).thenReturn("Caries");
        when(type.getDentistSpecialties()).thenReturn(typeSpecialties);
        when(type.getLabel()).thenReturn("Restauración");
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)),
                List.of(newPrestation(TYPE_ID, ref("tmp-1"), null, null, null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(dentistSpecialtyDomainService, times(1)).validateAllowed(consultation.getDentist(), treatmentSpecialties, "Caries");
        verify(dentistSpecialtyDomainService, times(1)).validateAllowed(consultation.getDentist(), typeSpecialties, "Restauración");
        verify(dentistSpecialtyDomainService, times(2)).validateAllowed(any(), any(), any());
    }

    /**
     * CASO: Prestación existente 812 (tipo Consulta) que avanza un step y una prestación nueva de tipo Radiografía en el
     * mismo request.
     * Regla: D-36 — la lista de tipos que se chequea trae el tipo de cada ítem de prestation[], nuevo o existente; armarla
     * es decisión del use case (§ 7 paso 2.3).
     * Validación: validatePrestationUnique se invoca una vez con una lista que contiene exactamente los dos tipos, el de la
     * prestación existente y el de la nueva.
     */
    @Test
    void execute_uniqueCheckReceivesTypesOfExistingAndNewPrestations() {
        PrestationType consulta = mock(PrestationType.class);
        PrestationType radiografia = mock(PrestationType.class);
        lenient().when(consulta.getId()).thenReturn(2L);
        lenient().when(radiografia.getId()).thenReturn(4L);
        lenient().when(consulta.getDentistSpecialties()).thenReturn(Set.of());
        lenient().when(radiografia.getDentistSpecialties()).thenReturn(Set.of());
        PrestationInstance existing = existingPrestationMock(812L, PrestationInstanceStatus.IN_PROGRESS, null);
        when(existing.getType()).thenReturn(consulta);
        stubExistingStepAdvance(existing, 9L, PrestationStepStatus.IN_PROGRESS);
        when(prestationTypeService.findById(4L)).thenReturn(radiografia);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(), List.of(
                existingPrestation(812L, new StepItemDTO(9L, null, PrestationStepStatus.FINISHED)),
                newPrestation(4L, null, scope(Tooth.T24), null, null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        ArgumentCaptor<List<PrestationType>> typesCaptor = ArgumentCaptor.forClass(List.class);
        verify(prestationInstanceDomainService, times(1)).validatePrestationUnique(typesCaptor.capture());
        assertThat(typesCaptor.getValue()).containsExactlyInAnyOrder(consulta, radiografia);
    }

    /**
     * CASO: Agregado nuevo de un tipo con allowedScopes = {TOOTH}, que trae solo originTreatmentInstanceRef {id: 145}
     * (sin tratamiento ni scope propio); resolveLocation devuelve null.
     * Regla: D-07 — una prestación con ubicación que nace sin ubicación efectiva se rechaza adentro del domain service;
     * el use case tiene que invocar la validación también con ubicación null.
     * Validación: validateScopeAllowedForType se invoca una vez con (tipo, null). Un if (effective != null) en el use
     * case se saltearía la validación y el verify falla.
     */
    @Test
    void execute_newPrestationWithOnlyOriginAndRestrictedAllowedScopes_validatesScopeAllowedWithNullLocation() {
        TreatmentInstance origin = existingTreatment(145L, TreatmentConditionType.FINISHED);
        when(treatmentInstanceQueryService.findByIdAndPatientId(145L, PATIENT_ID)).thenReturn(origin);
        when(type.getAllowedScopes()).thenReturn(Set.of(PrestationScopeType.TOOTH));
        when(prestationInstanceDomainService.resolveLocation(any(), isNull())).thenReturn(null);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(),
                List.of(newPrestation(TYPE_ID, null, null, refId(145L), null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(prestationInstanceDomainService, times(1)).validateScopeAllowedForType(eq(type), isNull());
    }

    /**
     * CASO: Bety, tratamiento nuevo tmp-1 (diente 16) y tres prestaciones nuevas: una con scope propio tooth [24], otra
     * que referencia tmp-1 con treatmentInstanceRef, y un Rebasado (tipo sin ubicación) con originTreatmentInstanceRef
     * por tempId. Ninguna combina scope propio con treatmentInstanceRef (D-24).
     * Regla: § 7 pasos 7.4 y 7.5 / D-50 — el use case le pasa a los validadores de ubicación los datos reales del
     * request, no valores por defecto.
     * Validación: resolveLocation recibe (scope propio mapeado del 24, null) para la primera y (scope vacío, el
     * TreatmentInstance guardado para tmp-1) para la segunda; validatePrestationWithoutOwnLocation recibe, para cada
     * una, su tipo, su scope propio, su treatmentInstanceRef y su origen exactos (el Rebasado: su tipo, scope vacío,
     * tratamiento null y la referencia de origen). Un use case que pase siempre Scope.empty() y null hace que D-50 nunca
     * rechace.
     */
    @Test
    void execute_newPrestationOwnScopeAndTreatmentRef_passesExactDataToLocationValidators() {
        PrestationType rebasado = mock(PrestationType.class);
        lenient().when(rebasado.getId()).thenReturn(8L);
        lenient().when(rebasado.getDentistSpecialties()).thenReturn(Set.of());
        when(prestationTypeService.findById(8L)).thenReturn(rebasado);
        when(type.getAllowedScopes()).thenReturn(Set.of(PrestationScopeType.TOOTH));
        Scope effective = Scope.build(List.of(Tooth.T24), null, null, null, false);
        when(prestationInstanceDomainService.resolveLocation(any(), any())).thenReturn(effective);
        TreatmentInstanceRefDTO treatmentRef = ref("tmp-1");
        TreatmentInstanceRefDTO originRef = ref("tmp-1");
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1", scope(Tooth.T16), 5L, null)),
                List.of(newPrestation(TYPE_ID, null, scope(Tooth.T24), null, null,
                                new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS)),
                        newPrestation(TYPE_ID, treatmentRef, null, null, null,
                                new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS)),
                        newPrestation(8L, null, null, originRef, null,
                                new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(treatmentInstanceRepository, atLeastOnceSave()).save(treatmentCaptor.capture());
        TreatmentInstance tmp1 = lastOf(treatmentCaptor);
        ArgumentCaptor<Scope> ownScopeResolvedAlone = ArgumentCaptor.forClass(Scope.class);
        verify(prestationInstanceDomainService, times(1)).resolveLocation(ownScopeResolvedAlone.capture(), isNull());
        assertThat(ownScopeResolvedAlone.getValue().getTooth()).containsExactly(Tooth.T24);
        ArgumentCaptor<Scope> emptyScopeResolvedWithTreatment = ArgumentCaptor.forClass(Scope.class);
        verify(prestationInstanceDomainService, times(1)).resolveLocation(emptyScopeResolvedWithTreatment.capture(), eq(tmp1));
        assertThat(emptyScopeResolvedWithTreatment.getValue().isEmpty()).isTrue();
        ArgumentCaptor<Scope> firstOwnScope = ArgumentCaptor.forClass(Scope.class);
        verify(prestationInstanceDomainService, times(1))
                .validatePrestationWithoutOwnLocation(eq(type), firstOwnScope.capture(), isNull(), isNull());
        assertThat(firstOwnScope.getValue().getTooth()).containsExactly(Tooth.T24);
        ArgumentCaptor<Scope> treatmentRefOwnScope = ArgumentCaptor.forClass(Scope.class);
        verify(prestationInstanceDomainService, times(1))
                .validatePrestationWithoutOwnLocation(eq(type), treatmentRefOwnScope.capture(), eq(treatmentRef), isNull());
        assertThat(treatmentRefOwnScope.getValue().isEmpty()).isTrue();
        ArgumentCaptor<Scope> secondOwnScope = ArgumentCaptor.forClass(Scope.class);
        verify(prestationInstanceDomainService, times(1))
                .validatePrestationWithoutOwnLocation(eq(rebasado), secondOwnScope.capture(), isNull(), eq(originRef));
        assertThat(secondOwnScope.getValue().isEmpty()).isTrue();
    }

    /**
     * CASO: Agregado nuevo sobre un tratamiento nuevo tmp-1 (diente 11), con originTreatmentInstanceRef {id: 145} de un
     * tratamiento existente FINISHED del paciente.
     * Regla: D-38 / § 7 paso 9 — el estado del origen se valida con cada origen distinto, nazca en el request o ya esté
     * persistido.
     * Validación: validateOriginState se invoca una vez con ese tratamiento existente (resuelto con findByIdAndPatientId)
     * y validateReferenceNotTerminal nunca lo recibe (el chequeo de referencia no terminal es solo para
     * treatmentInstanceRef). Un use case que solo registra los orígenes por tempId no valida el origen por id.
     */
    @Test
    void execute_originReferencedById_validatesOriginStateAndRegistersIt() {
        TreatmentInstance origin = existingTreatment(145L, TreatmentConditionType.FINISHED);
        when(treatmentInstanceQueryService.findByIdAndPatientId(145L, PATIENT_ID)).thenReturn(origin);
        ConsultationInstanceCreateRequestDTO dto = request(
                List.of(newTreatment("tmp-1", scope(Tooth.T11), 5L, null)),
                List.of(newPrestation(TYPE_ID, ref("tmp-1"), null, refId(145L), null,
                        new StepItemDTO(null, STEP_1, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(treatmentInstanceDomainService, times(1)).validateOriginState(origin);
        verify(treatmentInstanceDomainService, never()).validateReferenceNotTerminal(origin);
    }

    /**
     * CASO: Prestación existente 812 (de tipo Restauración, id 6) con un step nuevo {stepId: 5} que se agrega al avance.
     * Regla: § 7 paso 6.1 — el 400 por step fuera del catálogo del tipo aplica también a las prestaciones existentes.
     * Validación: validatePrestationStep se invoca una vez con (id del tipo de la prestación existente, 5). Un use case
     * que solo llame a validatePrestationStep para prestaciones nuevas hace fallar el verify.
     */
    @Test
    void execute_existingPrestationWithNewCatalogStep_validatesStepBelongsToPrestationType() {
        Long existingTypeId = 6L;
        PrestationType existingType = mock(PrestationType.class);
        lenient().when(existingType.getId()).thenReturn(existingTypeId);
        PrestationInstance prestation = existingPrestationMock(812L, PrestationInstanceStatus.IN_PROGRESS, null);
        lenient().when(prestation.getType()).thenReturn(existingType);
        when(prestationInstanceQueryService.findByIdAndPatientId(812L, PATIENT_ID)).thenReturn(prestation);
        PrestationStep catalogStep = catalogStep(5L, 3);
        when(prestationStepService.findById(5L)).thenReturn(catalogStep);
        ConsultationInstanceCreateRequestDTO dto = request(List.of(),
                List.of(existingPrestation(812L, new StepItemDTO(null, 5L, PrestationStepStatus.IN_PROGRESS))));

        useCase.execute(dto);

        verify(prestationInstanceDomainService, times(1)).validatePrestationStep(existingTypeId, 5L);
    }

    // ─── Helpers de request ───────────────────────────────────────────────────

    private ConsultationInstanceCreateRequestDTO request(List<TreatmentInstanceItemDTO> treatments, List<PrestationInstanceItemDTO> prestations) {
        return new ConsultationInstanceCreateRequestDTO(CONSULTATION_ID, "observación", treatments, prestations);
    }

    private ScopeDTO scope(Tooth... teeth) {
        return new ScopeDTO(List.of(teeth), null, null, null, null);
    }

    private TreatmentInstanceItemDTO newTreatment(String tempId, ScopeDTO scope, Long treatmentId, TreatmentConditionType condition) {
        return new TreatmentInstanceItemDTO(null, tempId, scope, treatmentId, condition);
    }

    private TreatmentInstanceItemDTO closeTreatment(Long id, TreatmentConditionType condition) {
        return new TreatmentInstanceItemDTO(id, null, null, null, condition);
    }

    private PrestationInstanceItemDTO newPrestation(Long typeId, TreatmentInstanceRefDTO treatmentRef, ScopeDTO ownScope,
                                                    TreatmentInstanceRefDTO originRef, Long promotionId, StepItemDTO... steps) {
        return new PrestationInstanceItemDTO(null, typeId, treatmentRef, ownScope, originRef, promotionId, null, null, List.of(steps));
    }

    private PrestationInstanceItemDTO existingPrestation(Long id, StepItemDTO... steps) {
        return new PrestationInstanceItemDTO(id, null, null, null, null, null, null, null, List.of(steps));
    }

    private TreatmentInstanceRefDTO ref(String tempId) {
        return new TreatmentInstanceRefDTO(null, tempId);
    }

    private TreatmentInstanceRefDTO refId(Long id) {
        return new TreatmentInstanceRefDTO(id, null);
    }

    // ─── Helpers de colaboradores ─────────────────────────────────────────────

    private PrestationStep catalogStep(Long id, int position) {
        PrestationStep step = mock(PrestationStep.class);
        lenient().when(step.getId()).thenReturn(id);
        lenient().when(step.getPosition()).thenReturn(position);
        return step;
    }

    /** Simula el save de JPA: asigna id a las entidades reales y deja rastro del orden de escritura. */
    private <T> T persisted(T entity) {
        events.add("save");
        if (!Mockito.mockingDetails(entity).isMock() && ReflectionTestUtils.getField(entity, "id") == null) {
            ReflectionTestUtils.setField(entity, "id", ids.incrementAndGet());
        }
        if (entity instanceof PrestationInstance prestation && !Mockito.mockingDetails(entity).isMock()
                && savedPrestations.stream().noneMatch(p -> p == prestation)) {
            savedPrestations.add(prestation);
        }
        return entity;
    }

    /** Lo que devolvería la base para un tratamiento: las ya persistidas que se pasan más las reales guardadas en este request. */
    private List<PrestationInstance> dbPrestationsOf(Long treatmentId, PrestationInstance... alreadyPersisted) {
        List<PrestationInstance> result = new ArrayList<>(List.of(alreadyPersisted));
        savedPrestations.stream()
                .filter(p -> p.getTreatmentInstance() != null && treatmentId.equals(p.getTreatmentInstance().getId()))
                .forEach(result::add);
        return result;
    }

    /** True si la lista trae alguna prestación real (las nuevas que armó el use case; las existentes son mocks). */
    private boolean containsNewPrestation(List<PrestationInstance> prestations) {
        return prestations.stream().anyMatch(p -> !Mockito.mockingDetails(p).isMock());
    }

    /** TreatmentInstance existente (dato de entrada): la condición sigue a applyCondition. */
    private TreatmentInstance existingTreatment(Long id, TreatmentConditionType initial) {
        TreatmentInstance existing = mock(TreatmentInstance.class);
        AtomicReference<TreatmentConditionType> condition = new AtomicReference<>(initial);
        lenient().when(existing.getId()).thenReturn(id);
        lenient().when(existing.getPatient()).thenReturn(patient);
        lenient().when(existing.getTreatmentCondition()).thenAnswer(inv -> condition.get());
        lenient().doAnswer(inv -> {
            condition.set(inv.getArgument(0));
            return null;
        }).when(existing).applyCondition(any());
        return existing;
    }

    /** PrestationInstance existente (dato de entrada): el estado sigue a applyStatus. */
    private PrestationInstance existingPrestationMock(Long id, PrestationInstanceStatus initial, TreatmentInstance treatmentInstance) {
        PrestationInstance existing = mock(PrestationInstance.class);
        AtomicReference<PrestationInstanceStatus> status = new AtomicReference<>(initial);
        lenient().when(existing.getId()).thenReturn(id);
        lenient().when(existing.getPatient()).thenReturn(patient);
        lenient().when(existing.getType()).thenReturn(type);
        lenient().when(existing.getTreatmentInstance()).thenReturn(treatmentInstance);
        lenient().when(existing.getStatus()).thenAnswer(inv -> status.get());
        lenient().doAnswer(inv -> {
            status.set(inv.getArgument(0));
            return null;
        }).when(existing).applyStatus(any());
        return existing;
    }

    /** PrestationStepInstance existente (dato de entrada): el estado sigue a applyStatus. */
    private PrestationStepInstance existingStep(Long id, PrestationStepStatus initial) {
        PrestationStepInstance step = mock(PrestationStepInstance.class);
        AtomicReference<PrestationStepStatus> status = new AtomicReference<>(initial);
        lenient().when(step.getId()).thenReturn(id);
        lenient().when(step.getStatus()).thenAnswer(inv -> status.get());
        PrestationStep catalogStep = prestationStepService.findById(STEP_1);
        lenient().when(step.getStep()).thenReturn(catalogStep);
        lenient().doAnswer(inv -> {
            status.set(inv.getArgument(0));
            return null;
        }).when(step).applyStatus(any());
        return step;
    }

    /** Deja resueltos la prestación 812 y su step existente para los casos de avance. */
    private void stubExistingStepAdvance(PrestationInstance prestation, Long stepId, PrestationStepStatus persisted) {
        PrestationStepInstance step = existingStep(stepId, persisted);
        when(prestationInstanceQueryService.findByIdAndPatientId(prestation.getId(), PATIENT_ID)).thenReturn(prestation);
        when(prestationStepInstanceQueryService.findByIdAndPrestationInstanceId(stepId, prestation.getId())).thenReturn(step);
    }

    private NotFoundException notFound() {
        return new NotFoundException("exception.notFound.user", null, "exception.notFound.log", new Object[]{"test"}, LogLevel.WARN);
    }

    private ConflictException conflict(String key) {
        return new ConflictException(key, null, key.replace(".user", ".log"), new Object[]{"test"}, LogLevel.ERROR);
    }

    private ForbiddenException forbidden() {
        return new ForbiddenException("exception.dentistSpecialtyDomainService.validateAllowed.user", null,
                "exception.dentistSpecialtyDomainService.validateAllowed.log", new Object[]{"test"}, LogLevel.ERROR);
    }

    // ─── Helpers de captura ───────────────────────────────────────────────────

    private org.mockito.verification.VerificationMode atLeastOnceSave() {
        return org.mockito.Mockito.atLeastOnce();
    }

    private <T> T lastOf(ArgumentCaptor<T> captor) {
        return captor.getValue();
    }

    private TreatmentInstance savedTreatmentOnTooth(Tooth tooth) {
        return new ArrayList<>(new LinkedHashSet<>(treatmentCaptor.getAllValues())).stream()
                .filter(t -> t.getScope().getTooth().contains(tooth))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No se guardó ningún TreatmentInstance sobre el diente " + tooth));
    }
}
