package com.odontologiaintegralfm.feature.consultation.core.model;

import com.odontologiaintegralfm.feature.consultation.core.enums.ConsultationStatus;
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
    private ConsultationStatus consultationStatus;


    private ConsultationHistory(Consultation consultation, ConsultationStatus consultationStatus, UserSec createdBy, LocalDateTime createAt, boolean enabled ) {
        this.consultation = consultation;
        this.consultationStatus = consultationStatus;
        this.setCreatedBy(createdBy);
        this.setCreatedAt(createAt);
        this.setEnabled(enabled);
    }

    public static ConsultationHistory build(Consultation consultation, UserSec user) {
        return new ConsultationHistory(
                consultation,
                ConsultationStatus.WAITING_ROOM,
                user,
                LocalDateTime.now(),
                true
        );
    }



}
