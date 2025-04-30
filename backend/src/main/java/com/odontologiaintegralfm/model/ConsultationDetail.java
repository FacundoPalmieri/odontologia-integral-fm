package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Entidad para representar los detalles de las consultas
 */

@Entity
@Data
@Table(name = "consultation_details")
public class ConsultationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tooth_id")
    private Tooth tooth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tooth_face_id")
    private ToothFace toothFace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_id")
    private Treatment treatment;

    @Column(nullable = false)
    private Boolean enabled;

    private LocalDate disabledAt;

}
