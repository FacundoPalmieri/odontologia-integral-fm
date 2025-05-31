package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.configuration.securityConfig.AuthenticatedUserService;
import com.odontologiaintegralfm.dto.PatientMedicalRiskRequestDTO;
import com.odontologiaintegralfm.dto.PatientMedicalRiskResponseDTO;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.PatientMedicalRisk;
import com.odontologiaintegralfm.model.Patient;
import com.odontologiaintegralfm.repository.IPatientMedicalRiskRepository;
import com.odontologiaintegralfm.service.interfaces.IPatientMedicalRiskService;
import com.odontologiaintegralfm.service.interfaces.IMedicalRiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author [Facundo Palmieri]
 */
@Service
public class PatientMedicalRiskService implements IPatientMedicalRiskService {
    @Autowired
    private IPatientMedicalRiskRepository patientMedicalRiskRepository;

    @Autowired
    private IMedicalRiskService medicalRiskService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;


    /**
     * Método para crear un riesgo médico asociado al paciente.
     * @return
     */
    public PatientMedicalRisk build(Long IdMedicalRisk, Patient patient, String observation) {
        PatientMedicalRisk medicalHistoryRisk = new PatientMedicalRisk();
        medicalHistoryRisk.setPatient(patient);
        medicalHistoryRisk.setMedicalRisk(medicalRiskService.getById(IdMedicalRisk));
        medicalHistoryRisk.setObservation(observation);
        medicalHistoryRisk.setCreatedAt(LocalDateTime.now());
        medicalHistoryRisk.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        medicalHistoryRisk.setEnabled(true);
        return  medicalHistoryRisk;
    }

    /**
     * Método para obtener la lista de riesgo clínicos "Habilitados" por medio del ID de la historía clínica.
     *
     * @param patient  paciente
     * @return objeto PatientMedicalRisk
     */
    @Override
    public Set<PatientMedicalRisk> getByPatient(Patient patient) {
        try{
            return patientMedicalRiskRepository.findByPatient(patient);
        }catch (DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "PatientMedicalRiskService", patient.getId(), patient.getPerson().getLastName() + "," + patient.getPerson().getFirstName(), "getById");

        }
    }

    /**
     * Método para crear riesgos médicos asociados a un paciente.
     * @param patientMedicalRiskRequestDTO Objeto que contiene el ID y la observación del riesgo escrita por el profesional.
     * @param patient paciente.
     * @return Set<PatientMedicalRiskResponseDTO>
     */
    @Override
    @Transactional
    public Set<PatientMedicalRiskResponseDTO> create(Set<PatientMedicalRiskRequestDTO> patientMedicalRiskRequestDTO, Patient patient) {
        try{
            Set <PatientMedicalRiskResponseDTO> patientMedicalRisks = new HashSet<>();
            for(PatientMedicalRiskRequestDTO dto : patientMedicalRiskRequestDTO){
                PatientMedicalRisk patientMedicalRisk = build(dto.medicalRiskId(), patient, dto.observation());
                patientMedicalRiskRepository.save(patientMedicalRisk);
                patientMedicalRisks.add(new PatientMedicalRiskResponseDTO(
                        patientMedicalRisk.getId(),
                        patientMedicalRisk.getMedicalRisk().getName(),
                        patientMedicalRisk.getObservation()
                ));
            }
            return patientMedicalRisks;
        }catch (DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "PatientMedicalRiskService", patient.getId(),": <- ID Paciente", "create");
        }
    }

    /**
     * Método para actualizar los riesgos médicos asociados a un paciente.
     * Si el ID del riesgo médico existe: Se actualiza la observación
     * si el ID del riesgo médico no existe :
     * <ul>
     *     <li>
     *         Se verifica si existe en el DTO pero no en la base y se procede a crear el riesgo médico.
     *         Se verifica Si el ID existe en la base y no en el DTO se da la baja lógica.
     *     </li>
     * </ul>
     *
     *
     * @param patient
     * @param patientMedicalRiskRequestDTO
     * @return
     */
    @Override
    public Set<PatientMedicalRiskResponseDTO> update(Patient patient, Set<PatientMedicalRiskRequestDTO> patientMedicalRiskRequestDTO) {

        //Obtiene los riesgos médicos de un paciente.
        Set<PatientMedicalRisk> currentRisks = getByPatient(patient);

        //Compara si el ID del riesgo del DTO es igual a la BD. Si coincide, actualiza campos.
        for(PatientMedicalRiskRequestDTO dto : patientMedicalRiskRequestDTO){
            boolean exists = false;
            for(PatientMedicalRisk currentRisk : currentRisks){
                if(dto.medicalRiskId().equals(currentRisk.getMedicalRisk().getId())){
                    currentRisk.setObservation(dto.observation());
                    currentRisk.setUpdatedAt(LocalDateTime.now());
                    currentRisk.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
                    exists = true;
                    break;
                }
            }

            //Si no coincide, crear el nuevo riesgo.
            if(!exists){
                PatientMedicalRisk patientMedicalRisk = build(dto.medicalRiskId(),patient,dto.observation());
                patientMedicalRiskRepository.save(patientMedicalRisk);
            }

        }

        //Realiza una nueva comparación para deshabilitar los riesgos de la BD que no están en el DTO.
        for(PatientMedicalRisk risk : currentRisks){
            boolean exists = false;
            for(PatientMedicalRiskRequestDTO dto: patientMedicalRiskRequestDTO){
                if(dto.medicalRiskId().equals(risk.getMedicalRisk().getId())){
                    exists = true;
                    break;
                }
            }
            if(!exists){
                risk.setEnabled(false);
                risk.setUpdatedAt(LocalDateTime.now());
                risk.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
                risk.setDisabledBy(authenticatedUserService.getAuthenticatedUser());
                risk.setDisabledAt(LocalDateTime.now());
                patientMedicalRiskRepository.save(risk);
            }
        }

        //Se realiza una nueva búsqueda actualizada desde la Base para retornar.
        Set<PatientMedicalRisk> medicalHistoryRisk = getByPatient(patient);

        return medicalHistoryRisk.stream()
                .map(medicalHistoryRiskResponse -> new PatientMedicalRiskResponseDTO(
                        medicalHistoryRiskResponse.getMedicalRisk().getId(),
                        medicalHistoryRiskResponse.getMedicalRisk().getName(),
                        medicalHistoryRiskResponse.getObservation()))
                .collect(Collectors.toSet());
    }

    /**
     * Método para convertir una Set de PatientMedicalRisk en un PatientMedicalRiskResponseDTO
     *
     * @param patientMedicalRisk Set de riesgos médicos de un paciente.
     * @return PatientMedicalRiskResponseDTO
     */
    @Override
    public Set<PatientMedicalRiskResponseDTO> convertToDTO(Set<PatientMedicalRisk> patientMedicalRisk) {
        try{
            return patientMedicalRisk.stream()
                    .map(obj -> new PatientMedicalRiskResponseDTO(obj.getId(), obj.getMedicalRisk().getName(), obj.getObservation()))
                    .collect(Collectors.toSet());
        }catch (DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "PatientMedicalRiskService", null,null, "convertToDTO");
        }
    }
}
