package com.odontologiaintegralfm.feature.patient.core.service.implement;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.patient.core.dto.PatientMedicalRiskRequestDTO;
import com.odontologiaintegralfm.feature.patient.core.dto.PatientMedicalRiskResponseDTO;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.feature.patient.core.model.PatientMedicalRisk;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
import com.odontologiaintegralfm.feature.patient.core.repository.IPatientMedicalRiskRepository;
import com.odontologiaintegralfm.feature.patient.core.repository.IPatientRepository;
import com.odontologiaintegralfm.feature.patient.core.service.interfaces.IPatientMedicalRiskService;
import com.odontologiaintegralfm.feature.patient.catalogs.service.interfaces.IMedicalRiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.time.LocalDateTime;
import java.util.Collections;
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

    @Autowired
    private IPatientRepository patientRepository;


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
     * @param patientId  paciente
     * @return objeto PatientMedicalRisk
     */
    @Override
    public Set<PatientMedicalRisk> getByPatientIdAndEnabledTrue(Long patientId) {
        try{
            return patientMedicalRiskRepository.findByPatientIdAndEnabledTrue(patientId);
        }catch (DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "PatientMedicalRiskService", patientId, null, "getById");

        }
    }


    private Set<PatientMedicalRisk> getByPatientId(Long patientId) {
        try{
            return patientMedicalRiskRepository.findByPatientId(patientId);
        }catch (DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "PatientMedicalRiskService", patientId, null, "getById");

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
     * @param patientId
     * @param patientMedicalRiskRequestDTO
     * @return
     */
    @Override
    public  Set<PatientMedicalRiskResponseDTO> CreateOrUpdate(Long patientId, Set<PatientMedicalRiskRequestDTO> patientMedicalRiskRequestDTO) {

        //Obtiene los riesgos médicos de un paciente.
        Set<PatientMedicalRisk> currentRisks = getByPatientId(patientId);

        //Obtiene paciente
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(()-> new NotFoundException("exception.patientNotFound.user",null, "exception.patientNotFound.log", new Object[]{patientId, "PatientMedicalRiskService", "CreateOrUpdate"}, LogLevel.ERROR ));


        //1. Si la nueva lista no tiene ningún riesgo médico y poseé riesgos previos, se deshabilitan todos.
        if (patientMedicalRiskRequestDTO == null || patientMedicalRiskRequestDTO.isEmpty()) {
            if(currentRisks != null){
                for (PatientMedicalRisk risk : currentRisks) {
                    if (risk.isEnabled()) {
                        risk.setEnabled(false);
                        risk.setUpdatedAt(LocalDateTime.now());
                        risk.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
                        risk.setDisabledBy(authenticatedUserService.getAuthenticatedUser());
                        risk.setDisabledAt(LocalDateTime.now());
                        patientMedicalRiskRepository.save(risk);
                    }
                }

                // Retorno conjunto vacío porque ya no hay riesgos activos
                return Collections.emptySet();
            }
        }


        /*2. Si la lista tiene información y existen riesgos previos:
             - Compara si el ID del riesgo del DTO es igual a la BD:
              Si existe:
                - Evalúa si está habilitado. En ese caso actualiza campos.
                - Sino lo vuelve a hablitar.

             Sino existe:
             -Crea el nuevo riesgo.
         */

        if( ! currentRisks.isEmpty() ) {
            for (PatientMedicalRiskRequestDTO dto : patientMedicalRiskRequestDTO) {
                boolean exists = false;
                for (PatientMedicalRisk currentRisk : currentRisks) {
                    if (dto.medicalRiskId().equals(currentRisk.getMedicalRisk().getId())) {
                        if(currentRisk.isEnabled()) {
                            currentRisk.setObservation(dto.observation());
                            currentRisk.setUpdatedAt(LocalDateTime.now());
                            currentRisk.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
                            exists = true;
                            break;
                        }else{
                            currentRisk.setEnabled(true);
                            currentRisk.setDisabledAt(null);
                            currentRisk.setDisabledBy(null);
                            currentRisk.setObservation(dto.observation());
                            exists = true;
                            break;
                        }

                    }
                }

                //Si no coincide, crear el nuevo riesgo.
                if (!exists) {
                    PatientMedicalRisk patientMedicalRisk = build(dto.medicalRiskId(), patient, dto.observation());
                    patientMedicalRiskRepository.save(patientMedicalRisk);
                }

            }


            //Realiza una nueva comparación para deshabilitar los riesgos de la BD que no están en el DTO.
            for (PatientMedicalRisk risk : currentRisks) {
                boolean exists = false;
                for (PatientMedicalRiskRequestDTO dto : patientMedicalRiskRequestDTO) {
                    if (dto.medicalRiskId().equals(risk.getMedicalRisk().getId())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    risk.setEnabled(false);
                    risk.setUpdatedAt(LocalDateTime.now());
                    risk.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());
                    risk.setDisabledBy(authenticatedUserService.getAuthenticatedUser());
                    risk.setDisabledAt(LocalDateTime.now());
                    patientMedicalRiskRepository.save(risk);
                }
            }

            //Se realiza una nueva búsqueda actualizada desde la Base para retornar.
            Set<PatientMedicalRisk> medicalHistoryRisk = getByPatientIdAndEnabledTrue(patientId);


            return medicalHistoryRisk.stream()
                    .map(medicalHistoryRiskResponse -> new PatientMedicalRiskResponseDTO(
                            medicalHistoryRiskResponse.getMedicalRisk().getId(),
                            medicalHistoryRiskResponse.getMedicalRisk().getName(),
                            medicalHistoryRiskResponse.getObservation()))
                    .collect(Collectors.toSet());



        }else{
            //3. Si la lista tiene información, pero no hay riesgos previos.
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
                throw new DataBaseException(e, "PatientMedicalRiskService", patient.getId()," <- ID Paciente", "CreateOrUpdate");
            }
        }
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
                    .map(obj -> new PatientMedicalRiskResponseDTO(obj.getMedicalRisk().getId(), obj.getMedicalRisk().getName(), obj.getObservation()))
                    .collect(Collectors.toSet());
        }catch (DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "PatientMedicalRiskService", null,null, "convertToDTO");
        }
    }
}
