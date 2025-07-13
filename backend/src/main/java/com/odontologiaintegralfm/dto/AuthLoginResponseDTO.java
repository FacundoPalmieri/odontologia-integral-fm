package com.odontologiaintegralfm.dto;
import lombok.Builder;
import lombok.Data;
import java.util.Set;


/**
 * DTO  que representa la respuesta de autenticación cuando un usuario inicia sesión en el sistema.
 *
 * Contiene información del usuario autenticado junto con el jwt y el refreshToken
 */
@Data
@Builder
public class AuthLoginResponseDTO {

    private Long idUser;
    private String username;
    private Set<RoleFullResponseDTO> roles; // Lista de Roles
    private String jwt;
    private String refreshToken;
    private PersonResponseDTO person;
}