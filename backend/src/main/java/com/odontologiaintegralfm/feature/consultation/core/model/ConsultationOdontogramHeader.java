package com.odontologiaintegralfm.feature.consultation.core.model;

import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Entidad que representa el encabezado de un Odontograma. Engloba todos los {@Link ConsultationOdontogramDetail} de una misma consulta.
 */

@Entity
@Table(name = "consultation_odontogram_headers")
@Audited
@Getter
@Setter
@NoArgsConstructor
public class ConsultationOdontogramHeader extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @Lob
    private String observation;



    private ConsultationOdontogramHeader(Consultation consultation, String observation) {
        this.consultation = consultation;
        this.observation = observation;
    }

    public static ConsultationOdontogramHeader build(Consultation consultation, String observation) {
        return new ConsultationOdontogramHeader(consultation, observation);

    }
}