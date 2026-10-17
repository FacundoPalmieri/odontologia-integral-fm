package com.odontologiaintegralfm.feature.consultation.prestationinstance.service;


import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrestationInstanceQueryService {

    private final IPrestationInstanceRepository prestationInstanceRepository;

    public PrestationInstanceQueryService(IPrestationInstanceRepository prestationInstanceRepository) {
        this.prestationInstanceRepository = prestationInstanceRepository;
    }

    @Transactional(readOnly = true)
    public PrestationInstance findById(Long id) {
        return prestationInstanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("exception.prestationInstanceQueryService.notFound.user", null, "exception.prestationInstanceQueryService.notFound.log", new Object[]{id, "PrestationInstanceQueryService", "findById"}, LogLevel.ERROR));
    }

    /**
     * Devuelve la prestación del paciente o 404. El mensaje es el mismo si el id no existe o si es de otro
     * paciente (D-60, ADR-0027).
     */
    @Transactional(readOnly = true)
    public PrestationInstance findByIdAndPatientId(Long id, Long patientId) {
        return prestationInstanceRepository.findByIdAndPatientId(id, patientId)
                .orElseThrow(() -> new NotFoundException("exception.prestationInstanceQueryService.notFoundForPatient.user", null, "exception.prestationInstanceQueryService.notFoundForPatient.log", new Object[]{id, patientId, "PrestationInstanceQueryService", "findByIdAndPatientId"}, LogLevel.WARN));
    }
}
