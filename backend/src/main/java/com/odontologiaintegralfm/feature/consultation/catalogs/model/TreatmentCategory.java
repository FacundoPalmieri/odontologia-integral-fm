package com.odontologiaintegralfm.feature.consultation.catalogs.model;

import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

/**
 * Entidad que representa la categoría de un tratamiento.
 */
@Entity
@Getter
@Setter
@Table(name = "treatment_categories")
@Where(clause = "enabled = true")
public class TreatmentCategory extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

}
