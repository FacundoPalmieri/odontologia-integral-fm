package com.odontologiaintegralfm.feature.consultation.core.model;

import com.odontologiaintegralfm.feature.consultation.catalogs.model.Tooth;
import com.odontologiaintegralfm.feature.consultation.catalogs.model.ToothFace;
import com.odontologiaintegralfm.feature.consultation.catalogs.model.Treatment;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * Entidad para representar los detalles de las consultas (Odontograma)
 */

@Entity
@Getter
@Setter
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tooth_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Tooth tooth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tooth_face_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private ToothFace toothFace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Treatment treatment;
}
