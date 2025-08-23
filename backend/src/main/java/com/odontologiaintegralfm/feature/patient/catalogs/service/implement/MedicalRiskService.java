package com.odontologiaintegralfm.feature.patient.catalogs.service.implement;

import com.odontologiaintegralfm.feature.patient.catalogs.dto.MedicalRiskResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.feature.patient.catalogs.model.MedicalRisk;
import com.odontologiaintegralfm.feature.patient.catalogs.repository.IMedicalRiskRepository;
import com.odontologiaintegralfm.feature.patient.catalogs.service.interfaces.IMedicalRiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MedicalRiskService implements IMedicalRiskService {
    @Autowired
    private IMedicalRiskRepository medicalRiskRepository;


    /**
     * Método para recuperar Set de todos los Riesgos médicos "Habilitados".
     *
     * @return Set de objetos {@link MedicalRisk} habilitados.
     */
    @Override
    public Response<Set<MedicalRiskResponseDTO>> getAll() {
        try{
            Set<MedicalRisk> medicalRisks = medicalRiskRepository.findAllByEnabledTrue();
            Set<MedicalRiskResponseDTO> medicalRiskResponseDTOs = medicalRisks.stream()
                    .map(medicalRisk -> new MedicalRiskResponseDTO(medicalRisk.getId(), medicalRisk.getName()))
                    .collect(Collectors.toSet());

            return new Response<>(true, null, medicalRiskResponseDTOs);

        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "MedicalRiskService",null, null, "getAll");
        }
    }

    /**
     * Método para recuperarRiesgos médicos "Habilitados" de acuerdo a los Ids recibidos.
     *
     * @param id de Riesgos médicos.
     * @return Set de objetos {@link MedicalRisk} habilitados.
     */
    @Override
    public MedicalRisk getById(Long id) {
        try{
            return medicalRiskRepository.findByIdAndEnabledTrue(id);

        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "MedicalRiskService",null, null, "getByIds");
        }
    }
}
