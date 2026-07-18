package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionResponseDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.PromotionStatus;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.mapper.PromotionMapper;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model.Promotion;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.repository.IPromotionRepository;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Trunca la vigencia de una promoción al momento actual (ADR-0022), sin modificar startDate/endDate.
 * Terminal: no admite reactivación (§ 7 del design).
 */
@Service
public class FinishPromotionUseCase {

    private final PromotionQueryService promotionQueryService;
    private final PromotionDomainService promotionDomainService;
    private final IPromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;

    public FinishPromotionUseCase(PromotionQueryService promotionQueryService, PromotionDomainService promotionDomainService,
                                   IPromotionRepository promotionRepository, PromotionMapper promotionMapper) {
        this.promotionQueryService = promotionQueryService;
        this.promotionDomainService = promotionDomainService;
        this.promotionRepository = promotionRepository;
        this.promotionMapper = promotionMapper;
    }

    @LogAction(
            value = "finishPromotionUseCase.logAction.finish",
            args = {"#result.data.id", "#result.data.label", "#result.data.finishedAt"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    @Transactional
    public Response<PromotionResponseDTO> execute(Long id) {

        Promotion promotion = promotionQueryService.findById(id);

        PromotionStatus status = promotionDomainService.resolveStatus(
                promotion.getStartDate(), promotion.getEndDate(), promotion.getFinishedAt());

        if (status == PromotionStatus.FINISHED) {
            throw new ConflictException(
                    "exception.finishPromotionUseCase.alreadyFinished.user", null,
                    "exception.finishPromotionUseCase.alreadyFinished.log", new Object[]{id, "FinishPromotionUseCase", "execute"},
                    LogLevel.ERROR);
        }

        promotion.setFinishedAt(LocalDateTime.now());

        Promotion savedPromotion = promotionRepository.save(promotion);
        return new Response<>(true, null, promotionMapper.toDTO(savedPromotion));
    }
}