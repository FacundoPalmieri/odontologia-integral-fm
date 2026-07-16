package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionResponseDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.PromotionStatus;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.mapper.PromotionMapper;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model.Promotion;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.repository.IPromotionRepository;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Edita una promoción existente con semántica de "objeto completo + rechazo explícito por campo" (ADR-0024).
 * Por cada campo del DTO: si es igual al persistido se ignora, si difiere se valida según el estado
 * derivado (NOT_STARTED/ACTIVE/FINISHED, ADR-0025) y la regla propia del campo (ADR-0026).
 */
@Service
public class UpdatePromotionUseCase {

    private final PromotionQueryService promotionQueryService;
    private final PromotionDomainService promotionDomainService;
    private final IPromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;

    public UpdatePromotionUseCase(PromotionQueryService promotionQueryService, PromotionDomainService promotionDomainService,
                                   IPromotionRepository promotionRepository, PromotionMapper promotionMapper) {
        this.promotionQueryService = promotionQueryService;
        this.promotionDomainService = promotionDomainService;
        this.promotionRepository = promotionRepository;
        this.promotionMapper = promotionMapper;
    }

    @LogAction(
            value = "updatePromotionUseCase.logAction.update",
            args = {"#result.data.id", "#result.data.label", "#result.data.discountType", "#result.data.value", "#result.data.startDate", "#result.data.endDate"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    @Transactional
    public Response<PromotionResponseDTO> execute(Long id, PromotionUpdateRequestDTO dto) {

        Promotion promotion = promotionQueryService.findById(id);
        PromotionStatus status = promotionDomainService.resolveStatus(promotion.getStartDate(), promotion.getEndDate());

        boolean changed = applyLabel(promotion, dto, status)
                | applyStartDate(promotion, dto, status)
                | applyEndDate(promotion, dto, status)
                | applyDiscount(promotion, dto, status);

        validateDateRange(promotion.getStartDate(), promotion.getEndDate());

        if (!changed) {
            return new Response<>(true, null, promotionMapper.toDTO(promotion));
        }

        Promotion savedPromotion = promotionRepository.save(promotion);
        return new Response<>(true, null, promotionMapper.toDTO(savedPromotion));
    }

    /**
     * label: solo editable en NOT_STARTED (recalcula name y valida unicidad).
     * En ACTIVE y FINISHED, cualquier diferencia es 409 (ADR-0026, req § 3).
     */
    private boolean applyLabel(Promotion promotion, PromotionUpdateRequestDTO dto, PromotionStatus status) {
        if (dto.label().equals(promotion.getLabel())) {
            return false;
        }

        if (status != PromotionStatus.NOT_STARTED) {
            throw fieldNotAllowed("label", status);
        }

        String candidateName = dto.label().trim().toLowerCase();
        if (promotionRepository.existsByNameAndIdNot(candidateName, promotion.getId())) {
            throw new ConflictException(
                    "exception.createPromotionUseCase.duplicateLabel.user", null,
                    "exception.createPromotionUseCase.duplicateLabel.log", new Object[]{candidateName, "UpdatePromotionUseCase", "applyLabel"},
                    LogLevel.ERROR);
        }

        promotion.setLabel(dto.label());
        promotion.setName(candidateName);
        return true;
    }

    /**
     * startDate: solo editable en NOT_STARTED (rechaza si queda en el pasado).
     * En ACTIVE y FINISHED, cualquier diferencia es 409 (ADR-0026, req § 3) — no tiene sentido
     * mover el inicio de una promoción que ya arrancó o terminó.
     */
    private boolean applyStartDate(Promotion promotion, PromotionUpdateRequestDTO dto, PromotionStatus status) {
        if (dto.startDate().equals(promotion.getStartDate())) {
            return false;
        }

        if (status != PromotionStatus.NOT_STARTED) {
            throw fieldNotAllowed("startDate", status);
        }

        if (dto.startDate().isBefore(LocalDate.now())) {
            throw new BadRequestException(
                    "exception.createPromotionUseCase.startDateInPast.user", null,
                    "exception.createPromotionUseCase.startDateInPast.log", new Object[]{dto.startDate(), "UpdatePromotionUseCase", "applyStartDate"},
                    LogLevel.ERROR);
        }

        promotion.setStartDate(dto.startDate());
        return true;
    }

    /**
     * endDate: editable en NOT_STARTED y ACTIVE (rechaza si queda antes de hoy; hoy inclusive
     * es válido — decisión #3, req § 3). En FINISHED, cualquier diferencia es 409 (ADR-0026).
     */
    private boolean applyEndDate(Promotion promotion, PromotionUpdateRequestDTO dto, PromotionStatus status) {
        if (dto.endDate().equals(promotion.getEndDate())) {
            return false;
        }

        if (status == PromotionStatus.FINISHED) {
            throw fieldNotAllowed("endDate", status);
        }

        if (dto.endDate().isBefore(LocalDate.now())) {
            throw new BadRequestException(
                    "exception.createPromotionUseCase.endDateInPast.user", null,
                    "exception.createPromotionUseCase.endDateInPast.log", new Object[]{dto.endDate(), "UpdatePromotionUseCase", "applyEndDate"},
                    LogLevel.ERROR);
        }

        promotion.setEndDate(dto.endDate());
        return true;
    }

    /**
     * discountType/value: solo editables en NOT_STARTED (valida rango vía PromotionDomainService).
     * En ACTIVE y FINISHED, cualquier diferencia en cualquiera de los dos es 409 (ADR-0026).
     */
    private boolean applyDiscount(Promotion promotion, PromotionUpdateRequestDTO dto, PromotionStatus status) {
        boolean discountTypeChanged = dto.discountType() != promotion.getDiscountType();
        boolean valueChanged = dto.value().compareTo(promotion.getValue()) != 0;

        if (!discountTypeChanged && !valueChanged) {
            return false;
        }

        if (status != PromotionStatus.NOT_STARTED) {
            throw fieldNotAllowed(discountTypeChanged ? "discountType" : "value", status);
        }

        promotionDomainService.validateValueRange(dto.discountType(), dto.value());

        promotion.setDiscountType(dto.discountType());
        promotion.setValue(dto.value());
        return true;
    }

    /**
     * Valida el invariante startDate <= endDate sobre los valores resultantes de la entidad,
     * tras aplicar los cambios de fecha (Decisión #13 — cubre el caso de mover un extremo
     * más allá del otro sin tocarlo explícitamente).
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException(
                    "exception.createPromotionUseCase.invalidDateRange.user", null,
                    "exception.createPromotionUseCase.invalidDateRange.log", new Object[]{startDate, endDate, "UpdatePromotionUseCase", "execute"},
                    LogLevel.ERROR);
        }
    }

    private ConflictException fieldNotAllowed(String field, PromotionStatus status) {
        return new ConflictException(
                "exception.updatePromotionUseCase.fieldNotAllowed.user", new Object[]{field, status.getLabel()},
                "exception.updatePromotionUseCase.fieldNotAllowed.log", new Object[]{field, status, "UpdatePromotionUseCase", "execute"},
                LogLevel.ERROR);
    }
}