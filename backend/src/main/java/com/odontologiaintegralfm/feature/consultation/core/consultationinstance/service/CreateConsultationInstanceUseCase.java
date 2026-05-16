package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationStep;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationStepService;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationTypePriceService;
import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.service.PrestationTypeService;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service.PromotionService;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.service.TreatmentConditionService;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.service.TreatmentService;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationStatusType;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationRepository;
import com.odontologiaintegralfm.feature.consultation.core.consultation.service.ConsultationQueryService;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto.ConsultationInstanceRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.dto.ConsultationInstanceResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.repository.IConsultationInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.dto.OdontogramRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.mapper.OdontogramMapper;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.model.Odontogram;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.repository.IOdontogramRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestation.dto.PrestationInstanceRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestation.dto.PrestationStepAdvancementRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.prestation.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestation.enums.PrestationStepStatus;
import com.odontologiaintegralfm.feature.consultation.core.prestation.mapper.PrestationInstanceMapper;
import com.odontologiaintegralfm.feature.consultation.core.prestation.model.PrestationInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestation.model.PrestationStepInstance;
import com.odontologiaintegralfm.feature.consultation.core.prestation.repository.IPrestationInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestation.repository.IPrestationStepInstanceRepository;
import com.odontologiaintegralfm.feature.consultation.core.prestation.service.PrestationInstanceDomainService;
import com.odontologiaintegralfm.feature.consultation.core.prestation.service.PrestationInstanceQueryService;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CreateConsultationInstanceUseCase {

    private final ConsultationQueryService consultationQueryService;
    private final IConsultationInstanceRepository consultationInstanceRepository;
    private final IOdontogramRepository odontogramRepository;
    private final IPrestationInstanceRepository prestationInstanceRepository;
    private final PrestationInstanceQueryService prestationInstanceQueryService;
    private final PrestationInstanceDomainService prestationInstanceDomainService;
    private final PrestationTypeService prestationTypeService;
    private final PrestationTypePriceService prestationTypePriceService;
    private final PrestationStepService prestationStepService;
    private final IPrestationStepInstanceRepository prestationStepInstanceRepository;
    private final TreatmentService treatmentService;
    private final TreatmentConditionService treatmentConditionService;
    private final PromotionService promotionService;
    private final OdontogramMapper odontogramMapper;
    private final PrestationInstanceMapper prestationInstanceMapper;
    private final MessageSource messageSource;

    public CreateConsultationInstanceUseCase(ConsultationQueryService consultationQueryService,
                                             IConsultationInstanceRepository consultationInstanceRepository,
                                             PrestationInstanceDomainService prestationInstanceDomainService,
                                             IOdontogramRepository odontogramRepository,
                                             IPrestationInstanceRepository prestationInstanceRepository,
                                             PrestationInstanceQueryService prestationInstanceQueryService,
                                             PrestationTypeService prestationTypeService,
                                             PrestationTypePriceService prestationTypePriceService,
                                             PrestationStepService prestationStepService,
                                             IPrestationStepInstanceRepository prestationStepInstanceRepository,
                                             TreatmentService treatmentService,
                                             TreatmentConditionService treatmentConditionService,
                                             PromotionService promotionService,
                                             OdontogramMapper odontogramMapper,
                                             PrestationInstanceMapper prestationInstanceMapper,
                                             MessageSource messageSource) {
        this.consultationQueryService = consultationQueryService;
        this.consultationInstanceRepository = consultationInstanceRepository;
        this.prestationInstanceDomainService = prestationInstanceDomainService;
        this.odontogramRepository = odontogramRepository;
        this.prestationInstanceRepository = prestationInstanceRepository;
        this.prestationInstanceQueryService = prestationInstanceQueryService;
        this.prestationStepService = prestationStepService;
        this.prestationStepInstanceRepository = prestationStepInstanceRepository;
        this.prestationTypeService = prestationTypeService;
        this.prestationTypePriceService = prestationTypePriceService;
        this.treatmentService = treatmentService;
        this.treatmentConditionService = treatmentConditionService;
        this.promotionService = promotionService;
        this.odontogramMapper = odontogramMapper;
        this.prestationInstanceMapper = prestationInstanceMapper;
        this.messageSource = messageSource;
    }

    @LogAction(
            value = "createConsultationInstanceUseCase.logAction.create.ok",
            args = {"#result.data.id"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    @Transactional
    public Response<ConsultationInstanceResponseDTO> execute(ConsultationInstanceRequestDTO consultationInstanceRequestDTO) {

        //Validamos que exista la consulta.
        Consultation consultation = consultationQueryService.findById(consultationInstanceRequestDTO.consultationId());

        //Validamos que la consulta esté con estado IN_CONSULTATION
        if(!consultation.getStatus().equals(ConsultationStatusType.IN_CONSULTATION)) {
            throw new ConflictException("exception.createConsultationInstanceUseCase.statusConflict.user", null, "exception.createConsultationInstanceUseCase.statusConflict.log", new Object[]{consultation.getId(),consultation.getStatus(),"createConsultationInstanceUseCase", "execute"}, LogLevel.ERROR);
        }

        //Validamos que no exista otra instancia para esa consulta
        ConsultationInstance consultationInstancePrevious = consultationInstanceRepository.findByConsultationId(consultationInstanceRequestDTO.consultationId());

        if(consultationInstancePrevious != null){
            throw new ConflictException("exception.createConsultationInstanceUseCase.existingInstance.user", null, "exception.createConsultationInstanceUseCase.existingInstance.log", new Object[]{consultationInstancePrevious.getId(),consultationInstanceRequestDTO.consultationId(),"createConsultationInstanceUseCase", "execute"}, LogLevel.ERROR);
        }


        //Construimos consulta.
        ConsultationInstance consultationInstance = ConsultationInstance.build(consultation, consultationInstanceRequestDTO.observation());
        consultationInstanceRepository.save(consultationInstance);


        // --- Odontograma ---
        //Construimos Odontograma desde DTO.
        List<Odontogram> odontogramList = new ArrayList<>();
        for(OdontogramRequestDTO odontogramRequestDTO: consultationInstanceRequestDTO.odontogram()){
            odontogramList.add(Odontogram.build(
                    consultationInstance,
                    odontogramRequestDTO.tooth(),
                    odontogramRequestDTO.toothFace(),
                    treatmentService.findById(odontogramRequestDTO.treatmentId()),
                    treatmentConditionService.findById(odontogramRequestDTO.treatmentConditionId())
            ));
        }

      List<Odontogram> odontogramsSaved =  odontogramRepository.saveAll(odontogramList);


        // --- Prestaciones Nuevas ---
        //Realizamos validaciones de dominio y construcción de entidad PrestationInstance
        List<PrestationInstance> prestationInstanceList = new ArrayList<>(List.of());
        for(PrestationInstanceRequestDTO prestationInstanceRequestDTO : consultationInstanceRequestDTO.prestationNew()){

            //Validamos que el step que envían en la request pertenezca a la misma prestación.
            if(prestationInstanceRequestDTO.prestationStepId() != null){
                prestationInstanceDomainService.validatePrestationStep(prestationInstanceRequestDTO.prestationTypeId(), prestationInstanceRequestDTO.prestationStepId());
            }

            //Validamos que si existe alguna PrestationInstance no finalizada para paciente + tipo + ubicación.
            prestationInstanceDomainService.validatePrestationInstancePending(consultation.getPatient().getId(), prestationInstanceRequestDTO);


            //Validamos si la prestación es "unique" debe ser unica en la lista.
            PrestationType prestationType = prestationTypeService.findById(prestationInstanceRequestDTO.prestationTypeId());
            if(prestationType.isUnique() &&
                    (consultationInstanceRequestDTO.prestationNew().size()>1 ||
                    !consultationInstanceRequestDTO.stepAdvancements().isEmpty())){
                throw new ConflictException("exception.createConsultationInstanceUseCase.prestationUnique.user", new Object[]{prestationType.getName()}, "exception.createConsultationInstanceUseCase.prestationUnique.log", new Object[]{prestationType.getId(),prestationType.getName(),"createConsultationInstanceUseCase", "execute"}, LogLevel.ERROR);
            }
            if(prestationType.isRequiresLocation()){
                prestationInstanceDomainService.validateLocation(prestationInstanceRequestDTO);
            }
            prestationInstanceDomainService.validateDiscount(prestationInstanceRequestDTO);


            /* Validamos si la prestación tiene como ubicación Odontograma.
               Sí tiene -> Matcheamos la ubicación de la prestación con el OdontogramRequestDTO recibido en ConsultationInstanceRequestDTO.
             */
            Odontogram odontogram = null;
            if(prestationInstanceRequestDTO.odontogram()!=null){
                 odontogram = odontogramsSaved.stream()
                        .filter(o ->
                                o.getTooth() == prestationInstanceRequestDTO.odontogram().tooth() &&
                                o.getToothFace() == prestationInstanceRequestDTO.odontogram().toothFace())
                        .findFirst()
                        .orElseThrow(() -> new BadRequestException("exception.createConsultationInstanceUseCase.odontogramNotFound.user", null, "exception.createConsultationInstanceUseCase.odontogramNotFound.log", new Object[]{prestationInstanceRequestDTO.odontogram().tooth(), prestationInstanceRequestDTO.odontogram().toothFace(), "createConsultationInstanceUseCase", "execute"}, LogLevel.ERROR));
            }



            //Se construye PrestationInstance
            PrestationInstance prestationInstance = PrestationInstance.build(
                    consultationInstance,
                    prestationType,
                    odontogram,
                    PrestationInstanceStatus.IN_PROGRESS, // Siempre se crea al nacer en este estado. Después se evalúa si todos los steps están COMPLETED
                    prestationInstanceRequestDTO.scope(),
                    prestationInstanceRequestDTO.tooth(),
                    prestationInstanceRequestDTO.toothFace(),
                    prestationInstanceRequestDTO.quadrant(),
                    prestationInstanceRequestDTO.maxillary(),
                    prestationTypePriceService.currentPrice(prestationInstanceRequestDTO.prestationTypeId()).getPrice(),
                    promotionService.findById(prestationInstanceRequestDTO.promotionId()),
                    prestationInstanceRequestDTO.discountType(),
                    prestationInstanceRequestDTO.discountValue()
           );
            prestationInstanceList.add(prestationInstance);
        }
        prestationInstanceRepository.saveAll(prestationInstanceList);



        // -- Avance de steps sobre Prestaciones Existentes ---

        //Validamos que solo existan steps con estados IN_PROGRESS, COMPLETED y OMITTED
        for(PrestationStepAdvancementRequestDTO prestationStepAdvancementRequestDTO : consultationInstanceRequestDTO.stepAdvancements()){
            if(prestationStepAdvancementRequestDTO.status().equals(PrestationStepStatus.CANCELLED)){
                throw new ConflictException("exception.createConsultationInstanceUseCase.stepsStatus.user",new Object[]{prestationStepAdvancementRequestDTO.status()},"exception.createConsultationInstanceUseCase.stepsStatus.log", new Object[]{prestationStepAdvancementRequestDTO.status(),prestationStepAdvancementRequestDTO.prestationStepId(),prestationStepAdvancementRequestDTO.prestationInstanceId() ,"createConsultationInstanceUseCase","execute"}, LogLevel.ERROR);
            }
        }

        //Agrupamos `stepAdvancements` por `prestationInstanceId`
        Map<Long, List<PrestationStepAdvancementRequestDTO>> prestationInstanceGroup = consultationInstanceRequestDTO.stepAdvancements()
                .stream()
                .collect(Collectors.groupingBy(PrestationStepAdvancementRequestDTO::prestationInstanceId));



        for(Map.Entry<Long, List<PrestationStepAdvancementRequestDTO>> entry : prestationInstanceGroup.entrySet()){
            PrestationInstance prestationInstance = prestationInstanceQueryService.findById(entry.getKey());

            // 1. Validar cada step: que sea del paciente y pertenezca a la prestación
            for(PrestationStepAdvancementRequestDTO dto : entry.getValue()){
                prestationInstanceDomainService.validateStepAdvancement(consultation.getPatient().getId(), prestationInstance, dto);
            }

            // 2. Traer steps del catálogo de esa prestación (para saber el orden)
            List<PrestationStep> catalogSteps = prestationStepService.findAllPrestationStepsByPrestation(prestationInstance.getType().getId());

            // 3. Traer steps ya guardados en base para esa prestación
            List<PrestationStepInstance> existingStepInstances = prestationStepInstanceRepository.findByPrestationInstance(prestationInstance);

            // 4. Validar orden y construir los nuevos steps
            List<PrestationStepInstance> newStepInstances = prestationInstanceDomainService.validateAndBuildStepAdvancements(catalogSteps, existingStepInstances, entry.getValue(), consultationInstance, prestationInstance);

            // 5. Evaluar si la prestación queda completada con los steps nuevos + los que ya estaban en base
            List<PrestationStepInstance> allStepInstances = new ArrayList<>(existingStepInstances);
            allStepInstances.addAll(newStepInstances);

            // 6. Si quedó completada, cambiarle el estado y guardarla
            if (prestationInstanceDomainService.shouldComplete(catalogSteps, allStepInstances)) {
                prestationInstance.complete();
                prestationInstanceRepository.save(prestationInstance);
            }
            // 7. Guardar los steps nuevos
            prestationStepInstanceRepository.saveAll(newStepInstances);
        }

        return new Response<>(
                true,
                messageSource.getMessage("createConsultationInstanceUseCase.create.ok", null, LocaleContextHolder.getLocale()),
                new ConsultationInstanceResponseDTO(
                        consultationInstance.getId(),
                        odontogramsSaved.stream().map(odontogramMapper::toDTO).toList(),
                        prestationInstanceList.stream().map(prestationInstanceMapper::toDTO).toList(),
                        consultationInstance.getObservation()
                )
        );
    }

}


