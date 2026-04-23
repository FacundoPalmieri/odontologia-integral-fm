package com.odontologiaintegralfm.feature.consultation.core.consultation.model;

import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationStatusType;
import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.*;


/**
 * Entidad que representar la "bitácora" de una consulta
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "consultations_status_history")
public class ConsultationHistory extends AuditableJPA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;

    @Enumerated(EnumType.STRING)
    @Column(name = "consultation_status", nullable = false)
    private ConsultationStatusType consultationStatusType;


    private ConsultationHistory(Consultation consultation, ConsultationStatusType consultationStatusType) {
        this.consultation = consultation;
        this.consultationStatusType = consultationStatusType;
    }

    public static ConsultationHistory build(Consultation consultation, ConsultationStatusType type) {
        return new ConsultationHistory(
                consultation,
                type
        );
    }



}
