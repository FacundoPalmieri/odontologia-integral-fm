package com.odontologiaintegralfm.feature.consultation.consultationinstance.service;

import com.odontologiaintegralfm.feature.consultation.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Reglas de dominio compartidas por los use cases de {@code consultationinstance} (ADR-0023).
 */
@Service
public class ConsultationInstanceDomainService {

    /**
     * Indica si el usuario es el odontólogo asignado a la consulta (REQ-039, T-03). Recibe el usuario porque cada
     * llamador ya lo tiene. No lanza nada: cada llamador arma su propio 404.
     */
    public boolean isAssignedDentist(Consultation consultation, UserSec user) {
        Long dentistPersonId = consultation.getDentist().getPerson().getId();
        return Objects.equals(dentistPersonId, user.getPerson().getId());
    }
}
