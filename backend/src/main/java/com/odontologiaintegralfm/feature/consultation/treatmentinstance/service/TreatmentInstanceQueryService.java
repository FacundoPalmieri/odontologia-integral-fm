package com.odontologiaintegralfm.feature.consultation.treatmentinstance.service;

import com.odontologiaintegralfm.feature.consultation.treatmentinstance.model.TreatmentInstance;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.repository.ITreatmentInstanceRepository;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TreatmentInstanceQueryService {

    private final ITreatmentInstanceRepository treatmentInstanceRepository;

    public TreatmentInstanceQueryService(ITreatmentInstanceRepository treatmentInstanceRepository) {
        this.treatmentInstanceRepository = treatmentInstanceRepository;
    }

    /**
     * Devuelve el tratamiento del paciente o 404. El mensaje es el mismo si el id no existe o si es de otro
     * paciente: no se le confirma a quien consulta que el id existe en otro paciente (D-60, ADR-0027).
     */
    public TreatmentInstance findByIdAndPatientId(Long id, Long patientId) {
        return treatmentInstanceRepository.findByIdAndPatientId(id, patientId)
                .orElseThrow(() -> new NotFoundException("exception.treatmentInstanceQueryService.notFound.user", null, "exception.treatmentInstanceQueryService.notFound.log", new Object[]{id, patientId, "TreatmentInstanceQueryService", "findByIdAndPatientId"}, LogLevel.WARN));
    }
}
