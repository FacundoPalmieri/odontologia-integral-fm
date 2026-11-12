package com.odontologiaintegralfm.feature.dentist.service;

import com.odontologiaintegralfm.feature.dentist.model.DentistSpecialty;
import com.odontologiaintegralfm.feature.dentist.model.Dentist;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ForbiddenException;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Regla de especialidad compartida por los catálogos de {@code Treatment} y de {@code PrestationType} (D-61, ADR-0023).
 */
@Service
public class DentistSpecialtyDomainService {

    /**
     * Rechaza (403) si la especialidad del odontólogo no está entre las permitidas. Un catálogo sin especialidades
     * cargadas (vacío o nulo) tampoco deja pasar a nadie (D-12), y un odontólogo sin especialidad no pasa.
     *
     * @param label texto visible del tratamiento o la prestación, para el mensaje al usuario
     */
    public void validateAllowed(Dentist dentist, Set<DentistSpecialty> allowedSpecialties, String label) {
        DentistSpecialty specialty = dentist.getDentistSpecialty();
        if (allowedSpecialties == null || allowedSpecialties.isEmpty() || specialty == null || !allowedSpecialties.contains(specialty)) {
            throw new ForbiddenException("exception.dentistSpecialtyDomainService.validateAllowed.user", new Object[]{label}, "exception.dentistSpecialtyDomainService.validateAllowed.log", new Object[]{specialty != null ? specialty.getId() : null, label, "DentistSpecialtyDomainService", "validateAllowed"}, LogLevel.ERROR);
        }
    }
}
