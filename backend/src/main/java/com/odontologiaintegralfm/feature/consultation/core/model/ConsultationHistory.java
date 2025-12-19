package com.odontologiaintegralfm.feature.consultation.core.model;

import com.odontologiaintegralfm.feature.consultation.core.enums.ConsultationStatusType;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representar la "bitácora" de una consulta
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "consultations_status_history")
public class ConsultationHistory extends Auditable {
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
