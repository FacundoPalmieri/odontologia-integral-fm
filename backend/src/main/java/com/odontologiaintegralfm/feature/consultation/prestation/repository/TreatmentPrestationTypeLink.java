package com.odontologiaintegralfm.feature.consultation.prestation.repository;

import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationType;

/**
 * Par (id de tratamiento, prestación) que devuelve {@code IPrestationTypeRepository.findTreatmentLinks} (REQ-039, D-03).
 */
public record TreatmentPrestationTypeLink(Long treatmentId, PrestationType prestationType) {
}
