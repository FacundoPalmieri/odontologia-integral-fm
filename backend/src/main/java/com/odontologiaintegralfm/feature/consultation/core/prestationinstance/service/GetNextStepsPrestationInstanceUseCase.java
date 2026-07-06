package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.repository.IPrestationStepRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationStepResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.mapper.PrestationStepMapper;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationStepInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.repository.IPrestationStepInstanceRepository;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class GetNextStepsPrestationInstanceUseCase {

    private final PrestationInstanceQueryService prestationInstanceQueryService;
    private final IPrestationStepRepository prestationStepRepository;
    private final IPrestationStepInstanceRepository prestationStepInstanceRepository;
    private final PrestationStepMapper prestationStepMapper;

    public GetNextStepsPrestationInstanceUseCase(PrestationInstanceQueryService prestationInstanceQueryService,
                                                 IPrestationStepRepository prestationStepRepository,
                                                 IPrestationStepInstanceRepository prestationStepInstanceRepository,
                                                 PrestationStepMapper prestationStepMapper) {
        this.prestationInstanceQueryService = prestationInstanceQueryService;
        this.prestationStepRepository = prestationStepRepository;
        this.prestationStepInstanceRepository = prestationStepInstanceRepository;
        this.prestationStepMapper = prestationStepMapper;
    }

    @Transactional(readOnly = true)
    public List<PrestationStepResponseDTO> execute(Long id) {
        PrestationInstance prestationInstance = prestationInstanceQueryService.findById(id);

        validateStatus(prestationInstance);
        validateHasStepsWorkflow(prestationInstance, id);

        List<PrestationStep> catalogSteps = loadCatalogSteps(prestationInstance);
        List<PrestationStepInstance> stepInstances = prestationStepInstanceRepository.findByPrestationInstance(prestationInstance);

        int startPosition = resolveStartPosition(stepInstances);
        List<PrestationStep> nextSteps = selectNextSteps(catalogSteps, startPosition);

        return prestationStepMapper.toDTO(nextSteps);
    }

    private void validateStatus(PrestationInstance prestationInstance) {
        if (prestationInstance.getStatus() != PrestationInstanceStatus.IN_PROGRESS) {
            throw new BadRequestException(
                    "exception.getNextStepsPrestationInstanceUseCase.invalidStatus.user", null,
                    "exception.getNextStepsPrestationInstanceUseCase.invalidStatus.log",
                    new Object[]{prestationInstance.getStatus().getLabel(), "GetNextStepsPrestationInstanceUseCase", "execute"},
                    LogLevel.WARN
            );
        }
    }

    private void validateHasStepsWorkflow(PrestationInstance prestationInstance, Long id) {
        if (!prestationInstance.getType().isHasSteps()) {
            throw new BadRequestException(
                    "exception.getNextStepsPrestationInstanceUseCase.noWorkflow.user", null,
                    "exception.getNextStepsPrestationInstanceUseCase.noWorkflow.log",
                    new Object[]{id, "GetNextStepsPrestationInstanceUseCase", "execute"},
                    LogLevel.WARN
            );
        }
    }

    private List<PrestationStep> loadCatalogSteps(PrestationInstance prestationInstance) {
        List<PrestationStep> catalogSteps = prestationStepRepository.findByPrestationId(prestationInstance.getType().getId());
        if (catalogSteps.isEmpty()) {
            throw new NotFoundException(
                    "exception.getNextStepsPrestationInstanceUseCase.noSteps.user", null,
                    "exception.getNextStepsPrestationInstanceUseCase.noSteps.log",
                    new Object[]{prestationInstance.getType().getId(), "GetNextStepsPrestationInstanceUseCase", "execute"},
                    LogLevel.ERROR
            );
        }
        return catalogSteps;
    }

    private int resolveStartPosition(List<PrestationStepInstance> stepInstances) {
        return stepInstances.stream()
                .filter(si -> si.getStatus() == PrestationStepStatus.COMPLETED
                           || si.getStatus() == PrestationStepStatus.OMITTED)
                .mapToInt(si -> si.getStep().getPosition())
                .max()
                .orElse(0);
    }

    private List<PrestationStep> selectNextSteps(List<PrestationStep> catalogSteps, int startPosition) {
        List<PrestationStep> nextSteps = new ArrayList<>();

        List<PrestationStep> sortedSteps = catalogSteps.stream()
                .sorted(Comparator.comparingInt(PrestationStep::getPosition))
                .toList();

        for (PrestationStep step : sortedSteps) {
            if (step.getPosition() <= startPosition) continue;
            nextSteps.add(step);
            if (step.isRequired()) break;
        }

        return nextSteps;
    }
}