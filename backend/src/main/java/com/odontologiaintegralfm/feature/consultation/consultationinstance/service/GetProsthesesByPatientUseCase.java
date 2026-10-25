package com.odontologiaintegralfm.feature.consultation.consultationinstance.service;

import com.odontologiaintegralfm.feature.consultation.consultationinstance.dto.ProsthesisResponseDTO;
import com.odontologiaintegralfm.feature.consultation.prestation.repository.IPrestationTypeRepository;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.scope.dto.ScopeDTO;
import com.odontologiaintegralfm.feature.consultation.scope.mapper.ScopeMapper;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.model.TreatmentInstance;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.repository.ITreatmentInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.service.ProsthesisDomainService;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.service.ProsthesisDomainService.CurrentProsthesis;
import com.odontologiaintegralfm.feature.patient.service.IPatientService;
import com.odontologiaintegralfm.shared.dto.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lista las prótesis vigentes de un paciente (REQ-038): valida al paciente, lee sus tratamientos y prestaciones
 * habilitados y deja que {@link ProsthesisDomainService} decida cuáles son prótesis. No persiste nada.
 * El detalle de cada paso está en el § 7 del design.
 */
@Service
public class GetProsthesesByPatientUseCase {

    private final IPatientService patientService;
    private final ITreatmentInstanceRepository treatmentInstanceRepository;
    private final IPrestationInstanceRepository prestationInstanceRepository;
    private final IPrestationTypeRepository prestationTypeRepository;
    private final ProsthesisDomainService prosthesisDomainService;
    private final ScopeMapper scopeMapper;

    public GetProsthesesByPatientUseCase(IPatientService patientService,
                                         ITreatmentInstanceRepository treatmentInstanceRepository,
                                         IPrestationInstanceRepository prestationInstanceRepository,
                                         IPrestationTypeRepository prestationTypeRepository,
                                         ProsthesisDomainService prosthesisDomainService,
                                         ScopeMapper scopeMapper) {
        this.patientService = patientService;
        this.treatmentInstanceRepository = treatmentInstanceRepository;
        this.prestationInstanceRepository = prestationInstanceRepository;
        this.prestationTypeRepository = prestationTypeRepository;
        this.prosthesisDomainService = prosthesisDomainService;
        this.scopeMapper = scopeMapper;
    }

    @Transactional(readOnly = true)
    public Response<List<ProsthesisResponseDTO>> execute(Long patientId) {

        // 1. Validar el paciente de la ruta (D-10)
        patientService.findByIdAndEnabledTrue(patientId);

        // 2. Leer lo del paciente (D-02, D-04, D-07)
        List<TreatmentInstance> treatmentInstances = treatmentInstanceRepository.findByPatientIdAndEnabledTrue(patientId);
        List<PrestationInstance> prestationInstances = prestationInstanceRepository.findByPatientIdAndEnabledTrue(patientId);
        Set<Long> repairLinkedTreatmentIds = new HashSet<>(prestationTypeRepository.findTreatmentIdsLinkedToOriginRepairs());

        // 3. Calcular las prótesis vigentes (D-01 a D-05)
        List<CurrentProsthesis> currentProstheses = prosthesisDomainService.buildCurrentProstheses(
                treatmentInstances, prestationInstances, repairLinkedTreatmentIds);

        // 4. Armar la respuesta (D-06)
        List<ProsthesisResponseDTO> prostheses = currentProstheses.stream()
                .map(prosthesis -> {
                    List<ScopeDTO> scopes = prosthesis.scopes().stream().map(scopeMapper::toScopeDTO).toList();
                    return new ProsthesisResponseDTO(prosthesis.origin().getId(), scopes);
                })
                .toList();

        return new Response<>(true, null, prostheses);
    }
}