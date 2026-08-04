package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationEventType;
import com.odontologiaintegralfm.feature.consultation.core.consultation.mapper.ConsultationMapper;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.ConsultationEvent;
import com.odontologiaintegralfm.feature.consultation.core.consultation.service.ConsultationEventService;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto.ConsultationInstanceResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto.ConsultationInstanceUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.repository.IConsultationInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationStepService;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationTypePriceService;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationTypeService;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model.Promotion;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service.PromotionQueryService;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.enums.TreatmentConditionType;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.model.TreatmentCondition;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.service.TreatmentConditionService;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.service.TreatmentService;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.mapper.OdontogramMapper;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.model.Odontogram;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.repository.IOdontogramRepository;
import com.odontologiaintegralfm.feature.consultation.core.payment.repository.IPaymentDetailRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationStepAdvancementRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationStepUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.mapper.PrestationInstanceMapper;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationStepInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.repository.IPrestationStepInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.service.PrestationInstanceDomainService;
import com.odontologiaintegralfm.feature.user.model.UserSec;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.infrastructure.systemparameter.enums.SystemParameterKey;
import com.odontologiaintegralfm.infrastructure.systemparameter.service.interfaces.ISystemParameterService;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UpdateConsultationInstanceUseCase {

    private final ConsultationInstanceQueryService consultationInstanceQueryService;
    private final AuthenticatedUserService authenticatedUserService;
    private final ISystemParameterService systemParameterService;
    private final IConsultationInstanceRepository consultationInstanceRepository;
    private final IOdontogramRepository odontogramRepository;
    private final IPrestationInstanceRepository prestationInstanceRepository;
    private final PrestationInstanceDomainService prestationInstanceDomainService;
    private final PrestationTypeService prestationTypeService;
    private final PrestationTypePriceService prestationTypePriceService;
    private final PrestationStepService prestationStepService;
    private final IPrestationStepInstanceRepository prestationStepInstanceRepository;
    private final TreatmentService treatmentService;
    private final TreatmentConditionService treatmentConditionService;
    private final PromotionQueryService promotionQueryService;
    private final OdontogramMapper odontogramMapper;
    private final PrestationInstanceMapper prestationInstanceMapper;
    private final MessageSource messageSource;
    private final ConsultationEventService consultationEventService;
    private final ConsultationMapper consultationMapper;
    private final IPaymentDetailRepository paymentDetailRepository;

    public UpdateConsultationInstanceUseCase(ConsultationInstanceQueryService consultationInstanceQueryService,
                                             AuthenticatedUserService authenticatedUserService,
                                             ISystemParameterService systemParameterService,
                                             IConsultationInstanceRepository consultationInstanceRepository,
                                             IOdontogramRepository odontogramRepository,
                                             IPrestationInstanceRepository prestationInstanceRepository,
                                             PrestationInstanceDomainService prestationInstanceDomainService,
                                             PrestationTypeService prestationTypeService,
                                             PrestationTypePriceService prestationTypePriceService,
                                             PrestationStepService prestationStepService,
                                             IPrestationStepInstanceRepository prestationStepInstanceRepository,
                                             TreatmentService treatmentService,
                                             TreatmentConditionService treatmentConditionService,
                                             PromotionQueryService promotionQueryService,
                                             OdontogramMapper odontogramMapper,
                                             PrestationInstanceMapper prestationInstanceMapper,
                                             MessageSource messageSource,
                                             ConsultationEventService consultationEventService,
                                             ConsultationMapper consultationMapper,
                                             IPaymentDetailRepository paymentDetailRepository) {
        this.consultationInstanceQueryService = consultationInstanceQueryService;
        this.authenticatedUserService = authenticatedUserService;
        this.systemParameterService = systemParameterService;
        this.consultationInstanceRepository = consultationInstanceRepository;
        this.odontogramRepository = odontogramRepository;
        this.prestationInstanceRepository = prestationInstanceRepository;
        this.prestationInstanceDomainService = prestationInstanceDomainService;
        this.prestationTypeService = prestationTypeService;
        this.prestationTypePriceService = prestationTypePriceService;
        this.prestationStepService = prestationStepService;
        this.prestationStepInstanceRepository = prestationStepInstanceRepository;
        this.treatmentService = treatmentService;
        this.treatmentConditionService = treatmentConditionService;
        this.promotionQueryService = promotionQueryService;
        this.odontogramMapper = odontogramMapper;
        this.prestationInstanceMapper = prestationInstanceMapper;
        this.messageSource = messageSource;
        this.consultationEventService = consultationEventService;
        this.consultationMapper = consultationMapper;
        this.paymentDetailRepository = paymentDetailRepository;
    }

    @LogAction(
            value = "updateConsultationInstanceUseCase.logAction.update.ok",
            args = {"#result.data.id"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    @Transactional
    public Response<ConsultationInstanceResponseDTO> execute(Long idConsultationInstance, ConsultationInstanceUpdateRequestDTO dto) {

        ConsultationInstance consultationInstance = consultationInstanceQueryService.findById(idConsultationInstance);
        Consultation consultation = consultationInstance.getConsultation();
        UserSec authenticatedUser = authenticatedUserService.getAuthenticatedUser();

        validateOwnership(idConsultationInstance, consultation, authenticatedUser);
        long correctionHours = validateCorrectionWindow(idConsultationInstance, consultationInstance);

        OdontogramDeltaResult odontogramDelta = applyOdontogramDelta(idConsultationInstance, dto.odontogram(), authenticatedUser, consultationInstance);

        List<Long> cascadeCancelledPrestationIds = cascadeDisabledOdontogramsToPrestations(idConsultationInstance, odontogramDelta.disabledOdontograms(), authenticatedUser, correctionHours);

        applyPrestationInstanceDelta(idConsultationInstance, consultationInstance, dto.prestationExisting(), dto.stepAdvancements(), correctionHours, cascadeCancelledPrestationIds, authenticatedUser);

        buildNewPrestationInstances(consultationInstance, consultation, dto, odontogramDelta.odontogramsSaved());

        consultationEventService.create(ConsultationEvent.build(consultation, ConsultationEventType.CONSULTATION_INSTANCE_CORRECTED, dto.observation()));

        consultationInstance.setObservation(dto.observation());
        consultationInstanceRepository.save(consultationInstance);

        return new Response<>(
                true,
                messageSource.getMessage("updateConsultationInstanceUseCase.update.ok", null, LocaleContextHolder.getLocale()),
                buildResponse(consultationInstance)
        );
    }

    /**
     * Arma el response con el estado completo persistido tras la corrección (mismo patrón que
     * GetConsultationInstanceUseCase) — no solo el delta aplicado en este PUT.
     */
    private ConsultationInstanceResponseDTO buildResponse(ConsultationInstance consultationInstance) {
        List<PrestationInstance> prestations = prestationInstanceRepository.findByConsultationInstanceId(consultationInstance.getId());

        Map<Long, List<PrestationStepInstance>> stepsByPrestationId = prestationStepInstanceRepository.findByPrestationInstanceIn(prestations)
                .stream()
                .collect(Collectors.groupingBy(step -> step.getPrestationInstance().getId()));

        List<PrestationInstanceResponseDTO> prestationDTOs = prestations.stream()
                .map(p -> {
                    BigDecimal paid = paymentDetailRepository.sumAmountByPrestationInstanceId(p.getId());
                    BigDecimal pending = p.getFinalAmount().subtract(paid);
                    PrestationInstanceResponseDTO base = prestationInstanceMapper.toDTO(p);
                    List<PrestationStepInstance> steps = stepsByPrestationId.getOrDefault(p.getId(), List.of());
                    return new PrestationInstanceResponseDTO(
                            base.id(), base.name(), base.odontogram(), base.scope(), base.scopeDetail(),
                            base.status(), base.price(), base.promotion(), base.promotionAmount(),
                            base.discountType(), base.discountValue(), base.discountAmount(),
                            base.finalAmount(), pending, prestationInstanceMapper.toStepDTOs(steps)
                    );
                })
                .toList();

        BigDecimal totalFinalAmount = prestations.stream()
                .map(PrestationInstance::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ConsultationInstanceResponseDTO(
                consultationInstance.getId(),
                consultationMapper.toDTO(consultationInstance.getConsultation()),
                odontogramRepository.findByConsultationInstanceId(consultationInstance.getId()).stream()
                        .map(odontogramMapper::toDTO).toList(),
                prestationDTOs,
                consultationInstance.getObservation(),
                totalFinalAmount
        );
    }

    /**
     * Valida que la ConsultationInstance corresponda al odontólogo autenticado.
     * No usa ForbiddenException a propósito (ADR-0027): para el atacante que no es dueño,
     * un recurso ajeno debe verse igual que uno inexistente.
     */
    private void validateOwnership(Long idConsultationInstance, Consultation consultation, UserSec authenticatedUser) {
        Long dentistPersonId = consultation.getDentist().getPerson().getId();
        Long authenticatedPersonId = authenticatedUser.getPerson().getId();
        if (!dentistPersonId.equals(authenticatedPersonId)) {
            throw new NotFoundException("exception.updateConsultationInstanceUseCase.notFound.user", null, "exception.updateConsultationInstanceUseCase.notFound.log", new Object[]{idConsultationInstance, authenticatedPersonId, "UpdateConsultationInstanceUseCase", "execute"}, LogLevel.WARN);
        }
    }

    /**
     * Valida que la corrección se realice dentro de la ventana de tiempo permitida
     * (createdAt + CONSULTATION_INSTANCE_CORRECTIONS horas).
     */
    private long validateCorrectionWindow(Long idConsultationInstance, ConsultationInstance consultationInstance) {
        long correctionHours = Long.parseLong(systemParameterService.getByKey(SystemParameterKey.CONSULTATION_INSTANCE_CORRECTIONS));
        LocalDateTime deadline = consultationInstance.getCreatedAt().plusHours(correctionHours);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new ConflictException("exception.updateConsultationInstanceUseCase.deadlineExceeded.user", null, "exception.updateConsultationInstanceUseCase.deadlineExceeded.log", new Object[]{idConsultationInstance, deadline, "UpdateConsultationInstanceUseCase", "execute"}, LogLevel.ERROR);
        }
        return correctionHours;
    }

    /**
     * Resultado del delta de Odontogram: odontogramsSaved lo consume buildNewPrestationInstances
     * (matchOdontogram, decisión #6 § 2); disabledOdontograms lo consume la cascada
     * Odontogram→PrestationInstance (§ 3.4, regla #8).
     */
    private record OdontogramDeltaResult(List<Odontogram> odontogramsSaved, List<Odontogram> disabledOdontograms) {}

    /**
     * Aplica el delta de odontograma: alta / edición in-place / baja explícita por id+enabled
     * (ADR-0008). Sin id -> alta (insert). Con id existente + enabled:true + campo distinto ->
     * actualiza el registro persistido directamente, mismo id — si existe una PrestationInstance
     * viva vinculada, valida la ubicación nueva contra PrestationType.allowedScopes de esa
     * prestación antes de aplicar el cambio (regla #29). Con id existente + enabled:false -> solo
     * disable(), resto del elemento ignorado (regla #5). Persistido ausente del request -> 409
     * objeto incompleto (regla #6). Con enabled:false e id que no matchea ningún vigente -> 404
     * (regla #25).
     */
    private OdontogramDeltaResult applyOdontogramDelta(Long idConsultationInstance, List<OdontogramUpdateRequestDTO> odontogramDtos,
                                                         UserSec authenticatedUser, ConsultationInstance consultationInstance) {
        List<Odontogram> persistedOdontograms = odontogramRepository.findByConsultationInstanceId(idConsultationInstance);
        List<PrestationInstance> persistedPrestations = prestationInstanceRepository.findByConsultationInstanceId(idConsultationInstance);

        List<Long> matchedIds = new ArrayList<>();
        List<Odontogram> odontogramsToInsert = new ArrayList<>();
        List<Odontogram> odontogramsUpdated = new ArrayList<>();
        List<Odontogram> disabledOdontograms = new ArrayList<>();

        for (OdontogramUpdateRequestDTO odontogramDto : odontogramDtos) {
            if (odontogramDto.id() == null) {
                odontogramsToInsert.add(buildOdontogram(consultationInstance, odontogramDto));
                continue;
            }

            Odontogram existing = persistedOdontograms.stream()
                    .filter(o -> o.getId().equals(odontogramDto.id()))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("exception.updateConsultationInstanceUseCase.odontogramNotFound.user", null, "exception.updateConsultationInstanceUseCase.odontogramNotFound.log", new Object[]{odontogramDto.id(), idConsultationInstance, "UpdateConsultationInstanceUseCase", "execute"}, LogLevel.WARN));
            matchedIds.add(existing.getId());

            if (!odontogramDto.enabled()) {
                existing.disable(authenticatedUser);
                disabledOdontograms.add(existing);
                continue;
            }

            boolean fieldsChanged = existing.getTooth() != odontogramDto.tooth()
                    || existing.getToothFace() != odontogramDto.toothFace()
                    || !existing.getTreatment().getId().equals(odontogramDto.treatmentId())
                    || !existing.getTreatmentCondition().getId().equals(odontogramDto.treatmentConditionId());
            if (!fieldsChanged) {
                // Sin cambios: sigue vigente tal cual, no dispara ningún update (regla #4, § 1).
                continue;
            }

            PrestationInstance linkedPrestation = persistedPrestations.stream()
                    .filter(p -> p.getOdontogram() != null && p.getOdontogram().getId().equals(existing.getId()))
                    .findFirst()
                    .orElse(null);
            if (linkedPrestation != null) {
                prestationInstanceDomainService.validateScopeAllowedForType(linkedPrestation.getType(), toScopeType(odontogramDto));
            }

            existing.setTooth(odontogramDto.tooth());
            existing.setToothFace(odontogramDto.toothFace());
            existing.setTreatment(treatmentService.findById(odontogramDto.treatmentId()));
            existing.setTreatmentCondition(treatmentConditionService.findById(odontogramDto.treatmentConditionId()));
            odontogramsUpdated.add(existing);
        }

        for (Odontogram persisted : persistedOdontograms) {
            if (!matchedIds.contains(persisted.getId())) {
                throw new ConflictException("exception.updateConsultationInstanceUseCase.odontogramMissingFromRequest.user", null, "exception.updateConsultationInstanceUseCase.odontogramMissingFromRequest.log", new Object[]{persisted.getId(), idConsultationInstance, "UpdateConsultationInstanceUseCase", "applyOdontogramDelta"}, LogLevel.ERROR);
            }
        }

        List<Odontogram> odontogramsSaved = new ArrayList<>(odontogramsUpdated);
        if (!odontogramsToInsert.isEmpty()) {
            odontogramsSaved.addAll(odontogramRepository.saveAll(odontogramsToInsert));
        }

        return new OdontogramDeltaResult(odontogramsSaved, disabledOdontograms);
    }

    /**
     * La ubicación de un Odontogram (tooth/toothFace) no es directamente un PrestationScopeType —
     * se traduce al scope equivalente (TOOTH_FACE si tiene toothFace, TOOTH si no) para poder
     * validarla contra PrestationType.allowedScopes de la prestación vinculada (regla #29).
     */
    private PrestationScopeType toScopeType(OdontogramUpdateRequestDTO odontogramDto) {
        return odontogramDto.toothFace() != null ? PrestationScopeType.TOOTH_FACE : PrestationScopeType.TOOTH;
    }

    private Odontogram buildOdontogram(ConsultationInstance consultationInstance, OdontogramUpdateRequestDTO odontogramDto) {
        return Odontogram.build(
                consultationInstance,
                odontogramDto.tooth(),
                odontogramDto.toothFace(),
                treatmentService.findById(odontogramDto.treatmentId()),
                treatmentConditionService.findById(odontogramDto.treatmentConditionId())
        );
    }

    /**
     * Cruza los Odontogram dados de baja explícita contra las PrestationInstance vivas que los
     * referencian y las cancela en cascada (§ 3.4, regla #8) — vive en el nivel de orquestación,
     * no dentro de applyOdontogramDelta (decisión #4 § 2: un sub-recurso no debe conocer ni operar
     * sobre otro dominio). Reusa la misma guarda de ventana que la baja explícita de prestación
     * completa (isStepEditable, sin chequeo de COMPLETED — regla #11 revisión). Devuelve los ids
     * cancelados para que applyPrestationInstanceDelta valide que el front las haya declarado con
     * enabled:false (regla #6 — ausencia o enabled:true → 409) y no re-ejecute disable()/reseteo de
     * treatmentCondition sobre ellas.
     */
    private List<Long> cascadeDisabledOdontogramsToPrestations(Long idConsultationInstance, List<Odontogram> disabledOdontograms,
                                                                 UserSec authenticatedUser, long correctionHours) {
        if (disabledOdontograms.isEmpty()) {
            return List.of();
        }

        List<Long> disabledOdontogramIds = disabledOdontograms.stream().map(Odontogram::getId).toList();
        List<PrestationInstance> persistedPrestations = prestationInstanceRepository.findByConsultationInstanceId(idConsultationInstance);

        List<Long> cascadeCancelledPrestationIds = new ArrayList<>();
        for (PrestationInstance prestation : persistedPrestations) {
            if (prestation.getOdontogram() == null || !disabledOdontogramIds.contains(prestation.getOdontogram().getId())) {
                continue;
            }

            List<PrestationStepInstance> steps = prestationStepInstanceRepository.findByPrestationInstance(prestation);
            boolean hasStepOutOfWindow = steps.stream().anyMatch(s -> !prestationInstanceDomainService.isStepEditable(s, correctionHours));
            if (hasStepOutOfWindow) {
                throw new ConflictException("exception.updateConsultationInstanceUseCase.stepOutOfWindow.user", null, "exception.updateConsultationInstanceUseCase.stepOutOfWindow.log", new Object[]{prestation.getId(), "UpdateConsultationInstanceUseCase", "cascadeDisabledOdontogramsToPrestations"}, LogLevel.ERROR);
            }

            prestation.disable(authenticatedUser);
            for (PrestationStepInstance step : steps) {
                step.disable(authenticatedUser);
            }
            cascadeCancelledPrestationIds.add(prestation.getId());
        }

        return cascadeCancelledPrestationIds;
    }

    /**
     * Aplica el delta de PrestationInstance/PrestationStepInstance ya persistidas (Paso 5.3). Reglas
     * aplicadas:
     * <ul>
     *   <li>Toda prestación vigente (o cancelada por la cascada en este mismo request) debe
     *   declararse en el request con el enabled real, sin excepciones — ausente o con enabled
     *   contradictorio → 409.</li>
     *   <li>Una prestación cancelada por la cascada Odontogram→PrestationInstance debe llegar con
     *   enabled:false, coincidiendo con el estado que el backend ya aplicó.</li>
     *   <li>Ni la prestación ni sus steps se pueden editar/dar de baja si su propio createdAt quedó
     *   fuera de la ventana de corrección.</li>
     *   <li>La baja explícita exige declarar cada step vigente con enabled:false (mismo criterio de
     *   completitud, sin excepción por baja del contenedor) y respeta la misma guarda de ventana.</li>
     *   <li>Edición in-place (mismo id) de scope/tooth/toothFace/quadrant/maxillary solo si la
     *   prestación no tiene Odontogram asociado — si lo tiene, cualquier campo de ubicación con
     *   valor es 409.</li>
     *   <li>El scope resultante, cuando cambia respecto al persistido, debe seguir siendo uno de los
     *   permitidos por el PrestationType de la prestación.</li>
     *   <li>El array de steps admite altas nuevas sobre una prestación ya persistida además de
     *   avances de estado sobre steps existentes.</li>
     * </ul>
     */
    private void applyPrestationInstanceDelta(Long idConsultationInstance, ConsultationInstance consultationInstance,
                                               List<PrestationInstanceUpdateRequestDTO> prestationExistingDtos,
                                               List<PrestationStepUpdateRequestDTO> stepRequest, long correctionHours,
                                               List<Long> cascadeCancelledPrestationIds,
                                               UserSec authenticatedUser) {
        List<PrestationInstance> persistedPrestations = prestationInstanceRepository.findByConsultationInstanceId(idConsultationInstance);
        Long patientId = consultationInstance.getConsultation().getPatient().getId();

        // regla #15: prestationInstanceId de stepAdvancements debe existir YA PERSISTIDO (nunca prestationNew)
        for (PrestationStepUpdateRequestDTO stepDto : stepRequest) {
            boolean exists = persistedPrestations.stream().anyMatch(p -> p.getId().equals(stepDto.prestationInstanceId()));
            if (!exists) {
                throw new NotFoundException("exception.updateConsultationInstanceUseCase.stepAdvancementPrestationInstanceIdNotFound.user", null, "exception.updateConsultationInstanceUseCase.stepAdvancementPrestationInstanceIdNotFound.log", new Object[]{stepDto.prestationInstanceId(), idConsultationInstance, "UpdateConsultationInstanceUseCase", "applyPrestationInstanceDelta"}, LogLevel.ERROR);
            }
        }

        Map<Long, List<PrestationStepUpdateRequestDTO>> stepsByPrestationId = stepRequest.stream()
                .collect(Collectors.groupingBy(PrestationStepUpdateRequestDTO::prestationInstanceId));

        Map<Long, PrestationInstanceUpdateRequestDTO> existingDtoByPrestationId = prestationExistingDtos.stream()
                .collect(Collectors.toMap(PrestationInstanceUpdateRequestDTO::id, dto -> dto, (a, b) -> a));

        for (PrestationInstance persisted : persistedPrestations) {
            PrestationInstanceUpdateRequestDTO existingDto = existingDtoByPrestationId.get(persisted.getId());
            List<PrestationStepUpdateRequestDTO> ownStepRequest = stepsByPrestationId.getOrDefault(persisted.getId(), List.of());

            if (existingDto == null) {
                throw new ConflictException("exception.updateConsultationInstanceUseCase.prestationInstanceMissingFromRequest.user", null, "exception.updateConsultationInstanceUseCase.prestationInstanceMissingFromRequest.log", new Object[]{persisted.getId(), idConsultationInstance, "UpdateConsultationInstanceUseCase", "applyPrestationInstanceDelta"}, LogLevel.ERROR);
            }

            // prestación ya cancelada por la cascada (regla #8): el front debe haberla declarado con
            // enabled:false (coincide con el resultado real, no se re-ejecuta disable()); con enabled:true
            // contradice el estado que el backend acaba de aplicar → 409
            if (cascadeCancelledPrestationIds.contains(persisted.getId())) {
                if (existingDto.enabled()) {
                    throw new ConflictException("exception.updateConsultationInstanceUseCase.prestationInstanceCancelledByCascadeEnabledTrue.user", null, "exception.updateConsultationInstanceUseCase.prestationInstanceCancelledByCascadeEnabledTrue.log", new Object[]{persisted.getId(), "UpdateConsultationInstanceUseCase", "applyPrestationInstanceDelta"}, LogLevel.ERROR);
                }
                continue;
            }

            if (!prestationInstanceDomainService.isPrestationInstanceEditable(persisted, correctionHours)) {
                throw new ConflictException("exception.updateConsultationInstanceUseCase.prestationInstanceOutOfWindow.user", null, "exception.updateConsultationInstanceUseCase.prestationInstanceOutOfWindow.log", new Object[]{persisted.getId(), "UpdateConsultationInstanceUseCase", "applyPrestationInstanceDelta"}, LogLevel.ERROR);
            }

            if (!existingDto.enabled()) {
                disablePrestationInstanceExplicit(persisted, ownStepRequest, correctionHours, authenticatedUser);
                continue;
            }

            prestationInstanceDomainService.validateLocationNotOverridable(persisted, existingDto);
            if (persisted.getScope() != existingDto.scope()) {
                prestationInstanceDomainService.validateScopeAllowedForType(persisted.getType(), existingDto.scope());
            }
            applyPrestationInstanceLocationEdit(persisted, existingDto);

            advancePrestationInstance(consultationInstance, patientId, persisted, ownStepRequest, correctionHours, authenticatedUser);
        }

        // completitud de steps (regla #6): step vigente ausente de stepAdvancements, prestación padre viva
        // y presente con enabled:true → 409 (prestación dada de baja explícita ya validó sus propios steps
        // dentro de disablePrestationInstanceExplicit, sin excepción, se excluye acá para no reevaluarla)
        validateStepCompleteness(persistedPrestations, existingDtoByPrestationId, stepsByPrestationId, cascadeCancelledPrestationIds);
    }

    /**
     * Baja explícita de PrestationInstance completa (regla #9): el front debe declarar cada uno de
     * sus steps vigentes en stepAdvancements con enabled:false — ausencia o enabled:true de un step
     * vigente es contradicción con la baja del padre → 409 (mismo criterio que la completitud de
     * regla #6, sin excepción por baja del contenedor). Guarda de ventana (regla #11/#12, sin chequeo
     * de COMPLETED). Si tiene Odontogram asociado, resetea su treatmentCondition a REQUIRED (regla
     * #10 — exclusivo de la baja explícita, nunca de la cascada).
     */
    private void disablePrestationInstanceExplicit(PrestationInstance persisted, List<PrestationStepUpdateRequestDTO> ownStepRequest,
                                                    long correctionHours, UserSec authenticatedUser) {
        List<PrestationStepInstance> steps = prestationStepInstanceRepository.findByPrestationInstance(persisted);

        Map<Long, PrestationStepUpdateRequestDTO> ownStepRequestById = ownStepRequest.stream()
                .filter(s -> s.id() != null)
                .collect(Collectors.toMap(PrestationStepUpdateRequestDTO::id, s -> s, (a, b) -> a));

        for (PrestationStepInstance step : steps) {
            PrestationStepUpdateRequestDTO stepDto = ownStepRequestById.get(step.getId());
            if (stepDto == null || stepDto.enabled()) {
                throw new ConflictException("exception.updateConsultationInstanceUseCase.stepMissingFromRequest.user", null, "exception.updateConsultationInstanceUseCase.stepMissingFromRequest.log", new Object[]{step.getId(), persisted.getId(), "UpdateConsultationInstanceUseCase", "disablePrestationInstanceExplicit"}, LogLevel.ERROR);
            }
        }

        boolean hasStepOutOfWindow = steps.stream().anyMatch(s -> !prestationInstanceDomainService.isStepEditable(s, correctionHours));
        if (hasStepOutOfWindow) {
            throw new ConflictException("exception.updateConsultationInstanceUseCase.stepOutOfWindow.user", null, "exception.updateConsultationInstanceUseCase.stepOutOfWindow.log", new Object[]{persisted.getId(), "UpdateConsultationInstanceUseCase", "disablePrestationInstanceExplicit"}, LogLevel.ERROR);
        }

        persisted.disable(authenticatedUser);
        for (PrestationStepInstance step : steps) {
            step.disable(authenticatedUser);
        }

        if (persisted.getOdontogram() != null) {
            // Sin save() explícito: el Odontogram es una entidad managed dentro de este @Transactional,
            // el dirty-checking de Hibernate genera el UPDATE al hacer commit (mismo criterio que el resto
            // de las ediciones in-place de este use case).
            TreatmentCondition requiredCondition = treatmentConditionService.findByName(TreatmentConditionType.REQUIRED.name());
            persisted.getOdontogram().setTreatmentCondition(requiredCondition);
        }
    }

    /**
     * Edición de ubicación de PrestationInstance existente sin Odontogram asociado (regla #16):
     * actualiza los campos in-place sobre el mismo registro persistido, mismo id (ADR-0008). Si no
     * hay cambio de ubicación, no hace nada — el elemento se considera declarado sin disparar
     * ningún update.
     */
    private void applyPrestationInstanceLocationEdit(PrestationInstance persisted, PrestationInstanceUpdateRequestDTO dto) {
        if (persisted.getOdontogram() != null) {
            return;
        }

        boolean locationChanged = persisted.getScope() != dto.scope() || persisted.getTooth() != dto.tooth()
                || persisted.getToothFace() != dto.toothFace() || persisted.getQuadrant() != dto.quadrant()
                || persisted.getMaxillary() != dto.maxillary();
        if (!locationChanged) {
            return;
        }

        persisted.setScope(dto.scope());
        persisted.setTooth(dto.tooth());
        persisted.setToothFace(dto.toothFace());
        persisted.setQuadrant(dto.quadrant());
        persisted.setMaxillary(dto.maxillary());
    }

    /**
     * Completitud de steps (regla #6): un PrestationStepInstance vigente ausente de stepAdvancements
     * → 409, sin excepciones — incluida una prestación con actividad parcial declarada sobre solo
     * alguno de sus steps (los demás steps vigentes de esa misma prestación deben declararse igual,
     * sin cambios, para no disparar 409). Una prestación cancelada por la cascada (regla #8) ya quedó
     * deshabilitada antes de llegar acá y se excluye para no reevaluar steps sobre un registro ya
     * disable(); su propia completitud de sub-recurso se validó en applyPrestationInstanceDelta antes
     * de la cascada. Una prestación dada de baja explícita (enabled:false) ya validó la completitud
     * de sus propios steps dentro de disablePrestationInstanceExplicit y se excluye acá por el mismo
     * motivo.
     */
    private void validateStepCompleteness(List<PrestationInstance> persistedPrestations,
                                           Map<Long, PrestationInstanceUpdateRequestDTO> existingDtoByPrestationId,
                                           Map<Long, List<PrestationStepUpdateRequestDTO>> stepsByPrestationId,
                                           List<Long> cascadeCancelledPrestationIds) {
        for (PrestationInstance persisted : persistedPrestations) {
            Long prestationId = persisted.getId();
            if (cascadeCancelledPrestationIds.contains(prestationId)) {
                continue;
            }
            PrestationInstanceUpdateRequestDTO existingDto = existingDtoByPrestationId.get(prestationId);
            // baja explícita del padre ya validó la completitud de sus propios steps arriba, sin excepción
            if (existingDto != null && !existingDto.enabled()) {
                continue;
            }

            List<Long> requestedStepIds = stepsByPrestationId.getOrDefault(prestationId, List.of()).stream()
                    .map(PrestationStepUpdateRequestDTO::id)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            List<PrestationStepInstance> steps = prestationStepInstanceRepository.findByPrestationInstance(persisted);
            for (PrestationStepInstance step : steps) {
                if (!requestedStepIds.contains(step.getId())) {
                    throw new ConflictException("exception.updateConsultationInstanceUseCase.stepMissingFromRequest.user", null, "exception.updateConsultationInstanceUseCase.stepMissingFromRequest.log", new Object[]{step.getId(), prestationId, "UpdateConsultationInstanceUseCase", "validateStepCompleteness"}, LogLevel.ERROR);
                }
            }
        }
    }

    /**
     * Avanza una PrestationInstance que sigue viva en el request: valida el avance en contexto de
     * corrección, respeta la inmutabilidad de steps fuera de su propia ventana (regla #12, ADR-0029:
     * ausente o con status/enabled distinto al persistido → 409; mismo valor → se ignora sin error,
     * ADR-0024), da de baja los steps editables con enabled:false, mapea altas (id==null) a
     * PrestationStepAdvancementRequestDTO (decisión #14) y evalúa si la prestación queda completa.
     */
    private void advancePrestationInstance(ConsultationInstance consultationInstance, Long patientId,
                                            PrestationInstance persisted, List<PrestationStepUpdateRequestDTO> stepRequest, long correctionHours,
                                            UserSec authenticatedUser) {
        List<PrestationStepInstance> existingStepInstances = prestationStepInstanceRepository.findByPrestationInstance(persisted);

        Map<Long, PrestationStepUpdateRequestDTO> requestById = stepRequest.stream()
                .filter(dto -> dto.id() != null)
                .collect(Collectors.toMap(PrestationStepUpdateRequestDTO::id, dto -> dto, (a, b) -> a));

        // Altas de steps nuevos sobre esta prestación existente (id == null, regla #14) — únicas que
        // pasan por buildStepAdvancements, exclusivo de construir registros nuevos.
        List<PrestationStepAdvancementRequestDTO> newStepRequest = new ArrayList<>();
        for (PrestationStepUpdateRequestDTO stepDto : stepRequest) {
            if (stepDto.id() == null) {
                newStepRequest.add(new PrestationStepAdvancementRequestDTO(persisted.getId(), stepDto.prestationStepId(), stepDto.status()));
            }
        }

        for (PrestationStepInstance existingStep : existingStepInstances) {
            PrestationStepUpdateRequestDTO stepDto = requestById.get(existingStep.getId());

            if (!prestationInstanceDomainService.isStepEditable(existingStep, correctionHours)) {
                boolean sameValue = stepDto != null && stepDto.enabled() && stepDto.status() == existingStep.getStatus();
                if (!sameValue) {
                    throw new ConflictException("exception.updateConsultationInstanceUseCase.stepOutOfWindow.user", null, "exception.updateConsultationInstanceUseCase.stepOutOfWindow.log", new Object[]{existingStep.getId(), "UpdateConsultationInstanceUseCase", "advancePrestationInstance"}, LogLevel.ERROR);
                }
                // Fuera de ventana, referenciado con el mismo valor persistido: se ignora sin error (ADR-0024).
                continue;
            }

            if (stepDto == null) {
                continue;
            }
            if (!stepDto.enabled()) {
                existingStep.disable(authenticatedUser);
                continue;
            }

            // Step existente que avanza de estado: edición in-place (mismo id, ADR-0008) — nunca
            // construye un registro nuevo.
            PrestationStepAdvancementRequestDTO advancementDto = new PrestationStepAdvancementRequestDTO(persisted.getId(), existingStep.getStep().getId(), stepDto.status());
            prestationInstanceDomainService.validateStepAdvancement(patientId, persisted, advancementDto);
            prestationInstanceDomainService.applyStepAdvancement(existingStep, advancementDto);
        }

        for (PrestationStepAdvancementRequestDTO advancementDto : newStepRequest) {
            prestationInstanceDomainService.validateStepAdvancement(patientId, persisted, advancementDto);
        }

        List<PrestationStep> catalogSteps = prestationStepService.findAllPrestationStepsByPrestation(persisted.getType().getId());
        List<PrestationStepInstance> newStepInstances = prestationInstanceDomainService.buildStepAdvancements(catalogSteps, existingStepInstances, newStepRequest, consultationInstance, persisted);

        List<PrestationStepInstance> allStepInstances = new ArrayList<>(existingStepInstances);
        allStepInstances.addAll(newStepInstances);

        if (prestationInstanceDomainService.shouldComplete(catalogSteps, allStepInstances)) {
            persisted.complete();
        }
        prestationStepInstanceRepository.saveAll(newStepInstances);
    }

    /**
     * Construye las PrestationInstance nuevas a agregar (mismas reglas que Create — ve el estado
     * ya actualizado por el delta de existentes, decisión #5b).
     */
    private void buildNewPrestationInstances(ConsultationInstance consultationInstance, Consultation consultation,
                                              ConsultationInstanceUpdateRequestDTO dto, List<Odontogram> odontogramsSaved) {
        List<PrestationInstance> newPrestationInstances = new ArrayList<>();
        for (PrestationInstanceRequestDTO prestationDto : dto.prestationNew()) {

            if (prestationDto.prestationStepId() != null) {
                prestationInstanceDomainService.validatePrestationStep(prestationDto.prestationTypeId(), prestationDto.prestationStepId());
            }

            prestationInstanceDomainService.validatePrestationInstancePending(consultation.getPatient().getId(), prestationDto);

            PrestationType prestationType = prestationTypeService.findById(prestationDto.prestationTypeId());
            boolean stepAdvancementsHasNoRealActivity = dto.stepAdvancements().stream().noneMatch(PrestationStepUpdateRequestDTO::enabled);
            prestationInstanceDomainService.validatePrestationUnique(prestationType, dto.prestationNew().size(), stepAdvancementsHasNoRealActivity);

            if (prestationType.isRequiresLocation()) {
                prestationInstanceDomainService.validateLocation(prestationDto);
            }
            prestationInstanceDomainService.validateDiscount(prestationDto);

            Odontogram odontogram = null;
            if (prestationDto.odontogram() != null) {
                odontogram = prestationInstanceDomainService.matchOdontogram(odontogramsSaved, prestationDto.odontogram());
                if (odontogram == null) {
                    throw new ConflictException("exception.updateConsultationInstanceUseCase.odontogramLocationMismatch.user", null, "exception.updateConsultationInstanceUseCase.odontogramLocationMismatch.log", new Object[]{prestationDto.odontogram().tooth(), prestationDto.odontogram().toothFace(), "UpdateConsultationInstanceUseCase", "buildNewPrestationInstances"}, LogLevel.ERROR);
                }
            }

            BigDecimal price = prestationTypePriceService.currentPrice(prestationDto.prestationTypeId()).getPrice();
            Promotion promotion = promotionQueryService.findById(prestationDto.promotionId());
            BigDecimal promotionAmount = promotion != null ? promotion.calculateAmount(price) : null;

            newPrestationInstances.add(PrestationInstance.build(
                    consultationInstance,
                    prestationType,
                    odontogram,
                    PrestationInstanceStatus.IN_PROGRESS,
                    prestationDto.scope(),
                    prestationDto.tooth(),
                    prestationDto.toothFace(),
                    prestationDto.quadrant(),
                    prestationDto.maxillary(),
                    price,
                    promotion,
                    promotionAmount,
                    prestationDto.discountType(),
                    prestationDto.discountValue()
            ));
        }

        if (!newPrestationInstances.isEmpty()) {
            prestationInstanceRepository.saveAll(newPrestationInstances);
        }
    }
}
