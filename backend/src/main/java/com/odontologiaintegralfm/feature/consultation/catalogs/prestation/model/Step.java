package com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model;


import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;



@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Where(clause = "enabled = true")
@Audited
@Table(name = "steps")
public class Step extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    @EqualsAndHashCode.Include
    private String name;


}
