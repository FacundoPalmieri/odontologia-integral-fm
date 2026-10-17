package com.odontologiaintegralfm.feature.consultation.scope.model;

import com.odontologiaintegralfm.feature.consultation.prestation.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.scope.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.scope.enums.ToothFace;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

/**
 * Ubicación clínica compartida por {@code TreatmentInstance} y {@code PrestationInstance} (D-10).
 * Persiste el nivel que declaró el front, nunca una lista expandida de dientes (D-07).
 * La geometría de negocio (solapar, cubrir, completar un nivel) vive en {@code ScopeDomainService}.
 */
@Embeddable
@Getter
@NoArgsConstructor
public class Scope {

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @Column(name = "tooth")
    @BatchSize(size = 50)
    private List<Tooth> tooth = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "tooth_face")
    private ToothFace toothFace;

    @Enumerated(EnumType.STRING)
    private Quadrant quadrant;

    @Enumerated(EnumType.STRING)
    private Maxillary maxillary;

    @Column(name = "full_mouth", nullable = false)
    private boolean fullMouth;

    private Scope(List<Tooth> tooth, ToothFace toothFace, Quadrant quadrant, Maxillary maxillary, boolean fullMouth) {
        this.tooth = tooth;
        this.toothFace = toothFace;
        this.quadrant = quadrant;
        this.maxillary = maxillary;
        this.fullMouth = fullMouth;
    }

    /**
     * Única vía de construcción (D-09). No deduplica los dientes.
     * Protege la invariante {@code toothFace} poblado ⇒ {@code tooth} no vacío.
     */
    public static Scope build(List<Tooth> tooth, ToothFace toothFace, Quadrant quadrant, Maxillary maxillary, boolean fullMouth) {
        List<Tooth> teeth = tooth == null ? new ArrayList<>() : new ArrayList<>(tooth);
        if (toothFace != null && teeth.isEmpty()) {
            throw new IllegalArgumentException("toothFace requiere al menos un diente en tooth");
        }
        return new Scope(teeth, toothFace, quadrant, maxillary, fullMouth);
    }

    /** Ubicación sin ningún grupo poblado. */
    public static Scope empty() {
        return new Scope(new ArrayList<>(), null, null, null, false);
    }

    public boolean isEmpty() {
        return (tooth == null || tooth.isEmpty())
                && toothFace == null
                && quadrant == null
                && maxillary == null
                && !fullMouth;
    }

    /** Nivel de la ubicación, derivado de los campos puntuales (D-06). */
    public PrestationScopeType level() {
        if (fullMouth) return PrestationScopeType.FULL_MOUTH;
        if (maxillary != null) return PrestationScopeType.MAXILLARY;
        if (quadrant != null) return PrestationScopeType.QUADRANT;
        if (toothFace != null) return PrestationScopeType.TOOTH_FACE;
        return PrestationScopeType.TOOTH;
    }
}