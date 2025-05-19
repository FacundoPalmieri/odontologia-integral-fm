package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import java.time.LocalDateTime;


/**
 * Entidad para representar los riesgos médicos de un paciente.
 */
@Entity
@Audited
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "medical_histories_risks")
public class MedicalHistoryRisk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = MedicalHistory.class)
    @JoinColumn(name = "medical_history_id",nullable = false, updatable = false)
    private MedicalHistory medicalHistory;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = MedicalRisk.class)
    @JoinColumn(name = "medical_risk_id",nullable = false, updatable = false)
    private MedicalRisk medicalRisk;

    @Size(max = 500)
    private String observation;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = UserSec.class)
    @JoinColumn(name = "created_by_id",nullable = false, updatable = false)
    private UserSec createdBy;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = UserSec.class)
    @JoinColumn(name = "updated_by_id")
    private UserSec updatedBy;

    private Boolean enabled;

    private LocalDateTime disabledAt;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = UserSec.class)
    @JoinColumn(name = "disabled_by_id")
    private UserSec disabledBy;

}
