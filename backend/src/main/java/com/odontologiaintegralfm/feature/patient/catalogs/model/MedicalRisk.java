package com.odontologiaintegralfm.feature.patient.catalogs.model;

import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

/**
 * Entidad para representar los riesgos médicos cargados en la aplicación.
 */

@Audited
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "medical_risk")
public class MedicalRisk extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    private Long parentId;

}
