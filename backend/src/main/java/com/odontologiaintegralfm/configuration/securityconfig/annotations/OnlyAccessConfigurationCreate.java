package com.odontologiaintegralfm.configuration.securityconfig.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) //Destino
@Retention(RetentionPolicy.RUNTIME)             //Permanencia
@PreAuthorize("hasAuthority('PERMISO_CONFIGURATION_CREATE')")
public @interface OnlyAccessConfigurationCreate {
}
