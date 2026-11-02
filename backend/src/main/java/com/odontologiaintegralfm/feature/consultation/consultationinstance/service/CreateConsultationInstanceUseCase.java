package com.odontologiaintegralfm.feature.consultation.consultationinstance.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.prestation.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.prestation.service.PrestationStepService;
import com.odontologiaintegralfm.feature.consultation.prestation.service.PrestationTypePriceService;
import com.odontologiaintegralfm.feature.consultation.prestation.service.PrestationTypeService;
import com.odontologiaintegralfm.feature.consultation.promotion.model.Promotion;
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
import com.odontologiaintegralfm.feature.consultation.scope.mapper.ScopeMapper;
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
import com.odontologiaintegralfm.feature.patient.model.Patient;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Create de {@link ConsultationInstance} (REQ-033): una sola transacción que valida la consulta, procesa los
 * tratamientos y prestaciones del request (existentes que avanzan y nuevos que nacen), deriva los estados
 * (steps → prestación → tratamiento) y recién al final, con todo persistido y flusheado, corre las validaciones
 * cruzadas (ADR-0033). El detalle de cada paso está en el § 7 del design.
 */
@Service
public class CreateConsultationInstanceUseCase {

    private final AuthenticatedUserService authenticatedUserService;
    private final ConsultationQueryService consultationQueryService;
    private final IConsultationInstanceRepository consultationInstanceRepository;
    private final ITreatmentInstanceRepository treatmentInstanceRepository;
    private final ITreatmentInstanceHistoryRepository treatmentInstanceHistoryRepository;
    private final TreatmentInstanceQueryService treatmentInstanceQueryService;
    private final TreatmentInstanceDomainService treatmentInstanceDomainService;
    private final IPrestationInstanceRepository prestationInstanceRepository;
    private final PrestationInstanceQueryService prestationInstanceQueryService;
    private final PrestationInstanceDomainService prestationInstanceDomainService;
    private final IPrestationStepInstanceRepository prestationStepInstanceRepository;
    private final PrestationStepInstanceQueryService prestationStepInstanceQueryService;
    private final IPrestationStepInstanceHistoryRepository prestationStepInstanceHistoryRepository;
    private final PrestationStepService prestationStepService;
    private final PrestationTypeService prestationTypeService;
    private final PrestationTypePriceService prestationTypePriceService;
    private final TreatmentService treatmentService;
    private final PromotionQueryService promotionQueryService;
    private final ScopeDomainService scopeDomainService;
    private final ScopeMapper scopeMapper;
    private final DentistSpecialtyDomainService dentistSpecialtyDomainService;
    private final ChangeConsultationStatusUseCase changeConsultationStatusUseCase;
    private final MessageSource messageSource;

    public CreateConsultationInstanceUseCase(AuthenticatedUserService authenticatedUserService,
                                             ConsultationQueryService consultationQueryService,
                                             IConsultationInstanceRepository consultationInstanceRepository,
                                             ITreatmentInstanceRepository treatmentInstanceRepository,
                                             ITreatmentInstanceHistoryRepository treatmentInstanceHistoryRepository,
                                             TreatmentInstanceQueryService treatmentInstanceQueryService,
                                             TreatmentInstanceDomainService treatmentInstanceDomainService,
                                             IPrestationInstanceRepository prestationInstanceRepository,
                                             PrestationInstanceQueryService prestationInstanceQueryService,
                                             PrestationInstanceDomainService prestationInstanceDomainService,
                                             IPrestationStepInstanceRepository prestationStepInstanceRepository,
                                             PrestationStepInstanceQueryService prestationStepInstanceQueryService,
                                             IPrestationStepInstanceHistoryRepository prestationStepInstanceHistoryRepository,
                                             PrestationStepService prestationStepService,
                                             PrestationTypeService prestationTypeService,
                                             PrestationTypePriceService prestationTypePriceService,
                                             TreatmentService treatmentService,
                                             PromotionQueryService promotionQueryService,
                                             ScopeDomainService scopeDomainService,
                                             ScopeMapper scopeMapper,
                                             DentistSpecialtyDomainService dentistSpecialtyDomainService,
                                             ChangeConsultationStatusUseCase changeConsultationStatusUseCase,
                                             MessageSource messageSource) {
        this.authenticatedUserService = authenticatedUserService;
        this.consultationQueryService = consultationQueryService;
        this.consultationInstanceRepository = consultationInstanceRepository;
        this.treatmentInstanceRepository = treatmentInstanceRepository;
        this.treatmentInstanceHistoryRepository = treatmentInstanceHistoryRepository;
        this.treatmentInstanceQueryService = treatmentInstanceQueryService;
        this.treatmentInstanceDomainService = treatmentInstanceDomainService;
        this.prestationInstanceRepository = prestationInstanceRepository;
        this.prestationInstanceQueryService = prestationInstanceQueryService;
        this.prestationInstanceDomainService = prestationInstanceDomainService;
        this.prestationStepInstanceRepository = prestationStepInstanceRepository;
        this.prestationStepInstanceQueryService = prestationStepInstanceQueryService;
        this.prestationStepInstanceHistoryRepository = prestationStepInstanceHistoryRepository;
        this.prestationStepService = prestationStepService;
        this.prestationTypeService = prestationTypeService;
        this.prestationTypePriceService = prestationTypePriceService;
        this.treatmentService = treatmentService;
        this.promotionQueryService = promotionQueryService;
        this.scopeDomainService = scopeDomainService;
        this.scopeMapper = scopeMapper;
        this.dentistSpecialtyDomainService = dentistSpecialtyDomainService;
        this.changeConsultationStatusUseCase = changeConsultationStatusUseCase;
        this.messageSource = messageSource;
    }

    @LogAction(
            value = "createConsultationInstanceUseCase.logAction.create.ok",
            args = {"#result.data"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    @Transactional
    public Response<Long> execute(ConsultationInstanceCreateRequestDTO dto) {

        // --- 1. Validar la consulta y su dueño ---
        Consultation consultation = consultationQueryService.findById(dto.consultationId());
        validateOwnership(consultation, authenticatedUserService.getAuthenticatedUser());

        if (!ConsultationStatusType.IN_CONSULTATION.equals(consultation.getStatus())) {
            throw new ConflictException("exception.createConsultationInstanceUseCase.statusConflict.user", null, "exception.createConsultationInstanceUseCase.statusConflict.log", new Object[]{consultation.getId(), consultation.getStatus(), "createConsultationInstanceUseCase", "execute"}, LogLevel.ERROR);
        }
        ConsultationInstance previous = consultationInstanceRepository.findByConsultationId(dto.consultationId());
        if (previous != null) {
            throw new ConflictException("exception.createConsultationInstanceUseCase.existingInstance.user", null, "exception.createConsultationInstanceUseCase.existingInstance.log", new Object[]{previous.getId(), dto.consultationId(), "createConsultationInstanceUseCase", "execute"}, LogLevel.ERROR);
        }
        Patient patient = consultation.getPatient();
        Long patientId = patient.getId();
        Dentist dentist = consultation.getDentist();

        List<TreatmentInstanceItemDTO> treatmentItems = dto.treatmentInstance() == null ? List.of() : dto.treatmentInstance();
        List<PrestationInstanceItemDTO> prestationItems = dto.prestation() == null ? List.of() : dto.prestation();

        // --- 2. Buscar lo que el request referencia y validar unicidad ---
        List<TreatmentToClose> treatmentsToClose = new ArrayList<>();
        for (TreatmentInstanceItemDTO item : treatmentItems) {
            if (item.id() != null) {
                TreatmentInstance existing = treatmentInstanceQueryService.findByIdAndPatientId(item.id(), patientId);
                treatmentInstanceDomainService.validateExplicitCloseAllowed(existing);
                treatmentsToClose.add(new TreatmentToClose(existing, item.treatmentCondition()));
            }
        }
        List<ExistingPrestation> existingPrestations = new ArrayList<>();
        List<NewPrestation> newPrestationItems = new ArrayList<>();
        List<PrestationType> prestationTypes = new ArrayList<>();
        for (PrestationInstanceItemDTO item : prestationItems) {
            if (item.id() != null) {
                PrestationInstance existing = prestationInstanceQueryService.findByIdAndPatientId(item.id(), patientId);
                existingPrestations.add(new ExistingPrestation(item, existing));
                prestationTypes.add(existing.getType());
            } else {
                PrestationType type = prestationTypeService.findById(item.prestationTypeId());
                newPrestationItems.add(new NewPrestation(item, type));
                prestationTypes.add(type);
            }
        }
        prestationInstanceDomainService.validatePrestationUnique(prestationTypes);

        // --- 3. Crear la ConsultationInstance ---
        ConsultationInstance consultationInstance = consultationInstanceRepository.save(ConsultationInstance.build(consultation, dto.observation()));

        // --- 4. Cerrar los TreatmentInstance existentes que se reemplazan o cancelan ---
        List<TreatmentInstance> treatmentInstancesReplacedExplicit = new ArrayList<>();
        for (TreatmentToClose toClose : treatmentsToClose) {
            toClose.treatmentInstance().applyCondition(toClose.condition());
            treatmentInstanceRepository.save(toClose.treatmentInstance());
            treatmentInstanceHistoryRepository.save(TreatmentInstanceHistory.build(toClose.treatmentInstance(), consultationInstance, toClose.condition()));
            if (toClose.condition() == TreatmentConditionType.REPLACED) {
                treatmentInstancesReplacedExplicit.add(toClose.treatmentInstance());
            }
        }

        // --- 5. Crear los TreatmentInstance nuevos ---
        List<TreatmentInstance> treatmentInstancesNuevos = new ArrayList<>();
        Map<String, TreatmentInstance> treatmentInstancesByTempId = new HashMap<>();
        for (TreatmentInstanceItemDTO item : treatmentItems) {
            if (item.id() == null) {
                TreatmentInstance created = createTreatmentInstance(item, patient, dentist, consultationInstance);
                treatmentInstancesNuevos.add(created);
                if (item.tempId() != null) {
                    treatmentInstancesByTempId.put(item.tempId(), created);
                }
            }
        }

        // Tratamientos tocados por las prestaciones (pasos 6 y 7), para derivar su condición en el paso 8.
        Map<Long, TreatmentInstance> touchedTreatmentInstances = new LinkedHashMap<>();
        List<PrestationInstance> prestationInstancesReplacedByTreatment = new ArrayList<>();
        List<PrestationInstance> prestationInstancesReplacedWithoutTreatment = new ArrayList<>();

        // --- 6. Avanzar las PrestationInstance existentes ---
        for (ExistingPrestation existing : existingPrestations) {
            PrestationInstance prestation = advanceExistingPrestation(existing, consultationInstance);
            TreatmentInstance treatmentInstance = prestation.getTreatmentInstance();
            if (treatmentInstance != null) {
                touchedTreatmentInstances.put(treatmentInstance.getId(), treatmentInstance);
            }
            if (prestation.getStatus() == PrestationInstanceStatus.REPLACED) {
                (treatmentInstance != null ? prestationInstancesReplacedByTreatment : prestationInstancesReplacedWithoutTreatment).add(prestation);
            }
        }

        // --- 7. Crear las PrestationInstance nuevas ---
        List<PrestationInstance> prestationInstancesNuevas = new ArrayList<>();
        Map<Long, TreatmentInstance> originTreatmentInstancesById = new LinkedHashMap<>();
        for (NewPrestation newPrestation : newPrestationItems) {
            PrestationInstanceItemDTO item = newPrestation.item();
            PrestationType type = newPrestation.type();

            dentistSpecialtyDomainService.validateAllowed(dentist, type.getDentistSpecialties(), type.getLabel());

            TreatmentInstance treatmentInstance = null;
            if (item.treatmentInstanceRef() != null) {
                treatmentInstance = resolveTreatmentInstanceRef(item.treatmentInstanceRef(), treatmentInstancesByTempId, patientId);
                treatmentInstanceDomainService.validateReferenceNotTerminal(treatmentInstance);
            }
            TreatmentInstance origin = null;
            if (item.originTreatmentInstanceRef() != null) {
                origin = resolveTreatmentInstanceRef(item.originTreatmentInstanceRef(), treatmentInstancesByTempId, patientId);
                originTreatmentInstancesById.putIfAbsent(origin.getId(), origin);
            }

            Scope ownScope = scopeMapper.toScope(item.scope());
            if (!ownScope.isEmpty()) {
                scopeDomainService.validateToothListDoesNotCompleteLevel(ownScope);
            }

            // Ambas se invocan siempre: la regla decide adentro según el tipo (D-41, D-50).
            prestationInstanceDomainService.validatePrestationWithoutOwnLocation(type, ownScope, item.treatmentInstanceRef(), item.originTreatmentInstanceRef());
            prestationInstanceDomainService.validateOriginRequired(type, origin);

            Set<PrestationScopeType> allowedScopes = type.getAllowedScopes();
            if (allowedScopes != null && !allowedScopes.isEmpty()) {
                // También con ubicación efectiva null: el domain service rechaza una prestación con ubicación que nace sin ella (D-07).
                Scope effectiveScope = prestationInstanceDomainService.resolveLocation(ownScope, treatmentInstance);
                prestationInstanceDomainService.validateScopeAllowedForType(type, effectiveScope);
                prestationInstanceDomainService.validateMaxTooth(type, effectiveScope);
                prestationInstanceDomainService.validateSameMaxillary(type, effectiveScope);
                prestationInstanceDomainService.validateSplintingContiguity(type, effectiveScope);
            }

            PrestationInstance created = createPrestationInstance(item, type, treatmentInstance, origin, ownScope, patient, consultationInstance);
            prestationInstancesNuevas.add(created);
            if (treatmentInstance != null) {
                touchedTreatmentInstances.put(treatmentInstance.getId(), treatmentInstance);
            }
        }

        // --- 8. Derivar la condición de los TreatmentInstance tocados ---
        for (TreatmentInstance treatmentInstance : touchedTreatmentInstances.values()) {
            List<PrestationInstance> allPrestations = prestationInstanceRepository.findByTreatmentInstanceId(treatmentInstance.getId());
            TreatmentConditionType derived = treatmentInstanceDomainService.calculateTreatmentCondition(allPrestations);
            if (derived != treatmentInstance.getTreatmentCondition()) {
                treatmentInstance.applyCondition(derived);
                treatmentInstanceRepository.save(treatmentInstance);
                treatmentInstanceHistoryRepository.save(TreatmentInstanceHistory.build(treatmentInstance, consultationInstance, derived));
            }
        }

        // --- 9. Validar sobre lo persistido (ADR-0033) ---
        prestationInstanceRepository.flush();

        treatmentInstanceDomainService.validateReplacementTraceability(treatmentInstancesReplacedExplicit, prestationInstancesReplacedByTreatment, treatmentInstancesNuevos, prestationInstancesNuevas);
        prestationInstanceDomainService.validatePrestationInstanceReplacementTraceability(prestationInstancesReplacedWithoutTreatment, treatmentInstancesNuevos, prestationInstancesNuevas);

        for (TreatmentInstance origin : originTreatmentInstancesById.values()) {
            treatmentInstanceDomainService.validateOriginState(origin);
        }

        List<TreatmentInstance> treatmentInstancesActivas = treatmentInstanceRepository.findActiveByPatientId(patientId);
        treatmentInstanceDomainService.validateLocationCollision(treatmentInstancesNuevos, treatmentInstancesActivas);
        List<PrestationInstance> prestationInstancesInProgress = prestationInstanceRepository.findInProgressByPatientId(patientId);
        prestationInstanceDomainService.validatePrestationInstancePending(prestationInstancesNuevas, prestationInstancesInProgress);

        // --- 10. Cerrar la consulta y responder ---
        changeConsultationStatusUseCase.execute(consultation, ConsultationStatusType.FINISHED);

        return new Response<>(
                true,
                messageSource.getMessage("createConsultationInstanceUseCase.create.ok", null, LocaleContextHolder.getLocale()),
                consultationInstance.getId()
        );
    }

    /**
     * Solo el odontólogo asignado a la consulta puede crear su instancia (D-62). Para quien no es dueño, una consulta
     * ajena se ve igual que una inexistente (404, ADR-0027).
     */
    private void validateOwnership(Consultation consultation, UserSec authenticatedUser) {
        Long dentistPersonId = consultation.getDentist().getPerson().getId();
        Long authenticatedPersonId = authenticatedUser.getPerson().getId();
        if (!Objects.equals(dentistPersonId, authenticatedPersonId)) {
            throw new NotFoundException("exception.createConsultationInstanceUseCase.consultationNotOwned.user", null, "exception.createConsultationInstanceUseCase.consultationNotOwned.log", new Object[]{consultation.getId(), authenticatedPersonId, "createConsultationInstanceUseCase", "validateOwnership"}, LogLevel.WARN);
        }
    }

    /** Paso 5: valida especialidad (D-61) y ubicación (D-08), crea el tratamiento con su alta en el historial (D-19, D-51). */
    private TreatmentInstance createTreatmentInstance(TreatmentInstanceItemDTO item, Patient patient, Dentist dentist, ConsultationInstance consultationInstance) {
        Treatment treatment = treatmentService.findById(item.treatmentId());
        dentistSpecialtyDomainService.validateAllowed(dentist, treatment.getDentistSpecialties(), treatment.getLabel());

        Scope scope = scopeMapper.toScope(item.scope());
        scopeDomainService.validateToothListDoesNotCompleteLevel(scope);

        TreatmentConditionType condition = item.treatmentCondition() != null ? item.treatmentCondition() : TreatmentConditionType.REQUIRED;
        TreatmentInstance created = treatmentInstanceRepository.save(TreatmentInstance.build(patient, scope, treatment, condition));
        treatmentInstanceHistoryRepository.save(TreatmentInstanceHistory.build(created, consultationInstance, condition));
        return created;
    }

    /** Paso 6: valida el avance con lo persistido, lo aplica con una fila de historial por step (D-19) y deriva el estado (D-27). */
    private PrestationInstance advanceExistingPrestation(ExistingPrestation existing, ConsultationInstance consultationInstance) {
        PrestationInstance prestation = existing.prestation();
        List<StepItemDTO> stepItems = existing.item().steps();
        Long typeId = prestation.getType().getId();

        List<PrestationStep> catalog = prestationStepService.findAllPrestationStepsByPrestation(typeId);
        List<PrestationStepInstance> persistedSteps = prestationStepInstanceRepository.findByPrestationInstance(prestation);

        // 6.1 Primero se resuelven todos los steps (404 / 400); recién después se valida el avance completo.
        List<ResolvedStep> resolvedSteps = new ArrayList<>();
        for (StepItemDTO stepItem : stepItems) {
            if (stepItem.id() != null) {
                resolvedSteps.add(new ResolvedStep(stepItem, null, prestationStepInstanceQueryService.findByIdAndPrestationInstanceId(stepItem.id(), prestation.getId())));
            } else {
                PrestationStep catalogStep = prestationStepService.findById(stepItem.stepId());
                prestationInstanceDomainService.validatePrestationStep(typeId, stepItem.stepId());
                resolvedSteps.add(new ResolvedStep(stepItem, catalogStep, null));
            }
        }
        prestationInstanceDomainService.validateStepAdvancement(prestation, catalog, persistedSteps, stepItems);

        // 6.2 Se aplica: un step existente avanza y uno nuevo se crea; los dos escriben una fila nueva de historial.
        List<PrestationStepInstance> allSteps = new ArrayList<>(persistedSteps);
        for (ResolvedStep resolved : resolvedSteps) {
            PrestationStepInstance step;
            if (resolved.existingStep() != null) {
                step = resolved.existingStep();
                step.applyStatus(resolved.item().status());
                if (allSteps.stream().noneMatch(known -> known == step)) {
                    allSteps.add(step);
                }
            } else {
                step = PrestationStepInstance.build(prestation, resolved.catalogStep(), resolved.item().status());
                allSteps.add(step);
            }
            prestationStepInstanceRepository.save(step);
            prestationStepInstanceHistoryRepository.save(PrestationStepInstanceHistory.build(step, consultationInstance, resolved.item().status()));
        }

        // 6.3 Se recalcula el estado desde los steps ya aplicados.
        prestation.applyStatus(prestationInstanceDomainService.calculatePrestationInstanceStatus(catalog, allSteps));
        return prestationInstanceRepository.save(prestation);
    }

    /** Paso 7.6 y 7.7: resuelve y valida los steps antes de crear nada, calcula precio y promoción, crea la prestación y sus steps. */
    private PrestationInstance createPrestationInstance(PrestationInstanceItemDTO item, PrestationType type, TreatmentInstance treatmentInstance,
                                                        TreatmentInstance origin, Scope ownScope, Patient patient, ConsultationInstance consultationInstance) {
        Long typeId = type.getId();
        List<PrestationStep> catalog = prestationStepService.findAllPrestationStepsByPrestation(typeId);

        List<PrestationStep> catalogSteps = new ArrayList<>();
        for (StepItemDTO stepItem : item.steps()) {
            catalogSteps.add(prestationStepService.findById(stepItem.stepId()));
            prestationInstanceDomainService.validatePrestationStep(typeId, stepItem.stepId());
        }
        prestationInstanceDomainService.validateStepAdvancement(null, catalog, List.of(), item.steps());

        BigDecimal price = prestationTypePriceService.currentPrice(typeId).getPrice();
        Promotion promotion = promotionQueryService.findById(item.promotionId());
        BigDecimal promotionAmount = promotion != null ? promotion.calculateAmount(price) : null;

        // Siempre nace IN_PROGRESS; el estado real se deriva de sus steps (D-27).
        PrestationInstance created = prestationInstanceRepository.save(PrestationInstance.build(
                patient, type, treatmentInstance, origin, ownScope, PrestationInstanceStatus.IN_PROGRESS,
                price, promotion, promotionAmount, item.discountType(), item.discountValue()));

        List<PrestationStepInstance> steps = new ArrayList<>();
        for (int i = 0; i < item.steps().size(); i++) {
            PrestationStepStatus status = item.steps().get(i).status();
            PrestationStepInstance step = prestationStepInstanceRepository.save(PrestationStepInstance.build(created, catalogSteps.get(i), status));
            prestationStepInstanceHistoryRepository.save(PrestationStepInstanceHistory.build(step, consultationInstance, status));
            steps.add(step);
        }

        created.applyStatus(prestationInstanceDomainService.calculatePrestationInstanceStatus(catalog, steps));
        return prestationInstanceRepository.save(created);
    }

    /**
     * Resuelve la referencia a un tratamiento: por {@code tempId} entre los que nacen en este request (400 si no está)
     * o por {@code id} entre los del paciente (404 si no existe o es de otro paciente) (D-23, D-38, D-60).
     */
    private TreatmentInstance resolveTreatmentInstanceRef(TreatmentInstanceRefDTO ref, Map<String, TreatmentInstance> treatmentInstancesByTempId, Long patientId) {
        if (ref.tempId() != null) {
            TreatmentInstance byTempId = treatmentInstancesByTempId.get(ref.tempId());
            if (byTempId == null) {
                throw new BadRequestException("exception.createConsultationInstanceUseCase.tempIdNotFound.user", new Object[]{ref.tempId()}, "exception.createConsultationInstanceUseCase.tempIdNotFound.log", new Object[]{ref.tempId(), "createConsultationInstanceUseCase", "resolveTreatmentInstanceRef"}, LogLevel.ERROR);
            }
            return byTempId;
        }
        return treatmentInstanceQueryService.findByIdAndPatientId(ref.id(), patientId);
    }

    private record TreatmentToClose(TreatmentInstance treatmentInstance, TreatmentConditionType condition) {
    }

    private record ExistingPrestation(PrestationInstanceItemDTO item, PrestationInstance prestation) {
    }

    private record NewPrestation(PrestationInstanceItemDTO item, PrestationType type) {
    }

    private record ResolvedStep(StepItemDTO item, PrestationStep catalogStep, PrestationStepInstance existingStep) {
    }
}
