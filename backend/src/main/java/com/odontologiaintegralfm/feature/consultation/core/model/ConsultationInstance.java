package com.odontologiaintegralfm.feature.consultation.core.model;

import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

/**
 * Entidad que representa una instancia de consulta.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "consultations_instances")
@Where(clause = "enabled = true")
@Audited
public class ConsultationInstance extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @Lob
    private String observation;

    private ConsultationInstance(Consultation consultation, String observation) {
        this.consultation = consultation;
        this.observation = observation;
    }

    public static ConsultationInstance build(Consultation consultation, String observation) {
        return new ConsultationInstance(consultation, observation);
    }
}

