package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.configuration.securityConfig.AuthenticatedUserService;
import com.odontologiaintegralfm.dto.MedicalHistoryRiskRequestDTO;
import com.odontologiaintegralfm.dto.MedicalHistoryRiskResponseDTO;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.MedicalHistory;
import com.odontologiaintegralfm.model.MedicalHistoryRisk;
import com.odontologiaintegralfm.model.Patient;
import com.odontologiaintegralfm.repository.IMedicalHistoryRiskRepository;
import com.odontologiaintegralfm.service.interfaces.IMedicalHistoryRiskService;
import com.odontologiaintegralfm.service.interfaces.IMedicalHistoryService;
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
public class MedicalHistoryRiskService implements IMedicalHistoryRiskService {
    @Autowired
    private IMedicalHistoryRiskRepository medicalHistoryRiskRepository;

    @Autowired
    private IMedicalRiskService medicalRiskService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private IMedicalHistoryService medicalHistoryService;


    /**
     * Método para crear un riesgo médico asociado al paciente.
     * @return
     */
    @Override
    public MedicalHistoryRisk buildMedicalHistoryRisk(MedicalHistory medicalHistory, Long IdMedicalRisk, String observation) {
        MedicalHistoryRisk medicalHistoryRisk = new MedicalHistoryRisk();
        medicalHistoryRisk.setMedicalHistory(medicalHistory);
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
     * @param idHistoryRisk id de la historia clínica.
     * @return objeto MedicalHistoryRisk
     */
    @Override
    public Set<MedicalHistoryRisk> getByIdHistoryRisk(Long idHistoryRisk) {
        try{
            return medicalHistoryRiskRepository.findByMedicalHistoryIdAndEnabledTrue(idHistoryRisk);
        }catch (DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "MedicalHistoryRiskService", idHistoryRisk, null, "getById");

        }
    }

    /**
     * Método para crear riesgos médicos asociados a un paciente.
     *
     * @param medicalHistoryRiskRequestDTO Objeto que contiene el ID y la observación del riesgo escrita por el profesional
     * @param medicalHistory               La historia clínica del paciente
     * @return Set<MedicalHistoryRiskResponseDTO>
     */
    @Override
    @Transactional
    public Set<MedicalHistoryRiskResponseDTO> createMedicalHistoryRisk(Set<MedicalHistoryRiskRequestDTO> medicalHistoryRiskRequestDTO, MedicalHistory medicalHistory) {
        try{
            Set <MedicalHistoryRiskResponseDTO> medicalHistoryRisks = new HashSet<>();
            for(MedicalHistoryRiskRequestDTO dto : medicalHistoryRiskRequestDTO){
                MedicalHistoryRisk medicalHistoryRisk = buildMedicalHistoryRisk(medicalHistory,dto.medicalRiskId(),dto.observation());
                medicalHistoryRiskRepository.save(medicalHistoryRisk);
                medicalHistoryRisks.add(new MedicalHistoryRiskResponseDTO(
                        medicalHistoryRisk.getId(),
                        medicalHistoryRisk.getMedicalRisk().getName(),
                        medicalHistoryRisk.getObservation()
                ));
            }
            return medicalHistoryRisks;
        }catch (DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "MedicalHistoryRiskService", medicalHistory.getId(),": <- ID Historia Clínica", "createMedicalHistoryRisk");

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
     * @param medicalHistoryRiskRequestDTO
     * @return
     */
    @Override
    public Set<MedicalHistoryRiskResponseDTO> updatePatientMedicalRisk(Patient patient, Set<MedicalHistoryRiskRequestDTO> medicalHistoryRiskRequestDTO) {

        //Obtiene la historía clínica del paciente
        MedicalHistory medicalHistory = medicalHistoryService.getByPatient(patient.getId());


        //Obtiene los riesgos por medio de la historia clínica.
        Set<MedicalHistoryRisk> currentRisks = getByIdHistoryRisk(medicalHistory.getId());

        //Compara si el ID del riesgo del DTO es igual a la BD. Si coincide, actualiza campos.
        for(MedicalHistoryRiskRequestDTO dto : medicalHistoryRiskRequestDTO){
            boolean exists = false;
            for(MedicalHistoryRisk medicalHistoryRisk : currentRisks){
                if(dto.medicalRiskId().equals(medicalHistoryRisk.getMedicalRisk().getId())){
                    medicalHistoryRisk.setObservation(dto.observation());
                    medicalHistoryRisk.setUpdatedAt(LocalDateTime.now());
                    medicalHistoryRisk.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
                    exists = true;
                    break;
                }
            }

            //Si no coincide, crear el nuevo riesgo.
            if(!exists){
                MedicalHistoryRisk medicalHistoryRisk = buildMedicalHistoryRisk(medicalHistory,dto.medicalRiskId(),dto.observation());
                medicalHistoryRiskRepository.save(medicalHistoryRisk);
            }

        }

        //Realiza una nueva comparación para deshabilitar los riesgos de la BD que no están en el DTO.
        for(MedicalHistoryRisk risk : currentRisks){
            boolean exists = false;
            for(MedicalHistoryRiskRequestDTO dto: medicalHistoryRiskRequestDTO){
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
                medicalHistoryRiskRepository.save(risk);
            }
        }

        //Se realiza una nueva búsqueda actualizada desde la Base para retoran.
        Set<MedicalHistoryRisk> medicalHistoryRisk = getByIdHistoryRisk(medicalHistory.getId());

        return medicalHistoryRisk.stream()
                .map(medicalHistoryRiskResponse -> new MedicalHistoryRiskResponseDTO(
                        medicalHistoryRiskResponse.getMedicalRisk().getId(),
                        medicalHistoryRiskResponse.getMedicalRisk().getName(),
                        medicalHistoryRiskResponse.getObservation()))
                .collect(Collectors.toSet());
    }
}
