package com.odontologiaintegralfm.feature.consultation.consultationinstance.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.authentication.model.Role;
import com.odontologiaintegralfm.feature.consultation.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.dto.CatalogTreeResponseDTO;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.dto.TreatmentWithPrestationTypeResponseDTO;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.prestation.dto.PrestationStepResponseDTO;
import com.odontologiaintegralfm.feature.consultation.prestation.dto.PrestationTypeWithStepsResponseDTO;
import com.odontologiaintegralfm.feature.consultation.prestation.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationTypePrice;
import com.odontologiaintegralfm.feature.consultation.prestation.model.Step;
import com.odontologiaintegralfm.feature.consultation.prestation.repository.IPrestationStepRepository;
import com.odontologiaintegralfm.feature.consultation.prestation.repository.IPrestationTypePriceRepository;
import com.odontologiaintegralfm.feature.consultation.prestation.repository.IPrestationTypeRepository;
import com.odontologiaintegralfm.feature.consultation.prestation.repository.TreatmentPrestationTypeLink;
import com.odontologiaintegralfm.feature.consultation.treatment.dto.TreatmentResponseDTO;
import com.odontologiaintegralfm.feature.consultation.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.treatment.mapper.TreatmentMapper;
import com.odontologiaintegralfm.feature.consultation.treatment.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.treatment.repository.ITreatmentRepository;
import com.odontologiaintegralfm.feature.dentist.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.model.DentistSpecialty;
import com.odontologiaintegralfm.feature.dentist.service.IDentistService;
import com.odontologiaintegralfm.feature.person.person.model.Person;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.AppException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests de la orquestación de GetCatalogTreeUseCase (REQ-039).
 *
 * Decisiones de armado (ADR-0031):
 * - Treatment, PrestationType, PrestationStep, Step, PrestationTypePrice, Dentist, DentistSpecialty, ConsultationInstance,
 *   Consultation, UserSec, Person y Role se mockean: son datos de entrada que el use case solo lee. Un mock devuelve
 *   null por defecto, así que los helpers dejan stubbeados (lenient) id, label, name y demás campos que el árbol copia.
 * - Los DTOs de salida y TreatmentPrestationTypeLink se construyen reales.
 * - TreatmentMapper está mockeado: el helper treatment(...) lo stubbea con el argumento exacto y devuelve un
 *   TreatmentResponseDTO propio de ese tratamiento.
 * - El filtro por especialidad y por fecha vive en las consultas (T-01): acá los repositorios devuelven lo que ya filtraron
 *   y se verifica con qué especialidad y fecha se los llamó (filas 1 y 3).
 * - Los stubs de repositorios usan matchers genéricos porque lo devuelto no depende de los argumentos; los argumentos
 *   se afirman con verify donde el plan los pide.
 * - Lo que no se stubbea devuelve el valor por defecto de Mockito (lista vacía): una consulta que el caso no define
 *   responde vacío, nunca null.
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class GetCatalogTreeUseCaseTest {

    private static final Long USER_PERSON_ID = 7L;
    private static final Long DENTIST_SPECIALTY_ID = 4L;
    private static final String DENTIST_ROLE = com.odontologiaintegralfm.feature.authentication.enums.Role.DENTIST.name();
    private static final String ADMIN_ROLE = com.odontologiaintegralfm.feature.authentication.enums.Role.ADMINISTRATOR.name();
    private static final String USE_CASE = "GetCatalogTreeUseCase";

    @Mock private AuthenticatedUserService authenticatedUserService;
    @Mock private IDentistService dentistService;
    @Mock private ConsultationInstanceQueryService consultationInstanceQueryService;
    @Mock private ConsultationInstanceDomainService consultationInstanceDomainService;
    @Mock private ITreatmentRepository treatmentRepository;
    @Mock private IPrestationTypeRepository prestationTypeRepository;
    @Mock private IPrestationStepRepository prestationStepRepository;
    @Mock private IPrestationTypePriceRepository prestationTypePriceRepository;
    @Mock private TreatmentMapper treatmentMapper;

    @InjectMocks private GetCatalogTreeUseCase useCase;

    /** El UserSec que devuelve getAuthenticatedUser(); lo arma loggedUser. */
    private UserSec authUser;

    // ─── Helpers de usuario, odontólogo e instancia ───────────────────────────

    /** Usuario autenticado con su Person y sus roles; va lenient porque es infraestructura del fixture. */
    private void loggedUser(long personId, String... roleNames) {
        Person person = mock(Person.class);
        lenient().when(person.getId()).thenReturn(personId);
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = mock(Role.class);
            lenient().when(role.getName()).thenReturn(roleName);
            roles.add(role);
        }
        authUser = mock(UserSec.class);
        lenient().when(authUser.getPerson()).thenReturn(person);
        lenient().when(authUser.getRolesList()).thenReturn(roles);
        lenient().when(authenticatedUserService.getAuthenticatedUser()).thenReturn(authUser);
    }

    private static Dentist dentistWithSpecialty(long specialtyId) {
        DentistSpecialty specialty = mock(DentistSpecialty.class);
        lenient().when(specialty.getId()).thenReturn(specialtyId);
        Dentist dentist = mock(Dentist.class);
        lenient().when(dentist.getDentistSpecialty()).thenReturn(specialty);
        return dentist;
    }

    /** Alta: el usuario autenticado tiene el Dentist de su Person. */
    private void loggedDentistOfSpecialty(long specialtyId) {
        // El Dentist se arma antes del when: crear mocks dentro de thenReturn deja un stubbing sin terminar.
        Dentist dentist = dentistWithSpecialty(specialtyId);
        when(dentistService.getById(USER_PERSON_ID)).thenReturn(Optional.of(dentist));
    }

    private static ConsultationInstance instance(long id, LocalDateTime createdAt, Dentist consultationDentist) {
        Consultation consultation = mock(Consultation.class);
        lenient().when(consultation.getDentist()).thenReturn(consultationDentist);
        ConsultationInstance instance = mock(ConsultationInstance.class);
        lenient().when(instance.getId()).thenReturn(id);
        lenient().when(instance.getCreatedAt()).thenReturn(createdAt);
        lenient().when(instance.getConsultation()).thenReturn(consultation);
        return instance;
    }

    // ─── Helpers de catálogo ──────────────────────────────────────────────────

    /** Tratamiento mockeado, con el mapper stubbeado (argumento exacto) para devolver su TreatmentResponseDTO. */
    private Treatment treatment(long id, String label) {
        return treatment(id, label, label.toLowerCase());
    }

    private Treatment treatment(long id, String label, String name) {
        Treatment treatment = mock(Treatment.class);
        lenient().when(treatment.getId()).thenReturn(id);
        lenient().when(treatment.getLabel()).thenReturn(label);
        lenient().when(treatment.getName()).thenReturn(name);
        lenient().when(treatmentMapper.toDTO(treatment))
                .thenReturn(new TreatmentResponseDTO(id, label, name, Set.of(TreatmentConditionType.REQUIRED)));
        return treatment;
    }

    private static PrestationType prestationType(long id, String label) {
        return prestationType(id, label.toUpperCase(), label, false, null, false, Set.of());
    }

    private static PrestationType prestationType(long id, String name, String label, boolean unique, Integer maxTooth,
                                                 boolean requiresOrigin, Set<PrestationScopeType> allowedScopes) {
        PrestationType type = mock(PrestationType.class);
        lenient().when(type.getId()).thenReturn(id);
        lenient().when(type.getName()).thenReturn(name);
        lenient().when(type.getLabel()).thenReturn(label);
        lenient().when(type.isUnique()).thenReturn(unique);
        lenient().when(type.getMaxTooth()).thenReturn(maxTooth);
        lenient().when(type.isRequiresOriginTreatment()).thenReturn(requiresOrigin);
        lenient().when(type.getAllowedScopes()).thenReturn(allowedScopes);
        return type;
    }

    /** Name que ordena al revés del id: ordenar por name da el orden descendente por id. */
    private static String invertedName(long id) {
        return String.format("N%04d", 9999 - id);
    }

    private static PrestationType unorderedByName(long id, String label) {
        return prestationType(id, invertedName(id), label, false, null, false, Set.of());
    }

    private static PrestationStep catalogStep(long id, int position, boolean required, String name, String label, PrestationType type) {
        Step step = mock(Step.class);
        lenient().when(step.getName()).thenReturn(name);
        lenient().when(step.getLabel()).thenReturn(label);
        PrestationStep catalogStep = mock(PrestationStep.class);
        lenient().when(catalogStep.getId()).thenReturn(id);
        lenient().when(catalogStep.getPosition()).thenReturn(position);
        lenient().when(catalogStep.isRequired()).thenReturn(required);
        lenient().when(catalogStep.getStep()).thenReturn(step);
        lenient().when(catalogStep.getPrestation()).thenReturn(type);
        return catalogStep;
    }

    /** Un step por prestación, con id propio (9000 + id de la prestación): el catálogo mínimo para que el armado no encuentre vacío. */
    private static List<PrestationStep> oneStepPerType(PrestationType... types) {
        return Stream.of(types)
                .map(type -> catalogStep(9000 + type.getId(), 1, true, "step_" + type.getId(), "Step " + type.getId(), type))
                .toList();
    }

    private static PrestationTypePrice price(PrestationType type, String amount) {
        PrestationTypePrice price = mock(PrestationTypePrice.class);
        lenient().when(price.getPrestationType()).thenReturn(type);
        lenient().when(price.getPrice()).thenReturn(new BigDecimal(amount));
        return price;
    }

    /** Un precio por prestación, con monto 100 + id: distinto entre prestaciones. */
    private static List<PrestationTypePrice> onePricePerType(PrestationType... types) {
        return Stream.of(types).map(type -> price(type, String.valueOf(100 + type.getId()) + ".00")).toList();
    }

    private static TreatmentPrestationTypeLink link(Treatment treatment, PrestationType type) {
        return new TreatmentPrestationTypeLink(treatment.getId(), type);
    }

    /** Matcher que pide que la colección de ids tenga exactamente estos ids, cada uno una sola vez. */
    private static ArgumentMatcher<Collection<Long>> exactlyIds(Long... expected) {
        return ids -> ids != null && ids.size() == expected.length && new HashSet<>(ids).equals(Set.of(expected));
    }

    /**
     * Lo que devuelven las consultas de catálogo. Las consultas de pares, de scopes y de steps se stubbean solo si el
     * use case las llega a llamar (hay tratamientos / hay prestaciones). Las de scopes devuelven las mismas instancias
     * de PrestationType que las de pares y de ramas, como hace Hibernate dentro de una sesión.
     * Los precios van lenient: con un catálogo vacío el use case puede no leerlos.
     */
    private void stubCatalog(List<Treatment> treatments, List<TreatmentPrestationTypeLink> links,
                             List<PrestationType> withoutTreatment, List<PrestationStep> steps,
                             List<PrestationTypePrice> prices) {
        when(treatmentRepository.findAllBySpecialtyWithConditions(anyLong())).thenReturn(treatments);
        when(prestationTypeRepository.findWithoutTreatmentBySpecialty(anyLong(), any(LocalDate.class))).thenReturn(withoutTreatment);
        if (!treatments.isEmpty()) {
            when(prestationTypeRepository.findTreatmentLinks(anyCollection(), any(LocalDate.class))).thenReturn(links);
        }
        List<PrestationType> allTypes = Stream.concat(
                links.stream().map(TreatmentPrestationTypeLink::prestationType), withoutTreatment.stream()).distinct().toList();
        if (!allTypes.isEmpty()) {
            when(prestationTypeRepository.findAllWithAllowedScopesByIdIn(anyCollection())).thenReturn(allTypes);
            when(prestationStepRepository.findByPrestationIdInFetchStep(anyCollection())).thenReturn(steps);
        }
        lenient().when(prestationTypePriceRepository.findAllPricesAtDate(any(LocalDate.class))).thenReturn(prices);
    }

    private void stubEmptyCatalog() {
        stubCatalog(List.of(), List.of(), List.of(), List.of(), List.of());
    }

    private void verifyNoCatalogReads() {
        verifyNoInteractions(treatmentRepository, prestationTypeRepository, prestationStepRepository, prestationTypePriceRepository);
    }

    // ─── Alta ─────────────────────────────────────────────────────────────────

    /**
     * CASO: una odontopediatra (Person 7, especialidad 4) pide el árbol sin parámetro. Su catálogo tiene el tratamiento
     * "Corona" con "Recementación" dentro, y "Topicación fluor" sin tratamiento.
     * Regla: D-04 y D-01 — sin parámetro el odontólogo es el del usuario (buscado por el id de su Person), los precios
     * son los de hoy y las dos ramas no se mezclan.
     * Validación: getById se llamó con 7L; las consultas reciben la especialidad 4 y la fecha de hoy (pares con el id
     * del tratamiento devuelto); rama 1 = [Corona con Recementación, con todos sus campos, el precio y los dos steps];
     * rama 2 = [Topicación fluor]; ConsultationInstanceQueryService no tuvo interacciones.
     */
    @Test
    void execute_noParam_returnsTreeOfAuthenticatedDentistSpecialty() {
        LocalDate today = LocalDate.now();
        loggedUser(USER_PERSON_ID, DENTIST_ROLE);
        loggedDentistOfSpecialty(DENTIST_SPECIALTY_ID);
        Treatment crown = treatment(10L, "Corona");
        PrestationType recementation = prestationType(21L, "RECEMENTATION", "Recementación", true, 4, false,
                Set.of(PrestationScopeType.TOOTH, PrestationScopeType.QUADRANT));
        PrestationType fluoride = prestationType(22L, "FLUORIDE_TOPICATION", "Topicación fluor", false, null, false, Set.of());
        PrestationStep removal = catalogStep(301L, 1, true, "cement_removal", "Retiro", recementation);
        PrestationStep cementation = catalogStep(302L, 2, false, "cementation", "Cementado", recementation);
        PrestationStep application = catalogStep(303L, 1, true, "application", "Aplicación", fluoride);
        stubCatalog(List.of(crown), List.of(link(crown, recementation)), List.of(fluoride),
                List.of(removal, cementation, application),
                List.of(price(recementation, "15000.50"), price(fluoride, "8000.00")));

        Response<CatalogTreeResponseDTO> response = useCase.execute(null);

        PrestationTypeWithStepsResponseDTO expectedRecementation = new PrestationTypeWithStepsResponseDTO(
                21L, "RECEMENTATION", "Recementación", true, 4, false,
                Set.of(PrestationScopeType.TOOTH, PrestationScopeType.QUADRANT), new BigDecimal("15000.50"),
                List.of(new PrestationStepResponseDTO(301L, "cement_removal", "Retiro", true, 1),
                        new PrestationStepResponseDTO(302L, "cementation", "Cementado", false, 2)));
        PrestationTypeWithStepsResponseDTO expectedFluoride = new PrestationTypeWithStepsResponseDTO(
                22L, "FLUORIDE_TOPICATION", "Topicación fluor", false, null, false, Set.of(), new BigDecimal("8000.00"),
                List.of(new PrestationStepResponseDTO(303L, "application", "Aplicación", true, 1)));
        assertThat(response.data().treatments()).containsExactly(new TreatmentWithPrestationTypeResponseDTO(
                new TreatmentResponseDTO(10L, "Corona", "corona", Set.of(TreatmentConditionType.REQUIRED)),
                List.of(expectedRecementation)));
        assertThat(response.data().prestationTypesWithoutTreatment()).containsExactly(expectedFluoride);
        verify(dentistService).getById(USER_PERSON_ID);
        verify(treatmentRepository).findAllBySpecialtyWithConditions(DENTIST_SPECIALTY_ID);
        verify(prestationTypeRepository).findWithoutTreatmentBySpecialty(DENTIST_SPECIALTY_ID, today);
        verify(prestationTypeRepository).findTreatmentLinks(argThat(exactlyIds(10L)), eq(today));
        verify(prestationTypePriceRepository).findAllPricesAtDate(today);
        verifyNoInteractions(consultationInstanceQueryService);
    }

    /**
     * CASO: una secretaria (Person 7) sin Dentist pide el árbol sin parámetro.
     * Regla: D-04 — sin Dentist responde 409, nunca un árbol vacío.
     * Validación: ConflictException con clave de usuario exception.dentistNotFound.user y clave de log
     * exception.dentistNotFound.log con los argumentos [7L, "GetCatalogTreeUseCase", "execute"]; ninguna consulta de
     * catálogo tuvo interacciones.
     */
    @Test
    void execute_noParam_userWithoutDentist_throwsConflict() {
        loggedUser(USER_PERSON_ID, "SECRETARY");
        when(dentistService.getById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(ConflictException.class)
                .satisfies(ex -> {
                    AppException exception = (AppException) ex;
                    assertThat(exception.getUserMessageKey()).isEqualTo("exception.dentistNotFound.user");
                    assertThat(exception.getLogMessageKey()).isEqualTo("exception.dentistNotFound.log");
                    assertThat(exception.getLogArgs()).containsExactly(7L, USE_CASE, "execute");
                });

        verifyNoCatalogReads();
    }

    // ─── Corrección (con instancia) ───────────────────────────────────────────

    /**
     * CASO: se corrige la instancia 57, creada el 2026-11-09 a las 23:30 en la consulta de una ortodoncista (especialidad 2),
     * y pide un usuario ADMINISTRATOR. El catálogo de Ortodoncia tiene el tratamiento "Corona" con "Radiografía" dentro y
     * una prestación sin tratamiento.
     * Regla: D-04 — con instancia, el odontólogo es el de la consulta y la fecha es el día del createdAt (el 9, no el 10).
     * Validación: las consultas (tratamientos, pares, sin tratamiento y precios) reciben la especialidad 2 y LocalDate
     * 2026-11-09; IDentistService no tuvo interacciones.
     */
    @Test
    void execute_withInstance_usesConsultationDentistAndInstanceDate() {
        loggedUser(1L, ADMIN_ROLE);
        ConsultationInstance instance = instance(57L, LocalDateTime.of(2026, 11, 9, 23, 30), dentistWithSpecialty(2L));
        when(consultationInstanceQueryService.findById(57L)).thenReturn(instance);
        Treatment crown = treatment(10L, "Corona");
        PrestationType xray = prestationType(6L, "Radiografía");
        PrestationType whitening = prestationType(5L, "Blanqueamiento");
        stubCatalog(List.of(crown), List.of(link(crown, xray)), List.of(whitening), oneStepPerType(xray, whitening),
                onePricePerType(xray, whitening));

        useCase.execute(57L);

        LocalDate instanceDay = LocalDate.of(2026, 11, 9);
        verify(treatmentRepository).findAllBySpecialtyWithConditions(2L);
        verify(prestationTypeRepository).findTreatmentLinks(argThat(exactlyIds(10L)), eq(instanceDay));
        verify(prestationTypeRepository).findWithoutTreatmentBySpecialty(2L, instanceDay);
        verify(prestationTypePriceRepository).findAllPricesAtDate(instanceDay);
        verifyNoInteractions(dentistService);
    }

    /**
     * CASO: se pide la instancia 999, que no existe.
     * Regla: D-04 — la instancia tiene que existir.
     * Validación: se propaga la misma NotFoundException del query service (misma instancia); ninguna consulta de
     * catálogo tuvo interacciones.
     */
    @Test
    void execute_withInstance_nonexistentInstance_throwsNotFound() {
        loggedUser(1L, ADMIN_ROLE);
        NotFoundException notFound = new NotFoundException("exception.getConsultationInstanceUseCase.notFound.user", null,
                "exception.getConsultationInstanceUseCase.notFound.log",
                new Object[]{999L, "ConsultationInstanceQueryService", "findById"}, LogLevel.WARN);
        when(consultationInstanceQueryService.findById(999L)).thenThrow(notFound);

        assertThatThrownBy(() -> useCase.execute(999L)).isSameAs(notFound);

        verifyNoCatalogReads();
    }

    /**
     * CASO: un usuario que solo es DENTIST (Person 7) pide la instancia 57 de otro odontólogo.
     * Regla: D-04 y ADR-0027 — una instancia ajena se ve igual que una inexistente: 404.
     * Validación: isAssignedDentist se llamó con la consulta de la instancia y el usuario autenticado, y devolvió false; NotFoundException con
     * clave de usuario exception.getConsultationInstanceUseCase.notFound.user y clave de log
     * exception.getCatalogTreeUseCase.instanceNotOwned.log con los argumentos [57L, 7L, "GetCatalogTreeUseCase",
     * "validateOwnership"]; ninguna consulta de catálogo tuvo interacciones.
     */
    @Test
    void execute_withInstance_onlyDentistRole_otherDentistInstance_throwsNotFound() {
        loggedUser(USER_PERSON_ID, DENTIST_ROLE);
        ConsultationInstance instance = instance(57L, LocalDateTime.of(2026, 11, 9, 10, 0), dentistWithSpecialty(2L));
        Consultation consultation = instance.getConsultation();
        when(consultationInstanceQueryService.findById(57L)).thenReturn(instance);
        when(consultationInstanceDomainService.isAssignedDentist(consultation, authUser)).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(57L))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> {
                    AppException exception = (AppException) ex;
                    assertThat(exception.getUserMessageKey()).isEqualTo("exception.getConsultationInstanceUseCase.notFound.user");
                    assertThat(exception.getLogMessageKey()).isEqualTo("exception.getCatalogTreeUseCase.instanceNotOwned.log");
                    assertThat(exception.getLogArgs()).containsExactly(57L, USER_PERSON_ID, USE_CASE, "validateOwnership");
                });

        verify(consultationInstanceDomainService).isAssignedDentist(consultation, authUser);
        verifyNoCatalogReads();
    }

    /**
     * CASO: un usuario que solo es DENTIST (Person 7) pide su propia instancia 57.
     * Regla: D-04 — el dueño de la instancia puede pedir su árbol.
     * Validación: isAssignedDentist (con la consulta de la instancia y el usuario autenticado) devuelve true; devuelve el árbol y las consultas
     * reciben la especialidad 2 del Dentist de la consulta; no lanza.
     */
    @Test
    void execute_withInstance_onlyDentistRole_ownInstance_returnsTree() {
        loggedUser(USER_PERSON_ID, DENTIST_ROLE);
        ConsultationInstance instance = instance(57L, LocalDateTime.of(2026, 11, 9, 10, 0), dentistWithSpecialty(2L));
        when(consultationInstanceQueryService.findById(57L)).thenReturn(instance);
        when(consultationInstanceDomainService.isAssignedDentist(instance.getConsultation(), authUser)).thenReturn(true);
        stubEmptyCatalog();

        Response<CatalogTreeResponseDTO> response = useCase.execute(57L);

        assertThat(response.data()).isNotNull();
        verify(treatmentRepository).findAllBySpecialtyWithConditions(2L);
    }

    /**
     * CASO: un usuario ADMINISTRATOR (Person 1) pide la instancia de un odontólogo con Person 9 y especialidad 3.
     * Regla: D-04 — el control de dueño solo aplica a quien tiene únicamente el rol DENTIST.
     * Validación: devuelve el árbol con la especialidad 3 del Dentist de la consulta; no lanza; isAssignedDentist no
     * tuvo interacciones.
     */
    @Test
    void execute_withInstance_adminAsksAnyInstance_returnsTree() {
        loggedUser(1L, ADMIN_ROLE);
        ConsultationInstance instance = instance(57L, LocalDateTime.of(2026, 11, 9, 10, 0), dentistWithSpecialty(3L));
        when(consultationInstanceQueryService.findById(57L)).thenReturn(instance);
        stubEmptyCatalog();

        Response<CatalogTreeResponseDTO> response = useCase.execute(57L);

        assertThat(response.data()).isNotNull();
        verify(treatmentRepository).findAllBySpecialtyWithConditions(3L);
        verifyNoInteractions(consultationInstanceDomainService);
    }

    /**
     * CASO: el dueño del consultorio (roles DENTIST y ADMINISTRATOR, Person 7) pide la instancia de otro odontólogo
     * (especialidad 3).
     * Regla: D-04 — el control de dueño se aplica solo si DENTIST es el único rol, no si el usuario tiene DENTIST entre otros.
     * Validación: devuelve el árbol con la especialidad 3; no lanza; isAssignedDentist no tuvo interacciones.
     */
    @Test
    void execute_withInstance_dentistAndAdminRoles_otherDentistInstance_returnsTree() {
        loggedUser(USER_PERSON_ID, DENTIST_ROLE, ADMIN_ROLE);
        ConsultationInstance instance = instance(57L, LocalDateTime.of(2026, 11, 9, 10, 0), dentistWithSpecialty(3L));
        when(consultationInstanceQueryService.findById(57L)).thenReturn(instance);
        stubEmptyCatalog();

        Response<CatalogTreeResponseDTO> response = useCase.execute(57L);

        assertThat(response.data()).isNotNull();
        verify(treatmentRepository).findAllBySpecialtyWithConditions(3L);
        verifyNoInteractions(consultationInstanceDomainService);
    }

    // ─── Precios ──────────────────────────────────────────────────────────────

    /**
     * CASO: "XRAY" (id 4) está bajo "Corona" y ese día solo hay precios de la prestación 99.
     * Regla: D-04 — una prestación del árbol sin precio vigente es un 409, y no se devuelve un árbol parcial.
     * Validación: ConflictException con clave de usuario exception.currentPrice.user y argumentos de usuario [4L],
     * clave de log exception.getCatalogTreeUseCase.priceNotFound.log con los argumentos [4L, hoy, "GetCatalogTreeUseCase",
     * "resolvePrices"].
     */
    @Test
    void execute_prestationWithoutPriceForDate_throwsConflict() {
        LocalDate today = LocalDate.now();
        loggedUser(USER_PERSON_ID, DENTIST_ROLE);
        loggedDentistOfSpecialty(DENTIST_SPECIALTY_ID);
        Treatment crown = treatment(10L, "Corona");
        PrestationType xray = prestationType(4L, "XRAY", "Radiografía", false, null, false, Set.of());
        PrestationType otherPrestation = prestationType(99L, "Otra");
        stubCatalog(List.of(crown), List.of(link(crown, xray)), List.of(), oneStepPerType(xray),
                onePricePerType(otherPrestation));

        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(ConflictException.class)
                .satisfies(ex -> {
                    AppException exception = (AppException) ex;
                    assertThat(exception.getUserMessageKey()).isEqualTo("exception.currentPrice.user");
                    assertThat(exception.getUserArgs()).containsExactly(4L);
                    assertThat(exception.getLogMessageKey()).isEqualTo("exception.getCatalogTreeUseCase.priceNotFound.log");
                    assertThat(exception.getLogArgs()).containsExactly(4L, today, USE_CASE, "resolvePrices");
                });
    }

    /**
     * CASO: "XRAY" (id 4) está bajo "Corona" y ese día tiene dos precios vigentes.
     * Regla: D-04 y D-11 — dos precios vigentes el mismo día es un error de datos: 409, no se elige uno.
     * Validación: ConflictException con clave de usuario exception.getCatalogTreeUseCase.multiplePrices.user y
     * argumentos de usuario [4L], clave de log exception.getCatalogTreeUseCase.multiplePrices.log con los argumentos
     * [4L, hoy, "GetCatalogTreeUseCase", "resolvePrices"].
     */
    @Test
    void execute_prestationWithTwoPricesForDate_throwsConflict() {
        LocalDate today = LocalDate.now();
        loggedUser(USER_PERSON_ID, DENTIST_ROLE);
        loggedDentistOfSpecialty(DENTIST_SPECIALTY_ID);
        Treatment crown = treatment(10L, "Corona");
        PrestationType xray = prestationType(4L, "XRAY", "Radiografía", false, null, false, Set.of());
        stubCatalog(List.of(crown), List.of(link(crown, xray)), List.of(), oneStepPerType(xray),
                List.of(price(xray, "1000.00"), price(xray, "1200.00")));

        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(ConflictException.class)
                .satisfies(ex -> {
                    AppException exception = (AppException) ex;
                    assertThat(exception.getUserMessageKey()).isEqualTo("exception.getCatalogTreeUseCase.multiplePrices.user");
                    assertThat(exception.getUserArgs()).containsExactly(4L);
                    assertThat(exception.getLogMessageKey()).isEqualTo("exception.getCatalogTreeUseCase.multiplePrices.log");
                    assertThat(exception.getLogArgs()).containsExactly(4L, today, USE_CASE, "resolvePrices");
                });
    }

    /**
     * CASO: el árbol tiene "XRAY" (id 4) con un solo precio, y la consulta de precios devuelve además dos precios de la
     * prestación 99, que no está en ninguna rama.
     * Regla: D-04 — el 409 por más de un precio se mira solo sobre las prestaciones del árbol del odontólogo.
     * Validación: devuelve el árbol sin lanzar; el precio de XRAY es 104.00 y no aparece la prestación 99.
     */
    @Test
    void execute_pricesOfPrestationsOutsideTree_areIgnored() {
        loggedUser(USER_PERSON_ID, DENTIST_ROLE);
        loggedDentistOfSpecialty(DENTIST_SPECIALTY_ID);
        Treatment crown = treatment(10L, "Corona");
        PrestationType xray = prestationType(4L, "XRAY", "Radiografía", false, null, false, Set.of());
        PrestationType outside = prestationType(99L, "Otra");
        stubCatalog(List.of(crown), List.of(link(crown, xray)), List.of(), oneStepPerType(xray),
                List.of(price(xray, "104.00"), price(outside, "1000.00"), price(outside, "2000.00")));

        Response<CatalogTreeResponseDTO> response = useCase.execute(null);

        assertThat(response.data().treatments()).hasSize(1);
        assertThat(response.data().treatments().get(0).prestationTypes())
                .extracting(PrestationTypeWithStepsResponseDTO::id, PrestationTypeWithStepsResponseDTO::price)
                .containsExactly(tuple(4L, new BigDecimal("104.00")));
        assertThat(response.data().prestationTypesWithoutTreatment()).isEmpty();
    }

    /**
     * CASO: la prestación 17 "Alfa" (bajo "Corona") y la 5 "Zeta" (sin tratamiento) están en el árbol y ninguna tiene precio.
     * Los ids 5 y 17 caen en un HashSet en orden inverso al numérico (17 en el bucket 1, 5 en el 5), el label pone primera
     * a la 17 y la rama de tratamientos se recorre antes: solo ordenar por id da la 5.
     * Regla: D-04 (ampliación 3) — las prestaciones se revisan en orden de id y el error nombra la de menor id.
     * Validación: ConflictException con clave de usuario exception.currentPrice.user y argumentos de usuario [5L].
     */
    @Test
    void execute_twoPrestationsWithoutPrice_reportsLowestId() {
        loggedUser(USER_PERSON_ID, DENTIST_ROLE);
        loggedDentistOfSpecialty(DENTIST_SPECIALTY_ID);
        Treatment crown = treatment(10L, "Corona");
        PrestationType underCrown = prestationType(17L, "Alfa");
        PrestationType withoutTreatment = prestationType(5L, "Zeta");
        stubCatalog(List.of(crown), List.of(link(crown, underCrown)), List.of(withoutTreatment),
                oneStepPerType(underCrown, withoutTreatment), List.of());

        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(ConflictException.class)
                .satisfies(ex -> {
                    AppException exception = (AppException) ex;
                    assertThat(exception.getUserMessageKey()).isEqualTo("exception.currentPrice.user");
                    assertThat(exception.getUserArgs()).containsExactly(5L);
                });
    }

    // ─── Armado del árbol ─────────────────────────────────────────────────────

    /**
     * CASO: "XRAY" (id 50) cuelga de "Corona" (id 4) y de "Puente" (id 5).
     * Regla: D-03 — la prestación se repite bajo cada tratamiento del que cuelga, con el mismo precio y los mismos steps.
     * Validación: los dos tratamientos tienen una única prestación con id 50, precio 150.00 y los mismos dos steps
     * (ids 9050 y 9051); las dos prestaciones son iguales por contenido.
     */
    @Test
    void execute_prestationWithTwoTreatments_appearsUnderBoth() {
        loggedUser(USER_PERSON_ID, DENTIST_ROLE);
        loggedDentistOfSpecialty(DENTIST_SPECIALTY_ID);
        Treatment crown = treatment(4L, "Corona");
        Treatment bridge = treatment(5L, "Puente");
        PrestationType xray = prestationType(50L, "XRAY", "Radiografía", false, null, false, Set.of());
        PrestationStep first = catalogStep(9050L, 1, true, "shot", "Toma", xray);
        PrestationStep second = catalogStep(9051L, 2, false, "report", "Informe", xray);
        stubCatalog(List.of(crown, bridge), List.of(link(crown, xray), link(bridge, xray)), List.of(),
                List.of(first, second), List.of(price(xray, "150.00")));

        Response<CatalogTreeResponseDTO> response = useCase.execute(null);

        List<TreatmentWithPrestationTypeResponseDTO> treatments = response.data().treatments();
        assertThat(treatments).extracting(branch -> branch.treatment().id()).containsExactly(4L, 5L);
        for (TreatmentWithPrestationTypeResponseDTO branch : treatments) {
            assertThat(branch.prestationTypes()).hasSize(1);
            PrestationTypeWithStepsResponseDTO prestation = branch.prestationTypes().get(0);
            assertThat(prestation.id()).isEqualTo(50L);
            assertThat(prestation.price()).isEqualTo(new BigDecimal("150.00"));
            assertThat(prestation.steps()).extracting(PrestationStepResponseDTO::id).containsExactly(9050L, 9051L);
        }
        assertThat(treatments.get(0).prestationTypes()).isEqualTo(treatments.get(1).prestationTypes());
    }

    /**
     * CASO: "Puente" (id 5) es un tratamiento del odontólogo sin ninguna prestación.
     * Regla: D-07 — un tratamiento sin prestaciones se devuelve igual, con la lista vacía.
     * Validación: treatments tiene un elemento con treatment.id 5 y prestationTypes vacía (no null); no lanza;
     * findAllWithAllowedScopesByIdIn y findByPrestationIdInFetchStep nunca se llamaron (no hay ids que consultar).
     */
    @Test
    void execute_treatmentWithoutPrestations_returnedWithEmptyList() {
        loggedUser(USER_PERSON_ID, DENTIST_ROLE);
        loggedDentistOfSpecialty(DENTIST_SPECIALTY_ID);
        Treatment bridge = treatment(5L, "Puente");
        stubCatalog(List.of(bridge), List.of(), List.of(), List.of(), List.of());

        Response<CatalogTreeResponseDTO> response = useCase.execute(null);

        assertThat(response.data().treatments()).hasSize(1);
        assertThat(response.data().treatments().get(0).treatment().id()).isEqualTo(5L);
        assertThat(response.data().treatments().get(0).prestationTypes()).isNotNull().isEmpty();
        verify(prestationTypeRepository, never()).findAllWithAllowedScopesByIdIn(anyCollection());
        verify(prestationStepRepository, never()).findByPrestationIdInFetchStep(anyCollection());
    }

    /**
     * CASO: las consultas devuelven todo desordenado. Tratamientos "Gamma" (1), "Beta" (9), "Alpha" (5) y "Beta" (8);
     * bajo "Alpha": "Zeta" (60), "Beta" (62), "Beta" (61) y "Alpha" (63), esta última con steps en position 3, 1, 2;
     * sin tratamiento: "Mu" (71), "Mu" (70) y "Ab" (72).
     * Los name se arman al revés del orden esperado (N + 9999 menos el id, o sea, descendente por id), así que ordenar por
     * name y no por label da otro resultado.
     * Regla: D-05 — tratamientos y prestaciones van por label ascendente y, si empatan, por id; los steps, por position.
     * Validación: treatments sale con ids [5, 8, 9, 1]; las prestaciones bajo "Alpha" con ids [63, 61, 62, 60]; la rama 2
     * con ids [72, 70, 71]; los steps de la 63 con position [1, 2, 3].
     */
    @Test
    void execute_listsAreOrdered() {
        loggedUser(USER_PERSON_ID, DENTIST_ROLE);
        loggedDentistOfSpecialty(DENTIST_SPECIALTY_ID);
        Treatment gamma = treatment(1L, "Gamma", invertedName(1L));
        Treatment betaNine = treatment(9L, "Beta", invertedName(9L));
        Treatment alpha = treatment(5L, "Alpha", invertedName(5L));
        Treatment betaEight = treatment(8L, "Beta", invertedName(8L));
        PrestationType zeta = unorderedByName(60L, "Zeta");
        PrestationType betaSixtyTwo = unorderedByName(62L, "Beta");
        PrestationType betaSixtyOne = unorderedByName(61L, "Beta");
        PrestationType alphaPrestation = unorderedByName(63L, "Alpha");
        PrestationType muSeventyOne = unorderedByName(71L, "Mu");
        PrestationType muSeventy = unorderedByName(70L, "Mu");
        PrestationType ab = unorderedByName(72L, "Ab");
        List<PrestationStep> steps = new ArrayList<>(oneStepPerType(zeta, betaSixtyTwo, betaSixtyOne,
                muSeventyOne, muSeventy, ab));
        steps.add(catalogStep(631L, 3, true, "third", "Tercero", alphaPrestation));
        steps.add(catalogStep(632L, 1, true, "first", "Primero", alphaPrestation));
        steps.add(catalogStep(633L, 2, true, "second", "Segundo", alphaPrestation));
        stubCatalog(List.of(gamma, betaNine, alpha, betaEight),
                List.of(link(alpha, zeta), link(alpha, betaSixtyTwo), link(alpha, betaSixtyOne), link(alpha, alphaPrestation)),
                List.of(muSeventyOne, muSeventy, ab), steps,
                onePricePerType(zeta, betaSixtyTwo, betaSixtyOne, alphaPrestation, muSeventyOne, muSeventy, ab));

        Response<CatalogTreeResponseDTO> response = useCase.execute(null);

        List<TreatmentWithPrestationTypeResponseDTO> treatments = response.data().treatments();
        assertThat(treatments).extracting(branch -> branch.treatment().id()).containsExactly(5L, 8L, 9L, 1L);
        List<PrestationTypeWithStepsResponseDTO> underAlpha = treatments.get(0).prestationTypes();
        assertThat(underAlpha).extracting(PrestationTypeWithStepsResponseDTO::id).containsExactly(63L, 61L, 62L, 60L);
        assertThat(underAlpha.get(0).steps()).extracting(PrestationStepResponseDTO::position).containsExactly(1, 2, 3);
        assertThat(response.data().prestationTypesWithoutTreatment())
                .extracting(PrestationTypeWithStepsResponseDTO::id).containsExactly(72L, 70L, 71L);
    }

    /**
     * CASO: "XRAY" (id 4) cuelga de "Corona" y de "Puente", y "Topicación fluor" (id 5) no tiene tratamiento.
     * Regla: T-02 — las consultas de scopes y de steps se hacen una vez, con los ids de las prestaciones de las dos
     * ramas, cada uno una sola vez.
     * Validación: findAllWithAllowedScopesByIdIn y findByPrestationIdInFetchStep se llamaron exactamente una vez con
     * una colección que contiene exactamente 4 y 5, sin repetidos.
     */
    @Test
    void execute_batchQueriesReceiveDistinctPrestationIds() {
        loggedUser(USER_PERSON_ID, DENTIST_ROLE);
        loggedDentistOfSpecialty(DENTIST_SPECIALTY_ID);
        Treatment crown = treatment(10L, "Corona");
        Treatment bridge = treatment(11L, "Puente");
        PrestationType xray = prestationType(4L, "Radiografía");
        PrestationType fluoride = prestationType(5L, "Topicación fluor");
        stubCatalog(List.of(crown, bridge), List.of(link(crown, xray), link(bridge, xray)), List.of(fluoride),
                oneStepPerType(xray, fluoride), onePricePerType(xray, fluoride));

        useCase.execute(null);

        verify(prestationTypeRepository, times(1)).findAllWithAllowedScopesByIdIn(argThat(exactlyIds(4L, 5L)));
        verify(prestationStepRepository, times(1)).findByPrestationIdInFetchStep(argThat(exactlyIds(4L, 5L)));
    }

    /**
     * CASO: la especialidad del odontólogo no tiene tratamientos ni prestaciones sin tratamiento.
     * Regla: T-02 — con el catálogo vacío no se hacen las consultas por lote (un {@code IN ()} vacío falla en la base).
     * Validación: treatments y prestationTypesWithoutTreatment son listas vacías (no null); findTreatmentLinks,
     * findAllWithAllowedScopesByIdIn y findByPrestationIdInFetchStep nunca se llamaron.
     */
    @Test
    void execute_specialtyWithoutCatalog_returnsEmptyTreeWithoutBatchQueries() {
        loggedUser(USER_PERSON_ID, DENTIST_ROLE);
        loggedDentistOfSpecialty(DENTIST_SPECIALTY_ID);
        stubEmptyCatalog();

        Response<CatalogTreeResponseDTO> response = useCase.execute(null);

        assertThat(response.data().treatments()).isNotNull().isEmpty();
        assertThat(response.data().prestationTypesWithoutTreatment()).isNotNull().isEmpty();
        verify(prestationTypeRepository, never()).findTreatmentLinks(anyCollection(), any(LocalDate.class));
        verify(prestationTypeRepository, never()).findAllWithAllowedScopesByIdIn(anyCollection());
        verify(prestationStepRepository, never()).findByPrestationIdInFetchStep(anyCollection());
    }
}