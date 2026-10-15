package com.odontologiaintegralfm.feature.dentist.catalogs.service;

import com.odontologiaintegralfm.feature.dentist.catalogs.model.DentistSpecialty;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
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
     * Rechaza (403) si el catálogo restringe por especialidad y la del odontólogo no está entre las permitidas.
     * Un catálogo sin especialidades cargadas no restringe a nadie; un odontólogo sin especialidad con un catálogo
     * restrictivo tampoco pasa.
     *
     * @param label texto visible del tratamiento o la prestación, para el mensaje al usuario
     */
    public void validateAllowed(Dentist dentist, Set<DentistSpecialty> allowedSpecialties, String label) {
        if (allowedSpecialties == null || allowedSpecialties.isEmpty()) {
            return;
        }
        DentistSpecialty specialty = dentist.getDentistSpecialty();
        if (specialty == null || !allowedSpecialties.contains(specialty)) {
            throw new ForbiddenException("exception.dentistSpecialtyDomainService.validateAllowed.user", new Object[]{label}, "exception.dentistSpecialtyDomainService.validateAllowed.log", new Object[]{specialty != null ? specialty.getId() : null, label, "DentistSpecialtyDomainService", "validateAllowed"}, LogLevel.ERROR);
        }
    }
}
