package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.MedicalHistoryRiskRequestDTO;
import com.odontologiaintegralfm.dto.MedicalHistoryRiskResponseDTO;
import com.odontologiaintegralfm.model.MedicalHistory;
import com.odontologiaintegralfm.model.MedicalHistoryRisk;
import com.odontologiaintegralfm.model.Patient;

import java.util.Set;

/**
 * @author [Facundo Palmieri]
 */
public interface IMedicalHistoryRiskService {

    /**
     * Método para construir un objeto de riesgo médico asociado al paciente.
     * @return
     */
    MedicalHistoryRisk buildMedicalHistoryRisk(MedicalHistory medicalHistory, Long IdMedicalRisk, String observation);

    /**
     *Método para obtener la lista de riesgo clínicos "Habilitados" por medio del ID de la historía clínica.
     *  @param idHistoryRisk id de la historia clínica.
     * @return objeto MedicalHistoryRisk
     */
    Set<MedicalHistoryRisk> getByIdHistoryRisk(Long idHistoryRisk);


    /**
     * Método para crear riesgos médicos asociados a un paciente.
     * @param medicalHistoryRiskRequestDTO Objeto que contiene el ID y la observación del riesgo escrita por el profesional
     * @param medicalHistory La historia clínica del paciente
     * @return Set<MedicalHistoryRiskResponseDTO>
     */
    Set<MedicalHistoryRiskResponseDTO> createMedicalHistoryRisk(Set <MedicalHistoryRiskRequestDTO> medicalHistoryRiskRequestDTO, MedicalHistory medicalHistory);

    /**
     * Método para actualizar los riesgos médicos asociados a un paciente.
     * @param patient
     * @param medicalHistoryRiskRequestDTO
     * @return
     */
    Set <MedicalHistoryRiskResponseDTO> updatePatientMedicalRisk(Patient patient, Set<MedicalHistoryRiskRequestDTO> medicalHistoryRiskRequestDTO);

}
