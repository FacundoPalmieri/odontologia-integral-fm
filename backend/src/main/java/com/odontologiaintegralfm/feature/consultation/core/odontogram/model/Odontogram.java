package com.odontologiaintegralfm.feature.consultation.core.odontogram.model;

import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.model.TreatmentCondition;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.ToothFace;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * Entidad para representar los detalles de las consultas (Odontograma)
 */

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "odontogram")
@Audited
public class Odontogram extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "odontogram_header_id", nullable = false)
    private ConsultationInstance consultationInstance;

    @Enumerated(EnumType.STRING)
    private Tooth tooth;

    @Enumerated(EnumType.STRING)
    private ToothFace toothFace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Treatment treatment;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_condition_id", nullable = false)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private TreatmentCondition treatmentCondition;



    private Odontogram(ConsultationInstance consultationInstance, Tooth tooth, ToothFace toothFace, Treatment treatment, TreatmentCondition treatmentCondition) {
        this.consultationInstance = consultationInstance;
        this.tooth = tooth;
        this.toothFace = toothFace;
        this.treatment = treatment;
        this.treatmentCondition = treatmentCondition;
    }

    public static Odontogram build(ConsultationInstance consultationInstance, Tooth tooth, ToothFace toothFace, Treatment treatment, TreatmentCondition treatmentCondition){
        return new Odontogram(
                consultationInstance,
                tooth,
                toothFace,
                treatment,
                treatmentCondition
        );
    }
}
