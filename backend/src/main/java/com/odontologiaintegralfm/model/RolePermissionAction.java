package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

/**
 * Entidad que funciona para relacionar un Rol con las acciones que posee un permiso.
 */

@Entity
@Audited
@Getter
@Setter
@Table(
        name = "roles_permissions_actions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "permission_id","action_id"})
)
public class RolePermissionAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    private Action action;

}
