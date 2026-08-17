package com.odontologiaintegralfm.feature.consultation.core.prestationinstance.service;


import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationStepService;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.model.Odontogram;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationInstanceUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.dto.PrestationStepAdvancementRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.model.PrestationStepInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestationinstance.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class PrestationInstanceDomainService {

    private final PrestationStepService prestationStepService;
    private final IPrestationInstanceRepository prestationInstanceRepository;
    private final PrestationStepInstanceQueryService prestationStepInstanceQueryService;


    PrestationInstanceDomainService(PrestationStepService prestationStepService, IPrestationInstanceRepository prestationInstanceRepository, PrestationStepInstanceQueryService prestationStepInstanceQueryService) {
        this.prestationStepService = prestationStepService;
        this.prestationInstanceRepository = prestationInstanceRepository;
        this.prestationStepInstanceQueryService = prestationStepInstanceQueryService;
    }



    /**
     * Valída que la prestación tenga una ubicación válida y consistente.
     * Reglas:
     * - Debe tener odontograma O scope, nunca ambos ni ninguno.
     * - Si tiene scope, valída que el campo de ubicación correspondiente esté presente
     *   (ej: scope TOOTH requiere tooth, scope QUADRANT requiere quadrant).
     * - Si tiene odontograma, no se valida scope (retorna directo).
     */
    public void validateLocation(PrestationInstanceRequestDTO prestationInstanceRequestDTO) {

        if((prestationInstanceRequestDTO.odontogram() != null) && (prestationInstanceRequestDTO.scope() == null)){
            return;
        }


        if((prestationInstanceRequestDTO.odontogram() == null) && (prestationInstanceRequestDTO.scope() == null)){
            throw new BadRequestException("exception.prestationInstanceDomainService.validateLocation.odontogramAndScopeNull.user",null,"exception.prestationInstanceDomainService.validateLocation.odontogramAndScopeNull.log", new Object[]{"PrestationInstanceDomainService","validateLocation"}, LogLevel.ERROR);
        }

        if((prestationInstanceRequestDTO.odontogram() != null)&& (prestationInstanceRequestDTO.scope() != null)){
            throw new BadRequestException("exception.prestationInstanceDomainService.validateLocation.odontogramAndScopeData.user",null,"exception.prestationInstanceDomainService.validateLocation.odontogramAndScopeData.log", new Object[]{"PrestationInstanceDomainService","validateLocation"}, LogLevel.ERROR);

        }

        switch (prestationInstanceRequestDTO.scope()) {
            case TOOTH:
                if(prestationInstanceRequestDTO.tooth() == null){
                    throw new BadRequestException("exception.prestationInstanceDomainService.validateLocation.scopeTooth.user", null,"exception.prestationInstanceDomainService.validateLocation.scopeTooth.log", new Object[]{"PrestationInstanceDomainService","validateLocation"}, LogLevel.ERROR);
                }
                break;
            case TOOTH_FACE:
                if((prestationInstanceRequestDTO.toothFace() == null) || (prestationInstanceRequestDTO.tooth() == null)){
                    throw new BadRequestException("exception.prestationInstanceDomainService.validateLocation.scopeToothFace.user", null,"exception.prestationInstanceDomainService.validateLocation.scopeToothFace.log", new Object[]{prestationInstanceRequestDTO.tooth(),prestationInstanceRequestDTO.toothFace(),"PrestationInstanceDomainService","validateLocation"}, LogLevel.ERROR);
                }
                break;
            case QUADRANT:
                if(prestationInstanceRequestDTO.quadrant() == null){
                    throw new BadRequestException("exception.prestationInstanceDomainService.validateLocation.quadrant.user", null,"exception.prestationInstanceDomainService.validateLocation.quadrant.log", new Object[]{"PrestationInstanceDomainService","validateLocation"}, LogLevel.ERROR);
                }
                break;

            case MAXILLARY:
                if(prestationInstanceRequestDTO.maxillary() == null){
                    throw new BadRequestException("exception.prestationInstanceDomainService.validateLocation.maxillary.user", null,"exception.prestationInstanceDomainService.validateLocation.maxillary.log", new Object[]{"PrestationInstanceDomainService","validateLocation"}, LogLevel.ERROR);
                }
                break;


            default:
                if (
                        (prestationInstanceRequestDTO.tooth() != null)     ||
                        (prestationInstanceRequestDTO.toothFace() != null) ||
                        (prestationInstanceRequestDTO.quadrant() != null)  ||
                        (prestationInstanceRequestDTO.maxillary() != null)) {

                    throw new BadRequestException("exception.prestationInstanceDomainService.validateLocation.fullMouth.user", null,"exception.prestationInstanceDomainService.validateLocation.fullMouth.log", new Object[]{prestationInstanceRequestDTO.tooth(),prestationInstanceRequestDTO.toothFace(),prestationInstanceRequestDTO.quadrant(),prestationInstanceRequestDTO.maxillary(),"PrestationInstanceDomainService","validateLocation"}, LogLevel.ERROR);
                }
        }
    }

    /**
     * Valída que no se apliquen simultáneamente una promoción y un descuento manual.
     * Solo puede existir uno de los dos en una misma prestación.
     */

    public void validateDiscount(PrestationInstanceRequestDTO prestationInstanceRequestDTO) {
        if(prestationInstanceRequestDTO.promotionId() != null && prestationInstanceRequestDTO.discountValue() != null){
            throw new BadRequestException("exception.prestationInstanceDomainService.validateDiscount.user", null,"exception.prestationInstanceDomainService.validateDiscount.log", new Object[]{prestationInstanceRequestDTO.promotionId(),prestationInstanceRequestDTO.discountValue(),"PrestationInstanceDomainService","validateDiscount"}, LogLevel.ERROR);
        }

        boolean hasType  = prestationInstanceRequestDTO.discountType()  != null;
        boolean hasValue = prestationInstanceRequestDTO.discountValue() != null;
        if(hasType != hasValue){
            throw new BadRequestException("exception.prestationInstanceDomainService.validateDiscount.incomplete.user", null,"exception.prestationInstanceDomainService.validateDiscount.incomplete.log", new Object[]{prestationInstanceRequestDTO.discountType(),prestationInstanceRequestDTO.discountValue(),"PrestationInstanceDomainService","validateDiscount"}, LogLevel.ERROR);
        }
    }



    /**
     * Valída que el step enviado pertenezca al tipo de prestación indicado.
     * Lanza BadRequestException si el step no forma parte del workflow de esa prestación.
     */
    public void validatePrestationStep(Long prestationId, Long prestationStepId) {

        List<PrestationStep> prestationSteps = prestationStepService.findAllPrestationStepsByPrestation(prestationId);

        boolean exists =  prestationSteps.stream()
                .anyMatch(prestationStep -> prestationStep.getId().equals(prestationStepId));


        if(!exists){
            throw new BadRequestException("exception.validatePrestationStep.user",null,"exception.validatePrestationStep.log",new Object[]{prestationStepId, prestationId,"PrestationInstanceDomainService","validatePrestationStep"},LogLevel.ERROR);
        }

    }


    /**
     * Valída que una prestación marcada como "unique" (isUnique) sea la única de su tipo en el request.
     * Se rechaza si hay más de una prestación nueva del mismo tipo, o si además del alta hay avances
     * de steps sobre prestaciones existentes en el mismo request.
     */
    public void validatePrestationUnique(PrestationType prestationType, int newCount, boolean stepAdvancementsEmpty) {
        if(prestationType.isUnique() && (newCount > 1 || !stepAdvancementsEmpty)){
            throw new ConflictException("exception.createConsultationInstanceUseCase.prestationUnique.user", new Object[]{prestationType.getName()}, "exception.createConsultationInstanceUseCase.prestationUnique.log", new Object[]{prestationType.getId(), prestationType.getName(), "PrestationInstanceDomainService", "validatePrestationUnique"}, LogLevel.ERROR);
        }
    }

    /**
     * Matchea la ubicación de una prestación (odontograma) contra la lista de Odontogram guardados en el mismo request.
     * Retorna el Odontogram correspondiente, o null si no hay match — la decisión de qué excepción lanzar
     * (400 en create, 409 en corrección) queda en cada UseCase caller.
     */
    public Odontogram matchOdontogram(List<Odontogram> odontogramsSaved, OdontogramRequestDTO odontogramRequestDTO) {
        return odontogramsSaved.stream()
                .filter(o ->
                        o.getTooth() == odontogramRequestDTO.tooth() &&
                        o.getToothFace() == odontogramRequestDTO.toothFace())
                .findFirst()
                .orElse(null);
    }

    /**
     * Valída que no exista una PrestationInstance IN_PROGRESS para el mismo paciente, tipo y ubicación (odontograma o scope).
     * Si existe y es el mismo step, se acepta únicamente si el nuevo estado es COMPLETED
     * (avance de step válido). Caso contrario lanza ConflictException.
     */

    public void validatePrestationInstancePending(Long patientId, PrestationInstanceRequestDTO prestationInstanceRequestDTO) {
        List<PrestationInstance> prestationInstances = prestationInstanceRepository.findPrestationInstanceByPatientAndStatus(patientId, PrestationInstanceStatus.IN_PROGRESS);
        if(prestationInstances.isEmpty()) return;

        for(PrestationInstance prestationInstance : prestationInstances){
            //Validamos que sea el mismo tipo de prestación.
            if(prestationInstance.getType().getId().equals(prestationInstanceRequestDTO.prestationTypeId())){
                //Validamos si es misma ubicación.
                //1. Odontograma
                if(prestationInstance.getOdontogram() != null && prestationInstanceRequestDTO.odontogram() != null){
                    if (prestationInstance.getOdontogram().getTooth()     == prestationInstanceRequestDTO.odontogram().tooth() &&
                        prestationInstance.getOdontogram().getToothFace() == prestationInstanceRequestDTO.odontogram().toothFace()
                    ) {
                        // Exception siempre que haya una PrestationInstance IN_PROGRESS en la misma ubicación para el mismo tipo
                        throw new ConflictException("exception.validatePrestationInstancePending.odontogram.user", null, "exception.validatePrestationInstancePending.odontogram.log", new Object[]{prestationInstance.getId(), prestationInstance.getOdontogram().getTooth(), prestationInstance.getOdontogram().getToothFace(), prestationInstanceRequestDTO.prestationStepId(), prestationInstanceRequestDTO.prestationStepStatus(), "PrestationInstanceDomainService", "validatePrestationInstancePending"}, LogLevel.ERROR);
                    }
                }
                //2. Scope
                if (prestationInstance.getScope() != null && prestationInstanceRequestDTO.scope() != null) {
                    if (prestationInstance.getScope() == prestationInstanceRequestDTO.scope()) {
                        if ((prestationInstance.getTooth() == prestationInstanceRequestDTO.tooth() && prestationInstance.getToothFace() == prestationInstanceRequestDTO.toothFace()) &&
                                prestationInstance.getQuadrant() == prestationInstanceRequestDTO.quadrant() &&
                                prestationInstance.getMaxillary() == prestationInstanceRequestDTO.maxillary()
                        ) {
                            // Exception siempre que haya una PrestationInstance IN_PROGRESS en la misma ubicación para el mismo tipo
                            throw new ConflictException("exception.validatePrestationInstancePending.scope.user", null, "exception.validatePrestationInstancePending.scope.log", new Object[]{prestationInstance.getId(), prestationInstance.getScope(), prestationInstance.getTooth(), prestationInstance.getToothFace(), prestationInstance.getQuadrant(), prestationInstance.getMaxillary(), "PrestationInstanceDomainService", "validatePrestationInstancePending"}, LogLevel.ERROR);
                        }
                    }
                }
            }
        }
    }

    /**
     * Evalúa si el step recibido ya existe como IN_PROGRESS en la prestación dada.
     * Retorna true si el step está IN_PROGRESS y el estado nuevo NO es COMPLETED
     * (lo que representaría un conflicto o duplicado inválido).
     */
    private boolean sameState(PrestationInstance prestationInstance, Long prestationStepId, PrestationStepStatus statusDTO , PrestationStepStatus prestationStepStatus){
        List<PrestationStepInstance> prestationStepsInstance = prestationStepInstanceQueryService.findByPrestationAndStepIdAndStatus(prestationInstance, prestationStepId, prestationStepStatus);
        if (!prestationStepsInstance.isEmpty()) {
            // Si es mismo step solo debería recibir estado completado.
            return !statusDTO.equals(PrestationStepStatus.FINISHED);
        }
        return false;
    }


    /**
     * Valida la transición de estado de un step (required/omitted, estado final, IN_PROGRESS→COMPLETED)
     * sin construir ni persistir nada. Comparte esta validación de negocio {@link #buildStepAdvancements}
     * (altas) y {@link #applyStepAdvancement} (edición in-place de un step existente que avanza) — decisión
     * #14 § 2 del design: ambos métodos difieren solo en qué hacen con el resultado, no en la regla.
     */
    private void validateStepTransition(PrestationStep catalogStep, PrestationStepStatus existingStatus, PrestationStepStatus newStatus, Long prestationInstanceId) {
        // Un step obligatorio no puede omitirse
        if (newStatus == PrestationStepStatus.OMITTED && catalogStep.isRequired()) {
            throw new BadRequestException("exception.prestationInstanceDomainService.validateAndBuild.omittedRequired.user", null, "exception.prestationInstanceDomainService.validateAndBuild.omittedRequired.log", new Object[]{catalogStep.getId(), prestationInstanceId, "PrestationInstanceDomainService", "validateStepTransition"}, LogLevel.ERROR);
        }

        if (existingStatus != null) {
            // Si el step ya tiene un estado final, no se puede volver a registrar
            if (existingStatus == PrestationStepStatus.FINISHED || existingStatus == PrestationStepStatus.OMITTED) {
                throw new ConflictException("exception.prestationInstanceDomainService.validateAndBuild.alreadyFinal.user", null, "exception.prestationInstanceDomainService.validateAndBuild.alreadyFinal.log", new Object[]{catalogStep.getId(), existingStatus, prestationInstanceId, "PrestationInstanceDomainService", "validateStepTransition"}, LogLevel.ERROR);
            }
            // Si el step está en progreso, solo se acepta completarlo
            if (existingStatus == PrestationStepStatus.IN_PROGRESS && newStatus != PrestationStepStatus.FINISHED) {
                throw new ConflictException("exception.prestationInstanceDomainService.validateAndBuild.inProgressNotCompleted.user", null, "exception.prestationInstanceDomainService.validateAndBuild.inProgressNotCompleted.log", new Object[]{catalogStep.getId(), newStatus, prestationInstanceId, "PrestationInstanceDomainService", "validateStepTransition"}, LogLevel.ERROR);
            }
        }
    }

    /**
     * Valida y construye los registros de avance de steps para una prestación existente — exclusivo de
     * altas (Create y steps id==null sobre prestación existente en Update, decisión #14 § 2). Nunca edita
     * un registro persistido: para eso está {@link #applyStepAdvancement}.
     *
     * Recibe:
     * - Los steps del catálogo (qué steps existen y en qué orden)
     * - Los steps ya guardados en base para esa prestación (qué ya ocurrió antes)
     * - Los steps que llegan en el request (qué quiere registrar el odontólogo ahora)
     * - La consulta y la prestación a las que pertenecen
     *
     * Lo que hace, paso a paso:
     * 1. Arma un mapa del catálogo para buscar cada step rápido por su ID.
     * 2. Ordena los steps del request según la posición del catálogo (no el orden de llegada).
     * 3. Carga en un mapa los estados ya guardados en base, para saber qué está resuelto.
     * 4. Por cada step del request, en orden de posición:
     *    - Valida la transición de estado (validateStepTransition).
     *    - Verifica que todos los steps anteriores ya estén resueltos (base o mismo request).
     *    - Construye el registro y lo marca como resuelto para que los siguientes lo vean.
     * 5. Devuelve la lista de registros lista para guardar en base.
     */
    public List<PrestationStepInstance> buildStepAdvancements(
            List<PrestationStep> catalogSteps,
            List<PrestationStepInstance> existingInstances,
            List<PrestationStepAdvancementRequestDTO> dtos,
            ConsultationInstance consultationInstance,
            PrestationInstance prestationInstance) {

        // Mapa del catálogo: stepId → PrestationStep, para buscar rápido sin iterar
        Map<Long, PrestationStep> catalogMap = catalogSteps.stream()
                .collect(Collectors.toMap(PrestationStep::getId, s -> s));

        // Ordena los steps del request por posición del catálogo (fuente de verdad del orden)
        List<PrestationStepAdvancementRequestDTO> sortedDtos = dtos.stream()
                .sorted(Comparator.comparingInt(dto -> catalogMap.get(dto.prestationStepId()).getPosition()))
                .collect(Collectors.toList());

        // Carga los estados ya guardados en base como punto de partida de lo resuelto
        Map<Long, PrestationStepStatus> resolvedSteps = new HashMap<>();
        for (PrestationStepInstance existing : existingInstances) {
            resolvedSteps.put(existing.getStep().getId(), existing.getStatus());
        }

        List<PrestationStepInstance> result = new ArrayList<>();

        for (PrestationStepAdvancementRequestDTO dto : sortedDtos) {
            PrestationStep catalogStep = catalogMap.get(dto.prestationStepId());

            validateStepTransition(catalogStep, resolvedSteps.get(dto.prestationStepId()), dto.status(), prestationInstance.getId());

            // Verifica que todos los steps anteriores (por posición) ya estén resueltos,
            // ya sea en base o registrados en este mismo request
            for (PrestationStep previousStep : catalogSteps) {
                if (previousStep.getPosition() < catalogStep.getPosition()) {
                    PrestationStepStatus previousStatus = resolvedSteps.get(previousStep.getId());
                    boolean isResolved = previousStatus == PrestationStepStatus.FINISHED || previousStatus == PrestationStepStatus.OMITTED;
                    if (!isResolved) {
                        throw new BadRequestException("exception.prestationInstanceDomainService.validateAndBuild.previousNotResolved.user", null, "exception.prestationInstanceDomainService.validateAndBuild.previousNotResolved.log", new Object[]{previousStep.getId(), catalogStep.getId(), prestationInstance.getId(), "PrestationInstanceDomainService", "buildStepAdvancements"}, LogLevel.ERROR);
                    }
                }
            }

            // Construye el registro listo para persistir
            result.add(PrestationStepInstance.build(prestationInstance, consultationInstance, catalogStep, dto.status()));

            // Marca el step como resuelto para que los siguientes del mismo request lo vean
            resolvedSteps.put(dto.prestationStepId(), dto.status());
        }

        return result;
    }

    /**
     * Avanza de estado un {@link PrestationStepInstance} YA PERSISTIDO, editándolo in-place
     * (mismo id, ADR-0008) — nunca construye un registro nuevo. Ese camino ({@link #buildStepAdvancements})
     * es exclusivo de altas.
     */
    public void applyStepAdvancement(PrestationStepInstance existingStep, PrestationStepAdvancementRequestDTO dto) {
        validateStepTransition(existingStep.getStep(), existingStep.getStatus(), dto.status(), dto.prestationInstanceId());
        existingStep.setStatus(dto.status());
    }


    /**
     * Evalúa si la prestación puede darse por completada.
     * Retorna true cuando todos los steps del catálogo tienen un registro con estado COMPLETED u OMITTED.
     * Recibe los steps del catálogo y todos los registros existentes (base + los recién construidos del request).
     */
    public boolean shouldComplete(List<PrestationStep> catalogSteps, List<PrestationStepInstance> allInstances) {

        // Arma un mapa de los estados actuales: stepId → estado
        Map<Long, PrestationStepStatus> resolvedSteps = new HashMap<>();
        for (PrestationStepInstance instance : allInstances) {
            resolvedSteps.put(instance.getStep().getId(), instance.getStatus());
        }

        // La prestación está completa si todos los steps del catálogo tienen estado final
        for (PrestationStep catalogStep : catalogSteps) {
            PrestationStepStatus status = resolvedSteps.get(catalogStep.getId());
            if (status != PrestationStepStatus.FINISHED && status != PrestationStepStatus.OMITTED) {
                return false;
            }
        }

        return true;
    }


    /**
     * Valída un avance de step sobre una prestación existente. Verifica:
     * 1. Que la prestación pertenezca al mismo paciente — 409 (ADR-0026: depende de mirar el
     *    estado persistido de la prestación, el mismo input sería válido con otro paciente dueño).
     * 2. Que la prestación esté IN_PROGRESS — 409 (mismo criterio, ADR-0026).
     * 3. Que el step pertenezca al tipo de esa prestación.
     * 4. Que no exista un step duplicado en estado inválido.
     */
    public void validateStepAdvancement(Long patientId, PrestationInstance existingPrestation, PrestationStepAdvancementRequestDTO dto) {

        Long prestationPatientId = existingPrestation.getConsultationInstance().getConsultation().getPatient().getId();
        if (!prestationPatientId.equals(patientId)) {
            throw new ConflictException("exception.prestationInstanceDomainService.validateStepAdvancement.notOwner.user", null, "exception.prestationInstanceDomainService.validateStepAdvancement.notOwner.log", new Object[]{existingPrestation.getId(), patientId, "PrestationInstanceDomainService", "validateStepAdvancement"}, LogLevel.ERROR);
        }

        if (existingPrestation.getStatus() != PrestationInstanceStatus.IN_PROGRESS) {
            throw new ConflictException("exception.prestationInstanceDomainService.validateStepAdvancement.notInProgress.user", new Object[]{existingPrestation.getId()}, "exception.prestationInstanceDomainService.validateStepAdvancement.notInProgress.log", new Object[]{existingPrestation.getId(), existingPrestation.getStatus(), "PrestationInstanceDomainService", "validateStepAdvancement"}, LogLevel.ERROR);
        }

        validatePrestationStep(existingPrestation.getType().getId(), dto.prestationStepId());

        boolean conflict = sameState(existingPrestation, dto.prestationStepId(), dto.status(), PrestationStepStatus.IN_PROGRESS);
        if (conflict) {
            throw new ConflictException("exception.prestationInstanceDomainService.validateStepAdvancement.duplicateStep.user", null, "exception.prestationInstanceDomainService.validateStepAdvancement.duplicateStep.log", new Object[]{existingPrestation.getId(), dto.prestationStepId(), "PrestationInstanceDomainService", "validateStepAdvancement"}, LogLevel.ERROR);
        }
    }

    /**
     * Valída si un PrestationStepInstance sigue dentro de su propia ventana de corrección,
     * medida desde el createdAt de la ConsultationInstance de origen del step (no de la
     * ConsultationInstance que se está corrigiendo) — ADR-0029: inmutabilidad por registro.
     */
    public boolean isStepEditable(PrestationStepInstance step, long correctionHours) {
        LocalDateTime deadline = step.getConsultationInstance().getCreatedAt().plusHours(correctionHours);
        return !LocalDateTime.now().isAfter(deadline);
    }

    /**
     * Valída si una PrestationInstance sigue dentro de su propia ventana de corrección, medida
     * desde su propio createdAt — mismo criterio que isStepEditable pero a nivel del contenedor
     * (regla #13, ADR-0029: sin esta guarda sería posible editar una prestación nacida en una
     * consulta vieja simplemente corrigiendo una consulta posterior).
     */
    public boolean isPrestationInstanceEditable(PrestationInstance prestationInstance, long correctionHours) {
        LocalDateTime deadline = prestationInstance.getCreatedAt().plusHours(correctionHours);
        return !LocalDateTime.now().isAfter(deadline);
    }

    /**
     * Valída que una PrestationInstance existente no reciba una edición de ubicación inválida
     * (regla #17/#27, ADR-0026 — depende de mirar el estado persistido, nunca 400):
     * - Si tiene Odontogram asociado y el request trae algún campo de ubicación con valor → 409
     *   (la ubicación se corrige editando el Odontogram, no estos campos).
     * - Si NO tiene Odontogram asociado, su tipo requiere ubicación, y los 5 campos llegan todos
     *   null → 409 (por ADR-0024 el front manda el objeto completo; todo-null es la ubicación real
     *   declarada, no ausencia de intención).
     */
    public void validateLocationNotOverridable(PrestationInstance persisted, PrestationInstanceUpdateRequestDTO dto) {
        boolean anyLocationFieldPresent = dto.scope() != null || dto.tooth() != null || dto.toothFace() != null
                || dto.quadrant() != null || dto.maxillary() != null;

        if (persisted.getOdontogram() != null) {
            if (anyLocationFieldPresent) {
                throw new ConflictException("exception.prestationInstanceDomainService.validateLocationNotOverridable.hasOdontogram.user", null, "exception.prestationInstanceDomainService.validateLocationNotOverridable.hasOdontogram.log", new Object[]{persisted.getId(), "PrestationInstanceDomainService", "validateLocationNotOverridable"}, LogLevel.ERROR);
            }
            return;
        }

        if (persisted.getType().isRequiresLocation() && !anyLocationFieldPresent) {
            throw new ConflictException("exception.prestationInstanceDomainService.validateLocationNotOverridable.locationRequired.user", null, "exception.prestationInstanceDomainService.validateLocationNotOverridable.locationRequired.log", new Object[]{persisted.getId(), "PrestationInstanceDomainService", "validateLocationNotOverridable"}, LogLevel.ERROR);
        }
    }

    /**
     * Valída que un scope de ubicación sea uno de los permitidos para el PrestationType dado
     * (regla #29, ADR-0026 — depende de mirar el catálogo persistido de la prestación). Se invoca
     * en dos direcciones de edición: al editar el scope directo de una PrestationInstance sin
     * Odontogram, y al editar un Odontogram con PrestationInstance viva vinculada (la ubicación
     * nueva del Odontogram debe ser compatible con el PrestationType de esa prestación).
     */
    public void validateScopeAllowedForType(PrestationType type, PrestationScopeType scope) {
        if (scope != null && !type.getAllowedScopes().contains(scope)) {
            throw new ConflictException("exception.prestationInstanceDomainService.validateScopeAllowedForType.user", null, "exception.prestationInstanceDomainService.validateScopeAllowedForType.log", new Object[]{scope, type.getId(), "PrestationInstanceDomainService", "validateScopeAllowedForType"}, LogLevel.ERROR);
        }
    }

}


