package com.odontologiaintegralfm.feature.consultation.consultationinstance.service;

import com.odontologiaintegralfm.feature.consultation.consultationinstance.dto.ClinicalContextResponseDTO;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.dto.PrestationInstanceContextResponseDTO;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.dto.StepContextResponseDTO;
import com.odontologiaintegralfm.feature.consultation.consultationinstance.dto.TreatmentInstanceContextResponseDTO;
import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.prestation.repository.IPrestationStepRepository;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.model.PrestationStepInstance;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.repository.IPrestationStepInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.scope.dto.ScopeResponseDTO;
import com.odontologiaintegralfm.feature.consultation.scope.mapper.ScopeMapper;
import com.odontologiaintegralfm.feature.consultation.scope.model.Scope;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.model.TreatmentInstance;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.repository.ITreatmentInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.service.ProsthesisDomainService;
import com.odontologiaintegralfm.feature.dentist.dto.DentistSpecialtyResponseDTO;
import com.odontologiaintegralfm.feature.dentist.model.DentistSpecialty;
import com.odontologiaintegralfm.feature.patient.service.IPatientService;
import com.odontologiaintegralfm.shared.dto.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Devuelve el contexto clínico de un paciente al iniciar una consulta (REQ-027): sus tratamientos abiertos y sus
 * prestaciones en curso, con la cadena de steps de cada prestación. No persiste nada.
 * El detalle de cada paso está en el § 7 del design.
 */
@Service
public class GetClinicalContextByPatientUseCase {

    private static final Comparator<TreatmentInstance> OLDEST_TREATMENT_FIRST = Comparator
            .comparing(TreatmentInstance::getCreatedAt)
            .thenComparing(TreatmentInstance::getId);

    private static final Comparator<PrestationInstance> OLDEST_PRESTATION_FIRST = Comparator
            .comparing(PrestationInstance::getCreatedAt)
            .thenComparing(PrestationInstance::getId);

    private static final Comparator<DentistSpecialty> BY_ID = Comparator.comparing(DentistSpecialty::getId);

    private final IPatientService patientService;
    private final ITreatmentInstanceRepository treatmentInstanceRepository;
    private final IPrestationInstanceRepository prestationInstanceRepository;
    private final IPrestationStepInstanceRepository prestationStepInstanceRepository;
    private final IPrestationStepRepository prestationStepRepository;
    private final ProsthesisDomainService prosthesisDomainService;
    private final ScopeMapper scopeMapper;

    public GetClinicalContextByPatientUseCase(IPatientService patientService,
                                              ITreatmentInstanceRepository treatmentInstanceRepository,
                                              IPrestationInstanceRepository prestationInstanceRepository,
                                              IPrestationStepInstanceRepository prestationStepInstanceRepository,
                                              IPrestationStepRepository prestationStepRepository,
                                              ProsthesisDomainService prosthesisDomainService,
                                              ScopeMapper scopeMapper) {
        this.patientService = patientService;
        this.treatmentInstanceRepository = treatmentInstanceRepository;
        this.prestationInstanceRepository = prestationInstanceRepository;
        this.prestationStepInstanceRepository = prestationStepInstanceRepository;
        this.prestationStepRepository = prestationStepRepository;
        this.prosthesisDomainService = prosthesisDomainService;
        this.scopeMapper = scopeMapper;
    }

    @Transactional(readOnly = true)
    public Response<ClinicalContextResponseDTO> execute(Long patientId) {

        // 1. Validar el paciente de la ruta (D-05)
        patientService.findByIdAndEnabledTrue(patientId);

        // 2. Leer lo abierto del paciente (D-01, D-02, D-06)
        List<TreatmentInstance> treatmentInstances = new ArrayList<>(treatmentInstanceRepository.findActiveByPatientId(patientId));
        List<PrestationInstance> prestationInstances = new ArrayList<>(prestationInstanceRepository.findInProgressByPatientId(patientId));
        treatmentInstances.sort(OLDEST_TREATMENT_FIRST);
        prestationInstances.sort(OLDEST_PRESTATION_FIRST);
        if (treatmentInstances.isEmpty() && prestationInstances.isEmpty()) {
            return new Response<>(true, null, new ClinicalContextResponseDTO(List.of(), List.of()));
        }

        // 3. Armar las ubicaciones del origen (D-02)
        Map<TreatmentInstance, List<Scope>> scopesByOrigin = prestationInstances.isEmpty()
                ? Map.of()
                : buildScopesByOrigin(patientId, prestationInstances);

        // 4. Armar los steps de cada prestación abierta (D-01, D-03)
        Map<Long, List<PrestationStepInstance>> stepInstancesByPrestation = prestationInstances.isEmpty()
                ? Map.of()
                : groupStepInstancesByPrestation(prestationInstances);
        Map<Long, List<PrestationStep>> catalogByType = prestationInstances.isEmpty()
                ? Map.of()
                : readCatalogByType(prestationInstances);

        // 5. Armar la respuesta (D-02, D-07)
        Map<Long, List<DentistSpecialtyResponseDTO>> specialtiesByTreatment = new HashMap<>();
        Map<Long, List<DentistSpecialtyResponseDTO>> specialtiesByType = new HashMap<>();

        List<TreatmentInstanceContextResponseDTO> treatments = treatmentInstances.stream()
                .map(treatmentInstance -> toTreatmentDTO(treatmentInstance, specialtiesByTreatment))
                .toList();
        List<PrestationInstanceContextResponseDTO> prestations = prestationInstances.stream()
                .map(prestationInstance -> toPrestationDTO(prestationInstance, scopesByOrigin,
                        stepInstancesByPrestation, catalogByType, specialtiesByType))
                .toList();

        return new Response<>(true, null, new ClinicalContextResponseDTO(treatments, prestations));
    }

    /** Ubicación del origen de cada prestación abierta que tenga uno, con los arreglos {@code FINISHED} del paciente. */
    private Map<TreatmentInstance, List<Scope>> buildScopesByOrigin(Long patientId, List<PrestationInstance> prestationInstances) {
        Set<TreatmentInstance> origins = new LinkedHashSet<>();
        for (PrestationInstance prestationInstance : prestationInstances) {
            if (prestationInstance.getOriginTreatmentInstance() != null) {
                origins.add(prestationInstance.getOriginTreatmentInstance());
            }
        }
        if (origins.isEmpty()) {
            return Map.of();
        }
        List<PrestationInstance> allPrestations = prestationInstanceRepository.findByPatientIdAndEnabledTrue(patientId);
        return prosthesisDomainService.buildScopesByOrigin(origins, allPrestations);
    }

    /** Instancias de step de todas las prestaciones en una sola consulta, agrupadas por id de prestación. */
    private Map<Long, List<PrestationStepInstance>> groupStepInstancesByPrestation(List<PrestationInstance> prestationInstances) {
        return prestationStepInstanceRepository.findByPrestationInstanceIn(prestationInstances).stream()
                .collect(Collectors.groupingBy(stepInstance -> stepInstance.getPrestationInstance().getId()));
    }

    /** Catálogo de steps de todos los tipos distintos en una sola consulta, agrupado por tipo y ordenado por {@code position}. */
    private Map<Long, List<PrestationStep>> readCatalogByType(List<PrestationInstance> prestationInstances) {
        Set<Long> typeIds = prestationInstances.stream()
                .map(prestationInstance -> prestationInstance.getType().getId())
                .collect(Collectors.toSet());
        return prestationStepRepository.findByPrestationIdInFetchStep(typeIds).stream()
                .collect(Collectors.groupingBy(
                        prestationStep -> prestationStep.getPrestation().getId(),
                        Collectors.collectingAndThen(Collectors.toList(), steps -> steps.stream()
                                .sorted(Comparator.comparingInt(PrestationStep::getPosition))
                                .toList())));
    }

    private TreatmentInstanceContextResponseDTO toTreatmentDTO(TreatmentInstance treatmentInstance,
                                                               Map<Long, List<DentistSpecialtyResponseDTO>> specialtiesByTreatment) {
        List<DentistSpecialtyResponseDTO> specialties = specialtiesByTreatment.computeIfAbsent(
                treatmentInstance.getTreatment().getId(),
                treatmentId -> toSpecialtyDTOs(treatmentInstance.getTreatment().getDentistSpecialties()));
        return new TreatmentInstanceContextResponseDTO(
                treatmentInstance.getId(),
                treatmentInstance.getTreatment().getLabel(),
                treatmentInstance.getTreatmentCondition(),
                scopeMapper.toScopeResponseDTO(treatmentInstance.getScope()),
                specialties);
    }

    private PrestationInstanceContextResponseDTO toPrestationDTO(PrestationInstance prestationInstance,
                                                                 Map<TreatmentInstance, List<Scope>> scopesByOrigin,
                                                                 Map<Long, List<PrestationStepInstance>> stepInstancesByPrestation,
                                                                 Map<Long, List<PrestationStep>> catalogByType,
                                                                 Map<Long, List<DentistSpecialtyResponseDTO>> specialtiesByType) {
        TreatmentInstance treatmentInstance = prestationInstance.getTreatmentInstance();
        TreatmentInstance origin = prestationInstance.getOriginTreatmentInstance();
        List<ScopeResponseDTO> originScopes = origin == null
                ? List.of()
                : scopesByOrigin.getOrDefault(origin, List.of()).stream().map(scopeMapper::toScopeResponseDTO).toList();
        Long typeId = prestationInstance.getType().getId();
        List<DentistSpecialtyResponseDTO> specialties = specialtiesByType.computeIfAbsent(typeId,
                id -> toSpecialtyDTOs(prestationInstance.getType().getDentistSpecialties()));
        return new PrestationInstanceContextResponseDTO(
                prestationInstance.getId(),
                prestationInstance.getType().getLabel(),
                prestationInstance.getStatus(),
                scopeMapper.toScopeResponseDTO(prestationInstance.getScope()),
                treatmentInstance == null ? null : treatmentInstance.getId(),
                origin == null ? null : origin.getId(),
                originScopes,
                specialties,
                toStepDTOs(catalogByType.get(typeId),
                        stepInstancesByPrestation.getOrDefault(prestationInstance.getId(), List.of())));
    }

    /** Un elemento por step del catálogo: con el id de la instancia si ya existe, o con el id del catálogo si no. */
    private List<StepContextResponseDTO> toStepDTOs(List<PrestationStep> catalog, List<PrestationStepInstance> stepInstances) {
        return catalog.stream()
                .map(prestationStep -> {
                    PrestationStepInstance stepInstance = stepInstances.stream()
                            .filter(candidate -> candidate.getStep().getId().equals(prestationStep.getId()))
                            .findFirst()
                            .orElse(null);
                    String label = prestationStep.getStep().getLabel();
                    return stepInstance == null
                            ? new StepContextResponseDTO(null, prestationStep.getId(), label, prestationStep.isRequired(), null)
                            : new StepContextResponseDTO(stepInstance.getId(), null, label, prestationStep.isRequired(), stepInstance.getStatus());
                })
                .toList();
    }

    private List<DentistSpecialtyResponseDTO> toSpecialtyDTOs(Set<DentistSpecialty> specialties) {
        if (specialties == null) {
            return List.of();
        }
        return specialties.stream()
                .sorted(BY_ID)
                .map(specialty -> new DentistSpecialtyResponseDTO(specialty.getId(), specialty.getName()))
                .toList();
    }
}