package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.time.LocalDate;

/**
 * Entidad para representar los detalles de las consultas
 */

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "consultation_details")
@Audited
public class ConsultationDetail {

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

    @Column(nullable = false)
    private Boolean enabled;

    private LocalDate disabledAt;

}
