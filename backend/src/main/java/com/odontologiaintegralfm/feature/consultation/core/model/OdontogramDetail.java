package com.odontologiaintegralfm.feature.consultation.core.model;

import com.odontologiaintegralfm.feature.consultation.catalogs.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.catalogs.model.TreatmentCondition;
import com.odontologiaintegralfm.feature.consultation.core.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.core.enums.ToothFace;
import com.odontologiaintegralfm.shared.model.Auditable;
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
@Table(name = "odontogram_details")
@Audited
public class OdontogramDetail extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "odontogram_header_id", nullable = false)
    private OdontogramHeader odontogramHeader;

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



    private OdontogramDetail(OdontogramHeader odontogramHeader, Tooth tooth, ToothFace toothFace, Treatment treatment, TreatmentCondition treatmentCondition) {
        this.odontogramHeader = odontogramHeader;
        this.tooth = tooth;
        this.toothFace = toothFace;
        this.treatment = treatment;
        this.treatmentCondition = treatmentCondition;
    }

    public static OdontogramDetail build(OdontogramHeader odontogramHeader, Tooth tooth, ToothFace toothFace, Treatment treatment, TreatmentCondition treatmentCondition){
        return new OdontogramDetail(
                odontogramHeader,
                tooth,
                toothFace,
                treatment,
                treatmentCondition
        );
    }
}
