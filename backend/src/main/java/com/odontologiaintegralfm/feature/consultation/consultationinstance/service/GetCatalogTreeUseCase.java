package com.odontologiaintegralfm.feature.consultation.consultationinstance.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.authentication.enums.Role;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.dto.CatalogTreeResponseDTO;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.dto.TreatmentWithPrestationTypeResponseDTO;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.prestation.dto.PrestationStepResponseDTO;
import com.odontologiaintegralfm.feature.consultation.prestation.dto.PrestationTypeWithStepsResponseDTO;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationTypePrice;
import com.odontologiaintegralfm.feature.consultation.prestation.repository.IPrestationStepRepository;
import com.odontologiaintegralfm.feature.consultation.prestation.repository.IPrestationTypePriceRepository;
import com.odontologiaintegralfm.feature.consultation.prestation.repository.IPrestationTypeRepository;
import com.odontologiaintegralfm.feature.consultation.prestation.repository.TreatmentPrestationTypeLink;
import com.odontologiaintegralfm.feature.consultation.treatment.mapper.TreatmentMapper;
import com.odontologiaintegralfm.feature.consultation.treatment.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.treatment.repository.ITreatmentRepository;
import com.odontologiaintegralfm.feature.dentist.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.service.IDentistService;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Devuelve el catálogo clínico de la especialidad del odontólogo en árbol (REQ-039): tratamientos con sus prestaciones
 * y prestaciones sin tratamiento, con steps y precio. Sin parámetro es un alta (usuario autenticado, precios de hoy);
 * con la instancia es una corrección (odontólogo de su consulta, prestaciones y precios a la fecha de la instancia).
 * La cantidad de consultas no depende del tamaño del catálogo (T-02). No persiste nada.
 * El detalle de cada paso está en el § 12 del design.
 */
@Service
public class GetCatalogTreeUseCase {

    private static final String CLASS_NAME = "GetCatalogTreeUseCase";

    private static final Comparator<PrestationTypeWithStepsResponseDTO> PRESTATION_ORDER =
            Comparator.comparing(PrestationTypeWithStepsResponseDTO::label).thenComparing(PrestationTypeWithStepsResponseDTO::id);
    private static final Comparator<TreatmentWithPrestationTypeResponseDTO> TREATMENT_ORDER =
            Comparator.comparing((TreatmentWithPrestationTypeResponseDTO branch) -> branch.treatment().label())
                    .thenComparing(branch -> branch.treatment().id());

    private final AuthenticatedUserService authenticatedUserService;
    private final IDentistService dentistService;
    private final ConsultationInstanceQueryService consultationInstanceQueryService;
    private final ConsultationInstanceDomainService consultationInstanceDomainService;
    private final ITreatmentRepository treatmentRepository;
    private final IPrestationTypeRepository prestationTypeRepository;
    private final IPrestationStepRepository prestationStepRepository;
    private final IPrestationTypePriceRepository prestationTypePriceRepository;
    private final TreatmentMapper treatmentMapper;

    public GetCatalogTreeUseCase(AuthenticatedUserService authenticatedUserService,
                                 IDentistService dentistService,
                                 ConsultationInstanceQueryService consultationInstanceQueryService,
                                 ConsultationInstanceDomainService consultationInstanceDomainService,
                                 ITreatmentRepository treatmentRepository,
                                 IPrestationTypeRepository prestationTypeRepository,
                                 IPrestationStepRepository prestationStepRepository,
                                 IPrestationTypePriceRepository prestationTypePriceRepository,
                                 TreatmentMapper treatmentMapper) {
        this.authenticatedUserService = authenticatedUserService;
        this.dentistService = dentistService;
        this.consultationInstanceQueryService = consultationInstanceQueryService;
        this.consultationInstanceDomainService = consultationInstanceDomainService;
        this.treatmentRepository = treatmentRepository;
        this.prestationTypeRepository = prestationTypeRepository;
        this.prestationStepRepository = prestationStepRepository;
        this.prestationTypePriceRepository = prestationTypePriceRepository;
        this.treatmentMapper = treatmentMapper;
    }

    @Transactional(readOnly = true)
    public Response<CatalogTreeResponseDTO> execute(Long consultationInstanceId) {

        // --- 1. Validar y resolver el odontólogo y la fecha ---
        UserSec user = authenticatedUserService.getAuthenticatedUser();
        Dentist dentist;
        LocalDate date;
        if (consultationInstanceId == null) {
            Long personId = user.getPerson().getId();
            dentist = dentistService.getById(personId).orElseThrow(() -> new ConflictException(
                    "exception.dentistNotFound.user", null,
                    "exception.dentistNotFound.log", new Object[]{personId, CLASS_NAME, "execute"}, LogLevel.ERROR));
            date = LocalDate.now();
        } else {
            ConsultationInstance instance = consultationInstanceQueryService.findById(consultationInstanceId);
            validateOwnership(instance, user);
            dentist = instance.getConsultation().getDentist();
            date = instance.getCreatedAt().toLocalDate();
        }
        Long specialtyId = dentist.getDentistSpecialty().getId();

        // --- 2. Leer el catálogo del odontólogo ---
        List<Treatment> treatments = treatmentRepository.findAllBySpecialtyWithConditions(specialtyId);
        List<TreatmentPrestationTypeLink> links = treatments.isEmpty()
                ? List.of()
                : prestationTypeRepository.findTreatmentLinks(treatments.stream().map(Treatment::getId).toList(), date);
        List<PrestationType> withoutTreatment = prestationTypeRepository.findWithoutTreatmentBySpecialty(specialtyId, date);

        Set<Long> prestationIds = new TreeSet<>();
        links.forEach(link -> prestationIds.add(link.prestationType().getId()));
        withoutTreatment.forEach(type -> prestationIds.add(type.getId()));

        Map<Long, List<PrestationStep>> stepsByPrestation = Map.of();
        if (!prestationIds.isEmpty()) {
            // Los allowedScopes se cargan sobre las mismas instancias de PrestationType que ya leyeron las consultas de arriba (misma sesión).
            prestationTypeRepository.findAllWithAllowedScopesByIdIn(prestationIds);
            stepsByPrestation = prestationStepRepository.findByPrestationIdInFetchStep(prestationIds).stream()
                    .collect(Collectors.groupingBy(step -> step.getPrestation().getId()));
        }

        // --- 3. Validar los precios y armar la respuesta ---
        Map<Long, BigDecimal> pricesByPrestation = resolvePrices(prestationIds, date);
        CatalogTreeResponseDTO tree = buildTree(treatments, links, withoutTreatment, stepsByPrestation, pricesByPrestation);

        return new Response<>(true, null, tree);
    }

    /**
     * Quien solo es {@code DENTIST} únicamente puede pedir una instancia suya; una ajena se ve igual que una inexistente
     * (404, ADR-0027). Quien tiene otro rol, aunque además sea {@code DENTIST}, puede pedir cualquiera (D-04).
     */
    private void validateOwnership(ConsultationInstance instance, UserSec user) {
        boolean onlyDentist = user.getRolesList().stream().allMatch(role -> Role.DENTIST.name().equals(role.getName()));
        if (onlyDentist && !consultationInstanceDomainService.isAssignedDentist(instance.getConsultation(), user)) {
            throw new NotFoundException(
                    "exception.getConsultationInstanceUseCase.notFound.user", null,
                    "exception.getCatalogTreeUseCase.instanceNotOwned.log",
                    new Object[]{instance.getId(), user.getPerson().getId(), CLASS_NAME, "validateOwnership"}, LogLevel.WARN);
        }
    }

    /**
     * Lee los precios vigentes a {@code date} y deja uno por prestación del árbol. Recorre las prestaciones en orden de
     * {@code id} y lanza el 409 con la primera que no tiene precio o tiene más de uno; los precios de prestaciones fuera
     * del árbol no se miran (D-04, D-11).
     */
    private Map<Long, BigDecimal> resolvePrices(Collection<Long> prestationTypeIds, LocalDate date) {
        Map<Long, List<BigDecimal>> pricesByType = prestationTypePriceRepository.findAllPricesAtDate(date).stream()
                .collect(Collectors.groupingBy(
                        (PrestationTypePrice price) -> price.getPrestationType().getId(),
                        Collectors.mapping(PrestationTypePrice::getPrice, Collectors.toList())));

        Map<Long, BigDecimal> resolved = new HashMap<>();
        for (Long id : new TreeSet<>(prestationTypeIds)) {
            List<BigDecimal> prices = pricesByType.get(id);
            if (prices == null || prices.isEmpty()) {
                throw new ConflictException(
                        "exception.currentPrice.user", new Object[]{id},
                        "exception.getCatalogTreeUseCase.priceNotFound.log", new Object[]{id, date, CLASS_NAME, "resolvePrices"}, LogLevel.ERROR);
            }
            if (prices.size() > 1) {
                throw new ConflictException(
                        "exception.getCatalogTreeUseCase.multiplePrices.user", new Object[]{id},
                        "exception.getCatalogTreeUseCase.multiplePrices.log", new Object[]{id, date, CLASS_NAME, "resolvePrices"}, LogLevel.ERROR);
            }
            resolved.put(id, prices.get(0));
        }
        return resolved;
    }

    /**
     * Arma las dos ramas: la prestación se repite bajo cada tratamiento del que cuelga, con el mismo precio y los mismos
     * steps (D-03). Tratamientos y prestaciones van por {@code label} y por {@code id}; los steps, por {@code position} (D-05).
     */
    private CatalogTreeResponseDTO buildTree(List<Treatment> treatments,
                                             List<TreatmentPrestationTypeLink> links,
                                             List<PrestationType> withoutTreatment,
                                             Map<Long, List<PrestationStep>> stepsByPrestation,
                                             Map<Long, BigDecimal> pricesByPrestation) {
        Map<Long, PrestationTypeWithStepsResponseDTO> prestationDtos = new HashMap<>();
        Map<Long, List<PrestationTypeWithStepsResponseDTO>> prestationsByTreatment = new LinkedHashMap<>();
        for (TreatmentPrestationTypeLink link : links) {
            PrestationTypeWithStepsResponseDTO dto = prestationDtos.computeIfAbsent(link.prestationType().getId(),
                    id -> toPrestationDto(link.prestationType(), stepsByPrestation, pricesByPrestation));
            prestationsByTreatment.computeIfAbsent(link.treatmentId(), id -> new ArrayList<>()).add(dto);
        }

        List<TreatmentWithPrestationTypeResponseDTO> treatmentBranch = treatments.stream()
                .map(treatment -> new TreatmentWithPrestationTypeResponseDTO(
                        treatmentMapper.toDTO(treatment),
                        prestationsByTreatment.getOrDefault(treatment.getId(), List.of()).stream().sorted(PRESTATION_ORDER).toList()))
                .sorted(TREATMENT_ORDER)
                .toList();

        List<PrestationTypeWithStepsResponseDTO> prestationBranch = withoutTreatment.stream()
                .map(type -> prestationDtos.computeIfAbsent(type.getId(), id -> toPrestationDto(type, stepsByPrestation, pricesByPrestation)))
                .sorted(PRESTATION_ORDER)
                .toList();

        return new CatalogTreeResponseDTO(treatmentBranch, prestationBranch);
    }

    private PrestationTypeWithStepsResponseDTO toPrestationDto(PrestationType type,
                                                               Map<Long, List<PrestationStep>> stepsByPrestation,
                                                               Map<Long, BigDecimal> pricesByPrestation) {
        List<PrestationStepResponseDTO> steps = stepsByPrestation.getOrDefault(type.getId(), List.of()).stream()
                .sorted(Comparator.comparingInt(PrestationStep::getPosition))
                .map(step -> new PrestationStepResponseDTO(step.getId(), step.getStep().getName(), step.getStep().getLabel(), step.isRequired(), step.getPosition()))
                .toList();
        return new PrestationTypeWithStepsResponseDTO(
                type.getId(), type.getName(), type.getLabel(), type.isUnique(), type.getMaxTooth(), type.isRequiresOriginTreatment(),
                type.getAllowedScopes() == null ? Set.of() : Set.copyOf(type.getAllowedScopes()),
                pricesByPrestation.get(type.getId()), steps);
    }
}