package com.odontologiaintegralfm.feature.consultation.core.model;

import com.odontologiaintegralfm.feature.consultation.catalogs.model.Treatment;
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
@Table(name = "consultation_odontogram")
@Audited
public class ConsultationOdontogram extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @Enumerated(EnumType.STRING)
    private Tooth tooth;

    @Enumerated(EnumType.STRING)
    private ToothFace toothFace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Treatment treatment;


    private ConsultationOdontogram(Consultation consultation, Tooth tooth, ToothFace toothFace, Treatment treatment) {
        this.consultation = consultation;
        this.tooth = tooth;
        this.toothFace = toothFace;
        this.treatment = treatment;
    }

    public static ConsultationOdontogram build(Consultation consultation, Tooth tooth, ToothFace toothFace, Treatment treatment){
        return new ConsultationOdontogram(
                consultation,
                tooth,
                toothFace,
                treatment
        );
    }
}
