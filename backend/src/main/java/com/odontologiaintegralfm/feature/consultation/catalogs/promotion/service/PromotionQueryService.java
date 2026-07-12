package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionResponseDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.mapper.PromotionMapper;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model.Promotion;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.repository.IPromotionRepository;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PromotionQueryService {
    private final IPromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;

    PromotionQueryService(IPromotionRepository promotionRepository, PromotionMapper promotionMapper) {
        this.promotionRepository = promotionRepository;
        this.promotionMapper = promotionMapper;
    }

    public Promotion findById(Long id) {
        if (id == null) return null;
        return promotionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("exception.promotionNotFound.user", null,"exception.promotionNotFound.log", new Object[]{id,"PromotionQueryService","findById"}, LogLevel.ERROR));
    }

    @Transactional(readOnly = true)
    public Response<List<PromotionResponseDTO>> getCurrent() {
        try {
            List<PromotionResponseDTO> promotions = promotionMapper.toDTO(promotionRepository.findAllActive(LocalDate.now()));
            return new Response<>(true, null, promotions);
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PromotionQueryService", null, null, "getCurrent");
        }
    }
}
