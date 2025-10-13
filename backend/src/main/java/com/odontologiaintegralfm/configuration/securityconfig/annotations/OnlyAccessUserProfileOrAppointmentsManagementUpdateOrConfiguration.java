package com.odontologiaintegralfm.configuration.securityconfig.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("" +
        "#id == @authenticatedUserService.authenticatedUser.id or " +
        "#id == @authenticatedUserService.authenticatedUser.person.id or " +
        "hasAuthority('PERMISO_CONFIGURATION_UPDATE') or hasAuthority('PERMISO_APPOINTMENT_MANAGEMENT_UPDATE')")
public @interface OnlyAccessUserProfileOrAppointmentsManagementUpdateOrConfiguration {
}
