package com.odontologiaintegralfm.configuration.securityconfig.annotations;

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
@PreAuthorize("hasAuthority('PERMISO_CONFIGURATION_CREATE') or hasAuthority('PERMISO_APPOINTEMENTS_MANAGEMENT_CREATE')")
public @interface OnlyAccessConfigurationReadOrAppointmentsManagement {
}
