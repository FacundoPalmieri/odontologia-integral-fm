package com.odontologiaintegralfm.feature.consultation.catalogs.treatment.model;

import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

/**
 * Entidad que representa la categoría de un tratamiento.
 */
@Entity
@Getter
@Setter
@Table(name = "treatment_categories")
@Where(clause = "enabled = true")
@Audited
public class TreatmentCategory extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

}
