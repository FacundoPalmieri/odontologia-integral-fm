package com.odontologiaintegralfm.feature.consultation.core.dto;


import com.odontologiaintegralfm.feature.consultation.core.model.Consultation;

public record ConsultationResponseDTO(
        Long id,
        String patientName,
        String dentistName,
        String consultationStatus) {
    public static ConsultationResponseDTO build(Consultation consultation) {
        return new ConsultationResponseDTO(
                consultation.getId(),
                consultation.getPatient().getPerson().getLastName() + "," + consultation.getPatient().getPerson().getFirstName(),
                consultation.getDentist().getPerson().getLastName() + "," + consultation.getDentist().getPerson().getFirstName(),
                consultation.getStatus().getLabel()
        );

    }
}
