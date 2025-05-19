package com.odontologiaintegralfm.service.interfaces;



import com.odontologiaintegralfm.model.MedicalHistory;
import com.odontologiaintegralfm.model.Patient;


public interface IMedicalHistoryService {
    /**
     * Método para crear una historia clínica y persistirla en la base de datos.
     * @param medicalHistory de la persona.
     * @return {@link MedicalHistory}
     */
    MedicalHistory create(MedicalHistory medicalHistory);

    /**
     * Método para obtener la historia clínica de una paciente por su id.
     * @param id del paciente
     * @return MedicalHistory
     */
    MedicalHistory getByPatient(Long id);

    /**
     * Método que construye un objeto {@link MedicalHistory}.
     * @param patient con los datos personales del paciente (incluye ID)
     * @return MedicalHistory con la historia clínica creada.
     */
     MedicalHistory buildMedicalHistory(Patient patient);


}
