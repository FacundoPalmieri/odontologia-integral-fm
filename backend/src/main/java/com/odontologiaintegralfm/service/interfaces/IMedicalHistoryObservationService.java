package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.model.MedicalHistoryObservation;


public interface IMedicalHistoryObservationService {

    /**
     * Método para crear una observación en la historia clínica.
     * @param medicalHistoryObservation con toda la información de la observación.
     * @return MedicalHistoryObservation con el objeto creado.
     */
    MedicalHistoryObservation create(MedicalHistoryObservation medicalHistoryObservation);
}
