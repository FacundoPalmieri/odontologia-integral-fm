package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.Repository.IPromotionRepository;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model.Promotion;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PromotionService {
    private IPromotionRepository promotionRepository;

    PromotionService(IPromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public Promotion findById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("exception.promotionNotFound.user", null,"exception.promotionNotFound.log", new Object[]{id,"PromotionService","findById"}, LogLevel.ERROR));
    }
}
