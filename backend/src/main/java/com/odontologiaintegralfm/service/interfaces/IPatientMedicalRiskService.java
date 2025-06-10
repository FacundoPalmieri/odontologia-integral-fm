package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.PatientMedicalRiskRequestDTO;
import com.odontologiaintegralfm.dto.PatientMedicalRiskResponseDTO;
import com.odontologiaintegralfm.model.PatientMedicalRisk;
import com.odontologiaintegralfm.model.Patient;

import java.util.Set;

/**
 * @author [Facundo Palmieri]
 */
public interface IPatientMedicalRiskService {

    /**
     * Método para construir un objeto de riesgo médico asociado al paciente.
     * @return
     */
    PatientMedicalRisk build(Long IdMedicalRisk,Patient patient, String observation);

    /**
     *Método para obtener la lista de riesgo clínicos "Habilitados" de un paciente
     * @param patient Paciente
     * @return objeto PatientMedicalRisk
     */
    Set<PatientMedicalRisk> getByPatient(Patient patient);


    /**
     * Método para crear riesgos médicos asociados a un paciente.
     * @param patientMedicalRiskRequestDTO Objeto que contiene el ID y la observación del riesgo escrita por el profesional.
     * @param patient paciente.
     * @return Set<PatientMedicalRiskResponseDTO>
     */
    Set<PatientMedicalRiskResponseDTO> create(Set <PatientMedicalRiskRequestDTO> patientMedicalRiskRequestDTO, Patient patient);

    /**
     * Método para actualizar los riesgos médicos asociados a un paciente.
     * @param patient paciente
     * @param patientMedicalRiskRequestDTO Riesgos médicos del paciente
     * @return
     */
    Set <PatientMedicalRiskResponseDTO> update(Patient patient, Set<PatientMedicalRiskRequestDTO> patientMedicalRiskRequestDTO);

    /**
     * Método para convertir una Set de PatientMedicalRisk en un PatientMedicalRiskResponseDTO
     * @param patientMedicalRisk Set de riesgos médicos de un paciente.
     * @return PatientMedicalRiskResponseDTO
     */
    Set<PatientMedicalRiskResponseDTO> convertToDTO(Set<PatientMedicalRisk> patientMedicalRisk);
}
