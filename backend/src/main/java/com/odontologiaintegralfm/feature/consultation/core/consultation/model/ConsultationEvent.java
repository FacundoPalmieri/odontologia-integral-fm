package com.odontologiaintegralfm.feature.consultation.core.consultation.model;

import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationEventType;
import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registra eventos relacionados a consultas odontológicas.
 * Ej: Correcciones de estado u odontogramas.
 * No puede volver a un estado anterior, luego del estado Finished.
 */


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "consultation_events")
public class ConsultationEvent extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsultationEventType eventType;

    @Lob
    private String observation;


    private ConsultationEvent(Consultation consultation, ConsultationEventType eventType, String observation){
        this.consultation = consultation;
        this.eventType = eventType;
        this.observation = observation;
    }


    public static ConsultationEvent build(Consultation consultation, ConsultationEventType eventType, String observation) {
        return new ConsultationEvent(
                consultation,
                eventType,
                observation
        );

    }
}


