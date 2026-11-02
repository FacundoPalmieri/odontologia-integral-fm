package com.odontologiaintegralfm.feature.consultation.treatmentinstance.service;

import com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.service.PrestationInstanceDomainService;
import com.odontologiaintegralfm.feature.consultation.scope.model.Scope;
import com.odontologiaintegralfm.feature.consultation.scope.service.ScopeDomainService;
import com.odontologiaintegralfm.feature.consultation.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.model.TreatmentInstance;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Decide cuáles son las prótesis vigentes de un paciente (REQ-038): qué tratamientos son origen de una prótesis,
 * qué ubicaciones suma cada una y cuáles quedan tapadas por una más nueva (ADR-0023). Trabaja sobre las listas
 * que recibe, que ya vienen filtradas por {@code enabled = true} (D-07).
 */
@Service
public class ProsthesisDomainService {

    /** Una prótesis vigente: su tratamiento de origen y sus ubicaciones; {@code scopes[0]} es la del origen. */
    public record CurrentProsthesis(TreatmentInstance origin, List<Scope> scopes) {
    }

    private static final Comparator<TreatmentInstance> OLDEST_FIRST = Comparator
            .comparing(TreatmentInstance::getCreatedAt)
            .thenComparing(TreatmentInstance::getId);

    private static final Comparator<PrestationInstance> OLDEST_PRESTATION_FIRST = Comparator
            .comparing(PrestationInstance::getCreatedAt)
            .thenComparing(PrestationInstance::getId);

    private final ScopeDomainService scopeDomainService;
    private final PrestationInstanceDomainService prestationInstanceDomainService;

    public ProsthesisDomainService(ScopeDomainService scopeDomainService,
                                   PrestationInstanceDomainService prestationInstanceDomainService) {
        this.scopeDomainService = scopeDomainService;
        this.prestationInstanceDomainService = prestationInstanceDomainService;
    }

    /**
     * Prótesis vigentes, de la más antigua a la más nueva por {@code createdAt} e {@code id} del origen (D-06).
     * Cada una trae la ubicación del origen y la de cada arreglo {@code FINISHED} que lleva dientes (D-04).
     */
    public List<CurrentProsthesis> buildCurrentProstheses(List<TreatmentInstance> treatmentInstances,
                                                          List<PrestationInstance> prestationInstances,
                                                          Set<Long> repairLinkedTreatmentIds) {
        Set<TreatmentInstance> treatmentsWithPlacement = findTreatmentsWithFinishedPlacement(prestationInstances);
        Map<TreatmentInstance, List<Scope>> repairScopesByOrigin = collectRepairScopes(prestationInstances);

        List<CurrentProsthesis> candidates = new ArrayList<>();
        for (TreatmentInstance treatmentInstance : treatmentInstances) {
            if (!isProsthesisOrigin(treatmentInstance, repairLinkedTreatmentIds, treatmentsWithPlacement)) {
                continue;
            }
            List<Scope> scopes = new ArrayList<>();
            scopes.add(treatmentInstance.getScope());
            scopes.addAll(repairScopesByOrigin.getOrDefault(treatmentInstance, List.of()));
            candidates.add(new CurrentProsthesis(treatmentInstance, scopes));
        }
        return discardOverlappedByNewer(candidates);
    }

    /** Tratamientos que tienen una prestación {@code FINISHED} marcada "puede ser origen" (D-03). */
    private Set<TreatmentInstance> findTreatmentsWithFinishedPlacement(List<PrestationInstance> prestationInstances) {
        Set<TreatmentInstance> treatments = new HashSet<>();
        for (PrestationInstance prestation : prestationInstances) {
            if (prestation.getStatus() == PrestationInstanceStatus.FINISHED
                    && prestation.getType().isCanBeOrigin()
                    && prestation.getTreatmentInstance() != null) {
                treatments.add(prestation.getTreatmentInstance());
            }
        }
        return treatments;
    }

    /** D-01 filtra por condición; D-02 acepta el {@code PREEXISTING} ligado a arreglos; D-03 acepta el {@code FINISHED} con Colocación. */
    private boolean isProsthesisOrigin(TreatmentInstance treatmentInstance, Set<Long> repairLinkedTreatmentIds,
                                       Set<TreatmentInstance> treatmentsWithPlacement) {
        TreatmentConditionType condition = treatmentInstance.getTreatmentCondition();
        if (condition == TreatmentConditionType.PREEXISTING) {
            return repairLinkedTreatmentIds.contains(treatmentInstance.getTreatment().getId());
        }
        if (condition == TreatmentConditionType.FINISHED) {
            return treatmentsWithPlacement.contains(treatmentInstance);
        }
        return false;
    }

    /**
     * Ubicaciones de los arreglos {@code FINISHED} que llevan dientes, agrupadas por su origen y ordenadas por
     * {@code createdAt} e {@code id} de la prestación (D-04, D-06).
     */
    private Map<TreatmentInstance, List<Scope>> collectRepairScopes(List<PrestationInstance> prestationInstances) {
        Map<TreatmentInstance, List<Scope>> scopesByOrigin = new LinkedHashMap<>();
        prestationInstances.stream()
                .filter(prestation -> prestation.getStatus() == PrestationInstanceStatus.FINISHED
                        && prestation.getOriginTreatmentInstance() != null
                        && prestation.getType().getAllowedScopes() != null
                        && !prestation.getType().getAllowedScopes().isEmpty())
                .sorted(OLDEST_PRESTATION_FIRST)
                .forEach(prestation -> {
                    Scope location = prestationInstanceDomainService.resolveLocation(
                            prestation.getScope(), prestation.getTreatmentInstance());
                    if (location != null && !location.isEmpty()) {
                        scopesByOrigin.computeIfAbsent(prestation.getOriginTreatmentInstance(), origin -> new ArrayList<>())
                                .add(location);
                    }
                });
        return scopesByOrigin;
    }

    /**
     * Deja afuera cada prótesis que tenga, entre las más nuevas, alguna que la solape, aunque esa también quede
     * afuera (D-05). Devuelve las que quedan ordenadas de la más antigua a la más nueva por origen.
     */
    private List<CurrentProsthesis> discardOverlappedByNewer(List<CurrentProsthesis> candidates) {
        List<CurrentProsthesis> sorted = new ArrayList<>(candidates);
        sorted.sort(Comparator.comparing(CurrentProsthesis::origin, OLDEST_FIRST));

        List<CurrentProsthesis> kept = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            boolean coveredByNewer = false;
            for (int j = i + 1; j < sorted.size() && !coveredByNewer; j++) {
                coveredByNewer = overlap(sorted.get(i), sorted.get(j));
            }
            if (!coveredByNewer) {
                kept.add(sorted.get(i));
            }
        }
        return kept;
    }

    private boolean overlap(CurrentProsthesis a, CurrentProsthesis b) {
        return a.scopes().stream().anyMatch(scopeA ->
                b.scopes().stream().anyMatch(scopeB -> scopeDomainService.overlaps(scopeA, scopeB)));
    }
}
