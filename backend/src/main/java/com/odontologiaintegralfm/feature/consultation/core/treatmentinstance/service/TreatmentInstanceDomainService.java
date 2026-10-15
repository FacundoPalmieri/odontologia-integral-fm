package com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.core.scope.model.Scope;
import com.odontologiaintegralfm.feature.consultation.core.scope.service.ScopeDomainService;
import com.odontologiaintegralfm.feature.consultation.core.treatmentinstance.model.TreatmentInstance;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reglas de dominio de {@link TreatmentInstance}: derivación de la condición, estados de referencia y de origen,
 * colisión de ubicación y trazabilidad de reemplazos (D-28, D-33, D-35, D-38, D-40, D-51).
 */
@Service
public class TreatmentInstanceDomainService {

    private static final Set<TreatmentConditionType> ACTIVE =
            Set.of(TreatmentConditionType.REQUIRED, TreatmentConditionType.IN_PROGRESS);

    private final ScopeDomainService scopeDomainService;

    public TreatmentInstanceDomainService(ScopeDomainService scopeDomainService) {
        this.scopeDomainService = scopeDomainService;
    }

    /**
     * Deriva la condición del tratamiento desde todas sus prestaciones (D-28). Precedencia:
     * IN_PROGRESS > REPLACED > FINISHED > CANCELED.
     */
    public TreatmentConditionType calculateTreatmentCondition(List<PrestationInstance> prestationInstances) {
        if (prestationInstances.isEmpty()) {
            throw new IllegalArgumentException("Un tratamiento sin prestaciones no se deriva (D-28)");
        }
        if (anyWithStatus(prestationInstances, PrestationInstanceStatus.IN_PROGRESS)) {
            return TreatmentConditionType.IN_PROGRESS;
        }
        if (anyWithStatus(prestationInstances, PrestationInstanceStatus.REPLACED)) {
            return TreatmentConditionType.REPLACED;
        }
        if (anyWithStatus(prestationInstances, PrestationInstanceStatus.FINISHED)) {
            return TreatmentConditionType.FINISHED;
        }
        return TreatmentConditionType.CANCELED;
    }

    /** El cierre explícito (REPLACED/CANCELED) solo se admite sobre un tratamiento persistido en REQUIRED (D-33). */
    public void validateExplicitCloseAllowed(TreatmentInstance treatmentInstance) {
        if (treatmentInstance.getTreatmentCondition() != TreatmentConditionType.REQUIRED) {
            throw new ConflictException("exception.treatmentInstanceDomainService.validateExplicitCloseAllowed.user", new Object[]{treatmentInstance.getTreatmentCondition()}, "exception.treatmentInstanceDomainService.validateExplicitCloseAllowed.log", new Object[]{treatmentInstance.getId(), treatmentInstance.getTreatmentCondition(), "TreatmentInstanceDomainService", "validateExplicitCloseAllowed"}, LogLevel.ERROR);
        }
    }

    /** Nada nuevo cuelga de un tratamiento cerrado: FINISHED, REPLACED, CANCELED o PREEXISTING (D-38). */
    public void validateReferenceNotTerminal(TreatmentInstance referenced) {
        TreatmentConditionType condition = referenced.getTreatmentCondition();
        if (condition != TreatmentConditionType.REQUIRED && condition != TreatmentConditionType.IN_PROGRESS) {
            throw new ConflictException("exception.treatmentInstanceDomainService.validateReferenceNotTerminal.user", new Object[]{condition}, "exception.treatmentInstanceDomainService.validateReferenceNotTerminal.log", new Object[]{referenced.getId(), condition, "TreatmentInstanceDomainService", "validateReferenceNotTerminal"}, LogLevel.ERROR);
        }
    }

    /** El tratamiento de origen de una prótesis tiene que estar FINISHED o PREEXISTING (D-38). */
    public void validateOriginState(TreatmentInstance origin) {
        TreatmentConditionType condition = origin.getTreatmentCondition();
        if (condition != TreatmentConditionType.FINISHED && condition != TreatmentConditionType.PREEXISTING) {
            throw new ConflictException("exception.treatmentInstanceDomainService.validateOriginState.user", new Object[]{condition}, "exception.treatmentInstanceDomainService.validateOriginState.log", new Object[]{origin.getId(), condition, "TreatmentInstanceDomainService", "validateOriginState"}, LogLevel.ERROR);
        }
    }

    /**
     * Colisión de ubicación (D-35, D-51, D-53): cada tratamiento nuevo que termina REQUIRED, IN_PROGRESS o
     * PREEXISTING no puede solaparse con otro tratamiento activo (REQUIRED/IN_PROGRESS) del paciente, salvo consigo
     * mismo. PREEXISTING nunca cuenta como activo del lado de lo persistido; los nuevos cerrados no se evalúan.
     * Un PREEXISTING nuevo no se compara contra los activos que nacen en el mismo request (se identifican por id).
     */
    public void validateLocationCollision(List<TreatmentInstance> treatmentInstancesNuevos, List<TreatmentInstance> treatmentInstancesActivas) {
        Set<Long> idsNuevos = treatmentInstancesNuevos.stream()
                .map(TreatmentInstance::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (TreatmentInstance nuevo : treatmentInstancesNuevos) {
            TreatmentConditionType condition = nuevo.getTreatmentCondition();
            if (condition != TreatmentConditionType.PREEXISTING && !ACTIVE.contains(condition)) {
                continue;
            }
            for (TreatmentInstance activo : treatmentInstancesActivas) {
                if (isSame(activo, nuevo) || !ACTIVE.contains(activo.getTreatmentCondition())) {
                    continue;
                }
                if (condition == TreatmentConditionType.PREEXISTING && idsNuevos.contains(activo.getId())) {
                    continue;
                }
                if (scopeDomainService.overlaps(nuevo.getScope(), activo.getScope())) {
                    String location = describe(nuevo.getScope());
                    throw new ConflictException("exception.treatmentInstanceDomainService.validateLocationCollision.user", new Object[]{location}, "exception.treatmentInstanceDomainService.validateLocationCollision.log", new Object[]{nuevo.getId(), activo.getId(), location, "TreatmentInstanceDomainService", "validateLocationCollision"}, LogLevel.ERROR);
                }
            }
        }
    }

    /**
     * Trazabilidad de los reemplazos de tratamientos (D-40, Casos A, B y C): por cada sujeto cerrado REPLACED tiene
     * que venir algo nuevo en el mismo request que cubra su ubicación. Si tras derivar el tratamiento de una
     * prestación reemplazada sigue IN_PROGRESS (otra prestación viva), el reemplazo no puede ser de otro tratamiento.
     * Los sujetos sin ubicación se saltean.
     */
    public void validateReplacementTraceability(List<TreatmentInstance> treatmentInstancesReplacedExplicit,
                                                List<PrestationInstance> prestationInstancesReplacedByTreatment,
                                                List<TreatmentInstance> treatmentInstancesNuevos,
                                                List<PrestationInstance> prestationInstancesNuevas) {
        for (TreatmentInstance replaced : treatmentInstancesReplacedExplicit) {
            Scope location = replaced.getScope();
            if (isBlank(location)) {
                continue;
            }
            if (!coveredByNewTreatment(location, treatmentInstancesNuevos, null)
                    && !coveredByNewPrestation(location, prestationInstancesNuevas, null)) {
                throw missingReplacement(replaced, location);
            }
        }

        for (PrestationInstance replacedPrestation : prestationInstancesReplacedByTreatment) {
            TreatmentInstance treatmentInstance = replacedPrestation.getTreatmentInstance();
            Scope location = treatmentInstance.getScope();
            if (isBlank(location)) {
                continue;
            }
            if (treatmentInstance.getTreatmentCondition() == TreatmentConditionType.IN_PROGRESS) {
                validateReplacementWhileTreatmentStillLive(treatmentInstance, location, treatmentInstancesNuevos, prestationInstancesNuevas);
            } else if (!coveredByNewTreatment(location, treatmentInstancesNuevos, null)
                    && !coveredByNewPrestation(location, prestationInstancesNuevas, null)) {
                throw missingReplacement(treatmentInstance, location);
            }
        }
    }

    /** Caso C (y B con reemplazo sobre el mismo tratamiento): el tratamiento sigue vivo, solo cambia la prestación. */
    private void validateReplacementWhileTreatmentStillLive(TreatmentInstance treatmentInstance, Scope location,
                                                            List<TreatmentInstance> treatmentInstancesNuevos,
                                                            List<PrestationInstance> prestationInstancesNuevas) {
        boolean otherTreatmentCovers = treatmentInstancesNuevos.stream()
                .anyMatch(nuevo -> !Objects.equals(nuevo.getTreatment(), treatmentInstance.getTreatment())
                        && scopeDomainService.covers(nuevo.getScope(), location));
        boolean otherTreatmentInstanceCovers = prestationInstancesNuevas.stream()
                .anyMatch(nueva -> nueva.getTreatmentInstance() != null
                        && !isSame(nueva.getTreatmentInstance(), treatmentInstance)
                        && scopeDomainService.covers(nueva.getTreatmentInstance().getScope(), location));
        if (otherTreatmentCovers || otherTreatmentInstanceCovers) {
            throw new ConflictException("exception.treatmentInstanceDomainService.validateReplacementTraceability.liveTreatment.user", null, "exception.treatmentInstanceDomainService.validateReplacementTraceability.liveTreatment.log", new Object[]{treatmentInstance.getId(), describe(location), "TreatmentInstanceDomainService", "validateReplacementTraceability"}, LogLevel.ERROR);
        }
        if (!coveredByNewPrestation(location, prestationInstancesNuevas, treatmentInstance)
                && !coveredByNewTreatment(location, treatmentInstancesNuevos, treatmentInstance.getTreatment())) {
            throw missingReplacement(treatmentInstance, location);
        }
    }

    /**
     * @param sameTreatment si no es null, solo cuentan los tratamientos nuevos de ese mismo Treatment
     */
    private boolean coveredByNewTreatment(Scope location, List<TreatmentInstance> treatmentInstancesNuevos,
                                          com.odontologiaintegralfm.feature.consultation.catalogs.treatment.model.Treatment sameTreatment) {
        return treatmentInstancesNuevos.stream()
                .filter(nuevo -> sameTreatment == null || Objects.equals(nuevo.getTreatment(), sameTreatment))
                .anyMatch(nuevo -> scopeDomainService.covers(nuevo.getScope(), location));
    }

    /**
     * Una prestación nueva cubre con su scope propio, o por construcción si referencia el mismo tratamiento
     * ({@code sameTreatmentInstance}); referenciar otro tratamiento cubre con la ubicación de ese tratamiento.
     */
    private boolean coveredByNewPrestation(Scope location, List<PrestationInstance> prestationInstancesNuevas,
                                           TreatmentInstance sameTreatmentInstance) {
        for (PrestationInstance nueva : prestationInstancesNuevas) {
            if (sameTreatmentInstance != null && isSame(nueva.getTreatmentInstance(), sameTreatmentInstance)) {
                return true;
            }
            Scope effective = effectiveLocation(nueva);
            if (effective != null && scopeDomainService.covers(effective, location)) {
                return true;
            }
        }
        return false;
    }

    private Scope effectiveLocation(PrestationInstance prestation) {
        if (!isBlank(prestation.getScope())) {
            return prestation.getScope();
        }
        return prestation.getTreatmentInstance() != null ? prestation.getTreatmentInstance().getScope() : null;
    }

    private ConflictException missingReplacement(TreatmentInstance replaced, Scope location) {
        return new ConflictException("exception.treatmentInstanceDomainService.validateReplacementTraceability.user", new Object[]{describe(location)}, "exception.treatmentInstanceDomainService.validateReplacementTraceability.log", new Object[]{replaced.getId(), describe(location), "TreatmentInstanceDomainService", "validateReplacementTraceability"}, LogLevel.ERROR);
    }

    private boolean anyWithStatus(List<PrestationInstance> prestationInstances, PrestationInstanceStatus status) {
        return prestationInstances.stream().anyMatch(prestation -> prestation.getStatus() == status);
    }

    /** Mismo tratamiento por `equals` de la entidad (compara por id), no por referencia. */
    private boolean isSame(TreatmentInstance a, TreatmentInstance b) {
        return Objects.equals(a, b);
    }

    private boolean isBlank(Scope scope) {
        return scope == null || scope.isEmpty();
    }

    /** Texto legible de una ubicación para los mensajes. */
    private String describe(Scope scope) {
        if (scope.isFullMouth()) {
            return "Boca completa";
        }
        if (scope.getMaxillary() != null) {
            return scope.getMaxillary().getLabel();
        }
        if (scope.getQuadrant() != null) {
            return scope.getQuadrant().getLabel();
        }
        String teeth = scope.getTooth().stream().map(Tooth::getCode).map(String::valueOf).collect(Collectors.joining(", "));
        return scope.getToothFace() != null ? teeth + " (" + scope.getToothFace().getLabel() + ")" : teeth;
    }
}
