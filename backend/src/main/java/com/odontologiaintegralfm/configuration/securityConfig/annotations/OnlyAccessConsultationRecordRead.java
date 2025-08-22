package com.odontologiaintegralfm.configuration.securityConfig.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority('PERMISO_CONSULTATION_RECORD_READ')")
public @interface OnlyAccessConsultationRecordRead {
}
