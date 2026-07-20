package com.odontologiaintegralfm.feature.consultation.core.consultation.service;

import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationHistoryResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationRepository;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.repository.IConsultationInstanceRepository;
import com.odontologiaintegralfm.feature.patient.core.service.interfaces.IPatientService;
import com.odontologiaintegralfm.shared.dto.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GetConsultationHistoryUseCase {

    private final IPatientService patientService;
    private final IConsultationRepository consultationRepository;
    private final IConsultationInstanceRepository consultationInstanceRepository;

    public GetConsultationHistoryUseCase(IPatientService patientService,
                                          IConsultationRepository consultationRepository,
                                          IConsultationInstanceRepository consultationInstanceRepository) {
        this.patientService = patientService;
        this.consultationRepository = consultationRepository;
        this.consultationInstanceRepository = consultationInstanceRepository;
    }

    @Transactional(readOnly = true)
    public Response<List<ConsultationHistoryResponseDTO>> execute(Long idPatient) {
        patientService.findById(idPatient);

        List<Consultation> consultations = consultationRepository.findByPatientIdOrderByIdDesc(idPatient);
        List<ConsultationInstance> instances = consultationInstanceRepository.findByConsultationPatientId(idPatient);

        Map<Long, Long> instanceIdByConsultationId = instances.stream()
                .collect(Collectors.toMap(i -> i.getConsultation().getId(), ConsultationInstance::getId));

        List<ConsultationHistoryResponseDTO> history = consultations.stream()
                .map(c -> ConsultationHistoryResponseDTO.build(c, instanceIdByConsultationId.get(c.getId())))
                .toList();

        return new Response<>(true, null, history);
    }
}