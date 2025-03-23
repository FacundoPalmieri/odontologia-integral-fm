package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


/**
 * Entidad que representar el Usuario securizado
 */
@Entity
@Table(name="users")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSec {

    /**Identificador único del usuario.*/
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
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
    @Column(name = "Intentos_Fallidos")
    private int failedLoginAttempts;

    /**Fecha y hora de bloqueo de usuario.*/
    @Column(name = "Fecha_Bloqueo")
    private LocalDateTime locktime;

    /**Fecha y hora de creación del usuario.*/
    @Column(name= "Fecha_Creacion")
    private LocalDateTime creationDateTime;

    /**Fecha y hora de la última actualización del usuario.*/
    @Column(name = "ultima_Actualizacion")
    private LocalDateTime lastUpdateDateTime;

    /**Indica si la cuenta está activada.*/
    @Column(name = "Activa",nullable = false, columnDefinition = "boolean default true")
    private boolean enabled;

    /**Indica si la cuenta no está expirada.*/
    @Column(name = "No_Expirada", nullable = false, columnDefinition = "boolean default true")
    private boolean accountNotExpired;

    /**Indica si la cuenta no está bloqueada.*/
    @Column(name = "No_Bloqueada",nullable = false, columnDefinition = "boolean default true")
    private boolean accountNotLocked;

    /**Indica si las credenciales no están expiradas.*/
    @Column(name = "Credenciales_NoExpiradas", nullable = false, columnDefinition = "boolean default true")
    private boolean credentialNotExpired;

    /**Lista de roles asociados al usuario.
     * Se utiliza Set porque no permite repetidos.
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL) //el eager carga todos los roles
    @JoinTable (name = "user_roles", joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns=@JoinColumn(name = "role_id"))
    private Set<Role> rolesList = new HashSet<>();

    /**Token para restablecimiento de contraseña.*/
    @Column(name="Token_Rest",length = 500)
    private String resetPasswordToken;
}
