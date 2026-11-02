package com.odontologiaintegralfm.feature.consultation.scope.service;

import com.odontologiaintegralfm.feature.consultation.scope.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.scope.model.Scope;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Geometría de las ubicaciones clínicas: expansión a dientes, solapamiento, cobertura, completitud de nivel
 * y contigüidad (ADR-0023). No tiene colaboradores.
 */
@Service
public class ScopeDomainService {

    /** Arcadas en el orden en que se recorren los dientes de un extremo al otro (cruzan la línea media). */
    private static final List<List<Tooth>> ARCHES = List.of(
            arch(18, 17, 16, 15, 14, 13, 12, 11, 21, 22, 23, 24, 25, 26, 27, 28),
            arch(48, 47, 46, 45, 44, 43, 42, 41, 31, 32, 33, 34, 35, 36, 37, 38),
            arch(55, 54, 53, 52, 51, 61, 62, 63, 64, 65),
            arch(85, 84, 83, 82, 81, 71, 72, 73, 74, 75)
    );

    private static List<Tooth> arch(int... codes) {
        return Arrays.stream(codes).mapToObj(Tooth::fromCode).toList();
    }

    /**
     * Rechaza (400) una lista de dientes que completa exactamente un nivel superior: un cuadrante
     * (permanente o temporal) o un maxilar completo de una misma dentición (D-08, decisión 7).
     * El front tiene que mandar el nivel explícito en su lugar.
     */
    public void validateToothListDoesNotCompleteLevel(Scope scope) {
        Set<Tooth> teeth = new HashSet<>(scope.getTooth());
        if (teeth.isEmpty()) {
            return;
        }
        for (Set<Tooth> levelTeeth : completableLevels()) {
            if (teeth.equals(levelTeeth)) {
                throw new BadRequestException("exception.scopeDomainService.validateToothListDoesNotCompleteLevel.user", null, "exception.scopeDomainService.validateToothListDoesNotCompleteLevel.log", new Object[]{scope.getTooth(), "ScopeDomainService", "validateToothListDoesNotCompleteLevel"}, LogLevel.ERROR);
            }
        }
    }

    /** True si las dos ubicaciones comparten algún diente; si ambas indican cara, tiene que ser la misma. Simétrico. */
    public boolean overlaps(Scope a, Scope b) {
        Set<Tooth> shared = new HashSet<>(expandTeeth(a));
        shared.retainAll(expandTeeth(b));
        if (shared.isEmpty()) {
            return false;
        }
        return a.getToothFace() == null || b.getToothFace() == null || a.getToothFace() == b.getToothFace();
    }

    /**
     * True si {@code newScope} incluye todos los dientes de {@code replaced} y, cuando trae cara, la reemplazada
     * trae la misma (D-40).
     */
    public boolean covers(Scope newScope, Scope replaced) {
        if (!expandTeeth(newScope).containsAll(expandTeeth(replaced))) {
            return false;
        }
        return newScope.getToothFace() == null || newScope.getToothFace() == replaced.getToothFace();
    }

    /** Expande una ubicación a sus dientes. Se usa solo al leer: nunca se persiste expandida (D-07). */
    public Set<Tooth> expandTeeth(Scope scope) {
        Set<Tooth> teeth = EnumSet.noneOf(Tooth.class);
        if (scope.isFullMouth()) {
            teeth.addAll(Arrays.asList(Tooth.values()));
        }
        if (scope.getMaxillary() != null) {
            teeth.addAll(scope.getMaxillary().getTeeth());
        }
        if (scope.getQuadrant() != null) {
            teeth.addAll(scope.getQuadrant().getTeeth());
        }
        teeth.addAll(scope.getTooth());
        return teeth;
    }

    /** True si los dientes ocupan posiciones consecutivas de una misma arcada; la línea media no corta (D-59). */
    public boolean areContiguous(List<Tooth> teeth) {
        Set<Tooth> distinct = new HashSet<>(teeth);
        if (distinct.size() <= 1) {
            return true;
        }
        for (List<Tooth> arch : ARCHES) {
            if (arch.containsAll(distinct)) {
                List<Integer> positions = new ArrayList<>();
                for (Tooth tooth : distinct) {
                    positions.add(arch.indexOf(tooth));
                }
                positions.sort(Integer::compareTo);
                return positions.get(positions.size() - 1) - positions.get(0) == positions.size() - 1;
            }
        }
        return false;
    }

    /** Conjuntos de dientes que, si llegan como lista, ya son un nivel superior: cuadrantes y maxilares por dentición. */
    private List<Set<Tooth>> completableLevels() {
        List<Set<Tooth>> levels = new ArrayList<>();
        for (Quadrant quadrant : Quadrant.values()) {
            levels.add(new HashSet<>(quadrant.getTeeth()));
        }
        for (Maxillary maxillary : Maxillary.values()) {
            levels.add(denture(maxillary, true));
            levels.add(denture(maxillary, false));
        }
        return levels;
    }

    private Set<Tooth> denture(Maxillary maxillary, boolean permanent) {
        Set<Tooth> teeth = new HashSet<>();
        for (Tooth tooth : maxillary.getTeeth()) {
            if ((tooth.getCode() < 50) == permanent) {
                teeth.add(tooth);
            }
        }
        return teeth;
    }
}
