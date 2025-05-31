package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


/**
 * Entidad que representar el Usuario securizado
 */
@Audited
@Entity
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="users")
public class UserSec extends Auditable {

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



    /** Constructor con campos heredados. */
    public UserSec(
            String username,
            String password,
            int failedLoginAttempts,
            LocalDateTime locktime,
            boolean accountNotExpired,
            boolean accountNotLocked,
            boolean credentialNotExpired,
            Set<Role> rolesList,
            String resetPasswordToken,

            //Heredados.
            LocalDateTime createdAt,
            UserSec createdBy,
            LocalDateTime updatedAt,
            UserSec updatedBy,
            boolean enabled,
            LocalDateTime disabledAt,
            UserSec disabledBy
    ) {
        this.username = username;
        this.password = password;
        this.failedLoginAttempts = failedLoginAttempts;
        this.locktime = locktime;
        this.accountNotExpired = accountNotExpired;
        this.accountNotLocked = accountNotLocked;
        this.credentialNotExpired = credentialNotExpired;
        this.rolesList = rolesList;
        this.resetPasswordToken = resetPasswordToken;

        // Heredados
        this.setCreatedAt(createdAt);
        this.setCreatedBy(createdBy);
        this.setUpdatedAt(updatedAt);
        this.setUpdatedBy(updatedBy);
        this.setEnabled(enabled);
        this.setDisabledAt(disabledAt);
        this.setDisabledBy(disabledBy);
    }
}
