package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionResponseDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.mapper.PromotionMapper;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model.Promotion;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.repository.IPromotionRepository;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnablePromotionUseCase {

    private final IPromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;
    private final AuthenticatedUserService authenticatedUserService;
    private final MessageSource messageSource;

    public EnablePromotionUseCase(IPromotionRepository promotionRepository, PromotionMapper promotionMapper, AuthenticatedUserService authenticatedUserService, @Qualifier("messageSource") MessageSource messageSource) {
        this.promotionRepository = promotionRepository;
        this.promotionMapper = promotionMapper;
        this.authenticatedUserService = authenticatedUserService;
        this.messageSource = messageSource;
    }

    @LogAction(
            value = "enablePromotionUseCase.logAction.enable",
            args = {"#result.data.id", "#result.data.label"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    @Transactional
    public Response<PromotionResponseDTO> execute(Long id) {

        Promotion promotion = promotionRepository.findByIdNative(id)
                .orElseThrow(() -> new NotFoundException("exception.promotionNotFound.user", null, "exception.promotionNotFound.log", new Object[]{id, "EnablePromotionUseCase", "execute"}, LogLevel.ERROR));

        if (promotion.getEnabled()) {
            throw new ConflictException("exception.promotion.alreadyEnabled.user", null, "exception.promotion.alreadyEnabled.log", new Object[]{id, "EnablePromotionUseCase", "execute"}, LogLevel.ERROR);
        }

        promotion.enable(authenticatedUserService.getAuthenticatedUser());

        Promotion savedPromotion = promotionRepository.save(promotion);

        return new Response<>(
                true,
                messageSource.getMessage("promotion.enabled.ok", null, LocaleContextHolder.getLocale()),
                promotionMapper.toDTO(savedPromotion)
        );
    }
}