package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.service;


import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationStepService;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.StepItemDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationStepInstance;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.core.scope.model.Scope;
import com.odontologiaintegralfm.feature.consultation.core.scope.service.ScopeDomainService;
import com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.dto.TreatmentInstanceRefDTO;
import com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.model.TreatmentInstance;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class PrestationInstanceDomainService {

    /** Valor del catálogo (name, no label) de Ferulización, que exige dientes contiguos (D-59). */
    private static final String SPLINTING = "SPLINTING";

    private final PrestationStepService prestationStepService;
    private final ScopeDomainService scopeDomainService;

    public PrestationInstanceDomainService(PrestationStepService prestationStepService, ScopeDomainService scopeDomainService) {
        this.prestationStepService = prestationStepService;
        this.scopeDomainService = scopeDomainService;
    }


    /**
     * Valída que el step enviado pertenezca al tipo de prestación indicado.
     * Lanza BadRequestException si el step no forma parte del workflow de esa prestación.
     */
    public void validatePrestationStep(Long prestationId, Long prestationStepId) {

        List<PrestationStep> prestationSteps = prestationStepService.findAllPrestationStepsByPrestation(prestationId);

        boolean exists =  prestationSteps.stream()
                .anyMatch(prestationStep -> prestationStep.getId().equals(prestationStepId));


        if(!exists){
            throw new BadRequestException("exception.validatePrestationStep.user",null,"exception.validatePrestationStep.log",new Object[]{prestationStepId, prestationId,"PrestationInstanceDomainService","validatePrestationStep"},LogLevel.ERROR);
        }

    }


    /**
     * Una prestación única (isUnique) no puede venir con ninguna otra en el mismo request, ni nueva ni con avance
     * de steps sobre una existente (D-36). Recibe el tipo de cada ítem de {@code prestation[]}.
     */
    public void validatePrestationUnique(List<PrestationType> prestationTypesDelRequest) {
        if (prestationTypesDelRequest.size() <= 1) {
            return;
        }
        for (PrestationType type : prestationTypesDelRequest) {
            if (type.isUnique()) {
                throw new ConflictException("exception.createConsultationInstanceUseCase.prestationUnique.user", new Object[]{type.getLabel()}, "exception.createConsultationInstanceUseCase.prestationUnique.log", new Object[]{type.getId(), type.getLabel(), "PrestationInstanceDomainService", "validatePrestationUnique"}, LogLevel.ERROR);
            }
        }
    }


    /**
     * Deriva el estado de la prestación desde sus steps (D-27), con 4 salidas: IN_PROGRESS, FINISHED, REPLACED o CANCELED.
     * Una cadena REPLACED/CANCELED tiene que ser inmediata al último FINISHED/OMITTED y cubrir todos los steps
     * posteriores del catálogo con el mismo valor; si no, 409.
     */
    public PrestationInstanceStatus calculatePrestationInstanceStatus(List<PrestationStep> catalogSteps, List<PrestationStepInstance> stepInstances) {
        List<PrestationStep> ordered = catalogSteps.stream()
                .sorted(Comparator.comparingInt(PrestationStep::getPosition))
                .toList();
        Map<Long, PrestationStepStatus> statusByStep = new HashMap<>();
        for (PrestationStepInstance instance : stepInstances) {
            statusByStep.put(instance.getStep().getId(), instance.getStatus());
        }

        int chainStart = -1;
        for (int i = 0; i < ordered.size(); i++) {
            if (isChainStatus(statusByStep.get(ordered.get(i).getId()))) {
                chainStart = i;
                break;
            }
        }

        if (chainStart < 0) {
            boolean allResolved = ordered.stream().allMatch(step -> isResolved(statusByStep.get(step.getId())));
            return allResolved ? PrestationInstanceStatus.FINISHED : PrestationInstanceStatus.IN_PROGRESS;
        }

        for (int i = 0; i < chainStart; i++) {
            if (!isResolved(statusByStep.get(ordered.get(i).getId()))) {
                throw new ConflictException("exception.prestationInstanceDomainService.calculatePrestationInstanceStatus.replacedNotImmediate.user", null, "exception.prestationInstanceDomainService.calculatePrestationInstanceStatus.replacedNotImmediate.log", new Object[]{ordered.get(chainStart).getId(), "PrestationInstanceDomainService", "calculatePrestationInstanceStatus"}, LogLevel.ERROR);
            }
        }
        PrestationStepStatus chainStatus = statusByStep.get(ordered.get(chainStart).getId());
        for (int i = chainStart; i < ordered.size(); i++) {
            if (statusByStep.get(ordered.get(i).getId()) != chainStatus) {
                throw new ConflictException("exception.prestationInstanceDomainService.calculatePrestationInstanceStatus.replacedGap.user", null, "exception.prestationInstanceDomainService.calculatePrestationInstanceStatus.replacedGap.log", new Object[]{ordered.get(i).getId(), "PrestationInstanceDomainService", "calculatePrestationInstanceStatus"}, LogLevel.ERROR);
            }
        }
        return chainStatus == PrestationStepStatus.REPLACED ? PrestationInstanceStatus.REPLACED : PrestationInstanceStatus.CANCELED;
    }


    /**
     * Valída el avance de steps pedido sobre una prestación (D-27), antes de aplicar nada. Para una prestación que
     * nace, {@code prestationInstance} es null y no hay steps persistidos.
     * 409: prestación terminal, step existente terminal, step IN_PROGRESS que no pasa a FINISHED/REPLACED/CANCELED,
     * step nuevo ya persistido, o steps anteriores sin resolver. 400: omitir un step requerido, o nacer REPLACED/CANCELED
     * el primer step del catálogo. Dentro de una cadena REPLACED/CANCELED del mismo request, el step anterior de la
     * cadena cuenta como antecedente válido.
     */
    public void validateStepAdvancement(PrestationInstance prestationInstance, List<PrestationStep> catalogSteps,
                                        List<PrestationStepInstance> stepInstancesPersistidos, List<StepItemDTO> stepItems) {
        Long prestationId = prestationInstance != null ? prestationInstance.getId() : null;
        if (prestationInstance != null && prestationInstance.getStatus() != PrestationInstanceStatus.IN_PROGRESS) {
            throw new ConflictException("exception.prestationInstanceDomainService.validateStepAdvancement.notInProgress.user", new Object[]{prestationId}, "exception.prestationInstanceDomainService.validateStepAdvancement.notInProgress.log", new Object[]{prestationId, prestationInstance.getStatus(), "PrestationInstanceDomainService", "validateStepAdvancement"}, LogLevel.ERROR);
        }

        Map<Long, PrestationStep> catalogById = catalogSteps.stream().collect(Collectors.toMap(PrestationStep::getId, step -> step));
        Map<Long, PrestationStepInstance> persistedByInstanceId = new HashMap<>();
        Map<Long, PrestationStepStatus> resolved = new HashMap<>();
        for (PrestationStepInstance persisted : stepInstancesPersistidos) {
            persistedByInstanceId.put(persisted.getId(), persisted);
            resolved.put(persisted.getStep().getId(), persisted.getStatus());
        }

        List<ItemWithStep> items = new ArrayList<>();
        for (StepItemDTO item : stepItems) {
            if (item.id() != null) {
                PrestationStepInstance persisted = persistedByInstanceId.get(item.id());
                if (persisted == null) {
                    throw new IllegalStateException("El step " + item.id() + " no está entre los steps persistidos de la prestación " + prestationId);
                }
                items.add(new ItemWithStep(item, persisted.getStep(), persisted));
            } else {
                PrestationStep catalogStep = catalogById.get(item.stepId());
                if (catalogStep == null) {
                    throw new BadRequestException("exception.validatePrestationStep.user", null, "exception.validatePrestationStep.log", new Object[]{item.stepId(), prestationId, "PrestationInstanceDomainService", "validateStepAdvancement"}, LogLevel.ERROR);
                }
                items.add(new ItemWithStep(item, catalogStep, null));
            }
        }
        items.sort(Comparator.comparingInt(entry -> entry.catalogStep().getPosition()));

        for (ItemWithStep entry : items) {
            PrestationStepStatus requested = entry.item().status();
            PrestationStep catalogStep = entry.catalogStep();

            if (entry.persisted() != null) {
                validateExistingStepTransition(entry.persisted(), requested, prestationId);
            } else {
                validateNewStep(catalogStep, requested, catalogSteps, resolved, prestationId);
            }

            for (PrestationStep previous : catalogSteps) {
                if (previous.getPosition() < catalogStep.getPosition()) {
                    PrestationStepStatus previousStatus = resolved.get(previous.getId());
                    boolean previousOk = isResolved(previousStatus) || (isChainStatus(requested) && isChainStatus(previousStatus));
                    if (!previousOk) {
                        throw new ConflictException("exception.prestationInstanceDomainService.validateAndBuild.previousNotResolved.user", null, "exception.prestationInstanceDomainService.validateAndBuild.previousNotResolved.log", new Object[]{previous.getId(), catalogStep.getId(), prestationId, "PrestationInstanceDomainService", "validateStepAdvancement"}, LogLevel.ERROR);
                    }
                }
            }
            resolved.put(catalogStep.getId(), requested);
        }
    }

    /** Un step existente terminal no admite estado nuevo; uno IN_PROGRESS solo pasa a FINISHED, REPLACED o CANCELED. */
    private void validateExistingStepTransition(PrestationStepInstance persisted, PrestationStepStatus requested, Long prestationId) {
        PrestationStepStatus current = persisted.getStatus();
        Long catalogStepId = persisted.getStep().getId();
        if (current != PrestationStepStatus.IN_PROGRESS) {
            throw new ConflictException("exception.prestationInstanceDomainService.validateAndBuild.alreadyFinal.user", null, "exception.prestationInstanceDomainService.validateAndBuild.alreadyFinal.log", new Object[]{catalogStepId, current, prestationId, "PrestationInstanceDomainService", "validateStepAdvancement"}, LogLevel.ERROR);
        }
        if (requested != PrestationStepStatus.FINISHED && !isChainStatus(requested)) {
            throw new ConflictException("exception.prestationInstanceDomainService.validateAndBuild.inProgressNotCompleted.user", null, "exception.prestationInstanceDomainService.validateAndBuild.inProgressNotCompleted.log", new Object[]{catalogStepId, requested, prestationId, "PrestationInstanceDomainService", "validateStepAdvancement"}, LogLevel.ERROR);
        }
    }

    /** Un step nuevo no puede estar ya persistido (409), omitirse si es requerido (400) ni nacer REPLACED/CANCELED en el primer lugar (400). */
    private void validateNewStep(PrestationStep catalogStep, PrestationStepStatus requested, List<PrestationStep> catalogSteps,
                                 Map<Long, PrestationStepStatus> resolved, Long prestationId) {
        if (resolved.containsKey(catalogStep.getId())) {
            throw new ConflictException("exception.prestationInstanceDomainService.validateStepAdvancement.duplicateStep.user", null, "exception.prestationInstanceDomainService.validateStepAdvancement.duplicateStep.log", new Object[]{catalogStep.getId(), "PrestationInstanceDomainService", "validateStepAdvancement"}, LogLevel.ERROR);
        }
        if (requested == PrestationStepStatus.OMITTED && catalogStep.isRequired()) {
            throw new BadRequestException("exception.prestationInstanceDomainService.validateAndBuild.omittedRequired.user", null, "exception.prestationInstanceDomainService.validateAndBuild.omittedRequired.log", new Object[]{catalogStep.getId(), prestationId, "PrestationInstanceDomainService", "validateStepAdvancement"}, LogLevel.ERROR);
        }
        boolean isFirst = catalogSteps.stream().noneMatch(step -> step.getPosition() < catalogStep.getPosition());
        if (isFirst && isChainStatus(requested)) {
            throw new BadRequestException("exception.prestationInstanceDomainService.calculatePrestationInstanceStatus.firstStepChainTerminal.user", null, "exception.prestationInstanceDomainService.calculatePrestationInstanceStatus.firstStepChainTerminal.log", new Object[]{catalogStep.getId(), "PrestationInstanceDomainService", "validateStepAdvancement"}, LogLevel.ERROR);
        }
    }


    /**
     * El nivel de la ubicación efectiva tiene que ser uno de los que admite el tipo (D-07). Una prestación con
     * {@code allowedScopes} no vacío no puede quedar sin ubicación efectiva (null o vacía). Un tipo con
     * {@code allowedScopes} vacío (sin ubicación propia) no se evalúa acá.
     */
    public void validateScopeAllowedForType(PrestationType type, Scope effectiveScope) {
        Set<PrestationScopeType> allowed = type.getAllowedScopes();
        if (allowed == null || allowed.isEmpty()) {
            return;
        }
        if (effectiveScope == null || effectiveScope.isEmpty() || !allowed.contains(effectiveScope.level())) {
            throw new ConflictException("exception.prestationInstanceDomainService.validateScopeAllowedForType.user", new Object[]{type.getLabel()}, "exception.prestationInstanceDomainService.validateScopeAllowedForType.log", new Object[]{effectiveScope == null ? null : effectiveScope.level(), type.getId(), "PrestationInstanceDomainService", "validateScopeAllowedForType"}, LogLevel.ERROR);
        }
    }

    /** Techo de dientes por prestación; solo aplica cuando la ubicación es por diente (TOOTH/TOOTH_FACE) (D-29). */
    public void validateMaxTooth(PrestationType type, Scope effectiveScope) {
        if (effectiveScope == null || type.getMaxTooth() == null) {
            return;
        }
        PrestationScopeType level = effectiveScope.level();
        boolean byTooth = level == PrestationScopeType.TOOTH || level == PrestationScopeType.TOOTH_FACE;
        if (byTooth && effectiveScope.getTooth().size() > type.getMaxTooth()) {
            throw new ConflictException("exception.prestationInstanceDomainService.validateMaxTooth.user", new Object[]{type.getLabel(), type.getMaxTooth()}, "exception.prestationInstanceDomainService.validateMaxTooth.log", new Object[]{type.getId(), effectiveScope.getTooth().size(), type.getMaxTooth(), "PrestationInstanceDomainService", "validateMaxTooth"}, LogLevel.ERROR);
        }
    }

    /** Con {@code maxTooth > 1}, todos los dientes tienen que ser del mismo maxilar; no se exige contigüidad (D-11). */
    public void validateSameMaxillary(PrestationType type, Scope effectiveScope) {
        if (effectiveScope == null || type.getMaxTooth() == null || type.getMaxTooth() <= 1) {
            return;
        }
        Set<Maxillary> maxillaries = effectiveScope.getTooth().stream().map(Maxillary::fromTooth).collect(Collectors.toSet());
        if (maxillaries.size() > 1) {
            throw new ConflictException("exception.prestationInstanceDomainService.validateSameMaxillary.user", new Object[]{type.getLabel()}, "exception.prestationInstanceDomainService.validateSameMaxillary.log", new Object[]{type.getId(), effectiveScope.getTooth(), "PrestationInstanceDomainService", "validateSameMaxillary"}, LogLevel.ERROR);
        }
    }

    /** Ferulización (name SPLINTING) exige dientes contiguos, además del mismo maxilar (D-59). */
    public void validateSplintingContiguity(PrestationType type, Scope effectiveScope) {
        if (effectiveScope == null || !SPLINTING.equals(type.getName())) {
            return;
        }
        if (!scopeDomainService.areContiguous(new ArrayList<>(effectiveScope.getTooth()))) {
            throw new ConflictException("exception.prestationInstanceDomainService.validateSplintingContiguity.user", new Object[]{type.getLabel()}, "exception.prestationInstanceDomainService.validateSplintingContiguity.log", new Object[]{type.getId(), effectiveScope.getTooth(), "PrestationInstanceDomainService", "validateSplintingContiguity"}, LogLevel.ERROR);
        }
    }

    /**
     * Un tipo sin ubicación propia ({@code allowedScopes} vacío: Rebasado, Refuerzo metálico) exige
     * {@code originTreatmentInstanceRef} y rechaza scope propio y {@code treatmentInstanceRef} (D-45, D-50).
     * La regla decide adentro por el tipo: con ubicación no hace nada.
     */
    public void validatePrestationWithoutOwnLocation(PrestationType type, Scope ownScope,
                                                     TreatmentInstanceRefDTO treatmentInstanceRef,
                                                     TreatmentInstanceRefDTO originTreatmentInstanceRef) {
        Set<PrestationScopeType> allowed = type.getAllowedScopes();
        if (allowed != null && !allowed.isEmpty()) {
            return;
        }
        if (originTreatmentInstanceRef == null) {
            throw new ConflictException("exception.prestationInstanceDomainService.validatePrestationWithoutOwnLocation.originMissing.user", new Object[]{type.getLabel()}, "exception.prestationInstanceDomainService.validatePrestationWithoutOwnLocation.originMissing.log", new Object[]{type.getId(), "PrestationInstanceDomainService", "validatePrestationWithoutOwnLocation"}, LogLevel.ERROR);
        }
        if (ownScope != null && !ownScope.isEmpty()) {
            throw new ConflictException("exception.prestationInstanceDomainService.validatePrestationWithoutOwnLocation.scopePopulated.user", new Object[]{type.getLabel()}, "exception.prestationInstanceDomainService.validatePrestationWithoutOwnLocation.scopePopulated.log", new Object[]{type.getId(), "PrestationInstanceDomainService", "validatePrestationWithoutOwnLocation"}, LogLevel.ERROR);
        }
        if (treatmentInstanceRef != null) {
            throw new ConflictException("exception.prestationInstanceDomainService.validatePrestationWithoutOwnLocation.treatmentRefPopulated.user", new Object[]{type.getLabel()}, "exception.prestationInstanceDomainService.validatePrestationWithoutOwnLocation.treatmentRefPopulated.log", new Object[]{type.getId(), "PrestationInstanceDomainService", "validatePrestationWithoutOwnLocation"}, LogLevel.ERROR);
        }
    }

    /**
     * Una prestación de reparación de prótesis exige origen: {@code allowedScopes} vacío (D-50) o
     * {@code requiresOriginTreatment} (D-41). La regla decide adentro por el tipo.
     */
    public void validateOriginRequired(PrestationType type, TreatmentInstance originTreatmentInstance) {
        if (originTreatmentInstance != null) {
            return;
        }
        Set<PrestationScopeType> allowed = type.getAllowedScopes();
        if (allowed == null || allowed.isEmpty()) {
            throw new ConflictException("exception.prestationInstanceDomainService.validatePrestationWithoutOwnLocation.originMissing.user", new Object[]{type.getLabel()}, "exception.prestationInstanceDomainService.validatePrestationWithoutOwnLocation.originMissing.log", new Object[]{type.getId(), "PrestationInstanceDomainService", "validateOriginRequired"}, LogLevel.ERROR);
        }
        if (type.isRequiresOriginTreatment()) {
            throw new ConflictException("exception.prestationInstanceDomainService.validateOriginRequired.requiresOrigin.user", new Object[]{type.getLabel()}, "exception.prestationInstanceDomainService.validateOriginRequired.requiresOrigin.log", new Object[]{type.getId(), "PrestationInstanceDomainService", "validateOriginRequired"}, LogLevel.ERROR);
        }
    }

    /**
     * No puede haber dos prestaciones IN_PROGRESS del mismo paciente, del mismo tipo y con ubicación solapada (D-37).
     * Se evalúa sobre el estado ya derivado: cada prestación nueva que terminó IN_PROGRESS contra las IN_PROGRESS del
     * paciente, excluyéndose a sí misma. Una prestación sin ubicación no participa.
     */
    public void validatePrestationInstancePending(List<PrestationInstance> prestationInstancesNuevas,
                                                  List<PrestationInstance> prestationInstancesInProgress) {
        for (PrestationInstance nueva : prestationInstancesNuevas) {
            if (nueva.getStatus() != PrestationInstanceStatus.IN_PROGRESS) {
                continue;
            }
            Scope location = resolveLocation(nueva.getScope(), nueva.getTreatmentInstance());
            if (location == null || location.isEmpty()) {
                continue;
            }
            for (PrestationInstance enCurso : prestationInstancesInProgress) {
                if (enCurso == nueva || !sameType(nueva.getType(), enCurso.getType())) {
                    continue;
                }
                Scope otherLocation = resolveLocation(enCurso.getScope(), enCurso.getTreatmentInstance());
                if (otherLocation != null && !otherLocation.isEmpty() && scopeDomainService.overlaps(location, otherLocation)) {
                    throw new ConflictException("exception.prestationInstanceDomainService.validatePrestationInstancePending.user", new Object[]{nueva.getType().getLabel()}, "exception.prestationInstanceDomainService.validatePrestationInstancePending.log", new Object[]{nueva.getId(), enCurso.getId(), nueva.getType().getId(), "PrestationInstanceDomainService", "validatePrestationInstancePending"}, LogLevel.ERROR);
                }
            }
        }
    }

    /**
     * Trazabilidad del reemplazo de una prestación sin tratamiento (D-40, Caso D): algún tratamiento o prestación
     * nuevos del request tiene que cubrir su ubicación. Las prestaciones sin ubicación efectiva se saltean.
     */
    public void validatePrestationInstanceReplacementTraceability(List<PrestationInstance> prestationInstancesReplacedWithoutTreatment,
                                                                  List<TreatmentInstance> treatmentInstancesNuevos,
                                                                  List<PrestationInstance> prestationInstancesNuevas) {
        for (PrestationInstance replaced : prestationInstancesReplacedWithoutTreatment) {
            Scope location = resolveLocation(replaced.getScope(), replaced.getTreatmentInstance());
            if (location == null || location.isEmpty()) {
                continue;
            }
            boolean coveredByTreatment = treatmentInstancesNuevos.stream()
                    .anyMatch(nuevo -> scopeDomainService.covers(nuevo.getScope(), location));
            boolean coveredByPrestation = prestationInstancesNuevas.stream()
                    .map(nueva -> resolveLocation(nueva.getScope(), nueva.getTreatmentInstance()))
                    .anyMatch(effective -> effective != null && !effective.isEmpty() && scopeDomainService.covers(effective, location));
            if (!coveredByTreatment && !coveredByPrestation) {
                throw new ConflictException("exception.prestationInstanceDomainService.validatePrestationInstanceReplacementTraceability.user", new Object[]{replaced.getType().getLabel()}, "exception.prestationInstanceDomainService.validatePrestationInstanceReplacementTraceability.log", new Object[]{replaced.getId(), replaced.getType().getId(), "PrestationInstanceDomainService", "validatePrestationInstanceReplacementTraceability"}, LogLevel.ERROR);
            }
        }
    }

    /**
     * Ubicación efectiva de una prestación: el scope propio si no está vacío; si no, el del tratamiento que
     * referencia; null si no tiene ninguno (rama solo-origen) (D-07).
     */
    public Scope resolveLocation(Scope ownScope, TreatmentInstance treatmentInstance) {
        if (ownScope != null && !ownScope.isEmpty()) {
            return ownScope;
        }
        return treatmentInstance != null ? treatmentInstance.getScope() : null;
    }

    private boolean sameType(PrestationType a, PrestationType b) {
        return a != null && b != null && Objects.equals(a.getId(), b.getId());
    }

    private boolean isResolved(PrestationStepStatus status) {
        return status == PrestationStepStatus.FINISHED || status == PrestationStepStatus.OMITTED;
    }

    private boolean isChainStatus(PrestationStepStatus status) {
        return status == PrestationStepStatus.REPLACED || status == PrestationStepStatus.CANCELED;
    }

    private record ItemWithStep(StepItemDTO item, PrestationStep catalogStep, PrestationStepInstance persisted) {
    }

}
