package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Entidad que representar el Usuario securizado
 */
@Audited
@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="users")
public class UserSec {

    /**Identificador único del usuario.*/
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    /**Nombre de usuario representado por el email.*/
    @Column(unique = true)
    @NotBlank(message = "El nombre de usuario no puede estar vacío.")
    @Size(max = 50, message = "El nombre de usuario no puede tener más de 50 caracteres.")
    private String username;

    /**Contraseña del usuario.*/
    @Column(nullable = false)
    @NotBlank(message = "La contraseña no puede estar vacía.")
    private String password;

    /**Número de intentos de inicio de sesión fallido.*/
    private int failedLoginAttempts;

    /**Fecha y hora de bloqueo de usuario.*/
    private LocalDateTime locktime;

    /**Fecha y hora de creación del usuario.*/
    private LocalDateTime creationDateTime;

    /**Fecha y hora de la última actualización del usuario.*/
    private LocalDateTime lastUpdateDateTime;

    /**Indica si la cuenta está activada.*/
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean enabled;

    /**Indica si la cuenta no está expirada.*/
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean accountNotExpired;

    /**Indica si la cuenta no está bloqueada.*/
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean accountNotLocked;

    /**Indica si las credenciales no están expiradas.*/
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean credentialNotExpired;

    /**Lista de roles asociados al usuario.
     * Se utiliza Set porque no permite repetidos.
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL) //el eager carga todos los roles
    @JoinTable (name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns=@JoinColumn(name = "role_id"))
    private Set<Role> rolesList = new HashSet<>();

    /**Token para restablecimiento de contraseña.*/
    @Column(length = 500)
    private String resetPasswordToken;

}
