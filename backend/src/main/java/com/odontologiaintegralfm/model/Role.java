package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

import java.util.HashSet;
import java.util.Set;


/**
 * Entidad que representa un rol de usuario.
 */
@Audited
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "roles")
public class Role {

    /**Identificador único del rol.*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**Nombre del rol.*/
    @Column(length = 50, unique = true)
    private String role;


    /**Lista de permisos asociados al rol.
     * Se utiliza Set porque no permite repetidos.
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable (name = "roles_permissions",
                joinColumns = @JoinColumn(name = "role_id"),
                inverseJoinColumns=@JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissionsList = new HashSet<>();
}
