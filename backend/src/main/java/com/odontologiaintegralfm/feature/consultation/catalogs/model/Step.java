package com.odontologiaintegralfm.feature.consultation.catalogs.model;

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
 * Entidad que representa un paso dentro de una prestación.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "steps")
@Where(clause = "enabled = true")
@Audited
public class Step extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;
}

