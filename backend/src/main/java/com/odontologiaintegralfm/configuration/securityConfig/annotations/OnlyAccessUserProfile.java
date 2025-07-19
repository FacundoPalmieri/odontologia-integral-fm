package com.odontologiaintegralfm.configuration.securityConfig.annotations;

import com.odontologiaintegralfm.configuration.securityConfig.AuthenticatedUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para permitir acceso al perfil solo del usuario auténticado y al admin como lectura.
 */

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("#id == @authenticatedUserService.authenticatedUser.id or hasAuthority('PERMISO_CONFIGURATION_READ')")
public @interface OnlyAccessUserProfile {
}
