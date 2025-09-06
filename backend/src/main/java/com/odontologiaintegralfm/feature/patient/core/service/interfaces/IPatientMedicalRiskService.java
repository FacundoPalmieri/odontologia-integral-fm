package com.odontologiaintegralfm.feature.patient.core.service.interfaces;

import com.odontologiaintegralfm.feature.patient.core.dto.PatientMedicalRiskRequestDTO;
import com.odontologiaintegralfm.feature.patient.core.dto.PatientMedicalRiskResponseDTO;
import com.odontologiaintegralfm.feature.patient.core.model.PatientMedicalRisk;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;

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
     * @param patientId Paciente
     * @return objeto PatientMedicalRisk
     */
    Set<PatientMedicalRisk> getByPatientIdAndEnabledTrue(Long patientId);


    /**
     * Método para actualizar los riesgos médicos asociados a un paciente.
     * @param patientId Id del paciente
     * @param patientMedicalRiskRequestDTO Riesgos médicos del paciente
     * @return
     */
   Set<PatientMedicalRiskResponseDTO> CreateOrUpdate(Long patientId, Set<PatientMedicalRiskRequestDTO> patientMedicalRiskRequestDTO);

    /**
     * Método para convertir una Set de PatientMedicalRisk en un PatientMedicalRiskResponseDTO
     * @param patientMedicalRisk Set de riesgos médicos de un paciente.
     * @return PatientMedicalRiskResponseDTO
     */
    Set<PatientMedicalRiskResponseDTO> convertToDTO(Set<PatientMedicalRisk> patientMedicalRisk);
}
