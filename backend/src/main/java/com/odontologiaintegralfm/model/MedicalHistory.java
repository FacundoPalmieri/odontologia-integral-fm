package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad para representar las historias clínicas.
 */
@Audited
@Entity
@Data
@Table (name = "medical_histories")
public class MedicalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, targetEntity = Patient.class)
    @JoinColumn(name = "patient_id",nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private LocalDate dateTime;

    @ManyToMany(fetch = FetchType.LAZY, targetEntity = MedicalRisk.class)
    @JoinTable(
            name = "medical_histories_medical_risk",
            joinColumns = @JoinColumn(name = "medical_histories_id"),
            inverseJoinColumns = @JoinColumn(name = "medical_risk_id")
    )
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private Set<MedicalRisk> medicalRisks = new HashSet<>();

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = UserSec.class)
    @JoinColumn(name = "update_by_id")
    private Long updatedBy;

    private Boolean enabled;

    private LocalDateTime disabledAt;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = UserSec.class)
    @JoinColumn(name = "disabled_by_id")
    private UserSec disabledBy;

}
