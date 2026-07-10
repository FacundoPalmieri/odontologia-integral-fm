package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionCreateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionResponseDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.DiscountType;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.mapper.PromotionMapper;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model.Promotion;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.repository.IPromotionRepository;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.enums.LogType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class CreatePromotionUseCase {

    private final IPromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;

    public CreatePromotionUseCase(IPromotionRepository promotionRepository, PromotionMapper promotionMapper) {
        this.promotionRepository = promotionRepository;
        this.promotionMapper = promotionMapper;
    }

    /**
     * Crea una nueva promoción, validando reglas de fecha, rango de value según discountType
     * y unicidad de label antes de persistir.
     *
     * @throws BadRequestException si las fechas son incoherentes o el value está fuera de rango.
     * @throws ConflictException si el label ya existe (normalizado).
     */
    @LogAction(
            value = "createPromotionUseCase.logAction.create",
            args = {"#result.data.id", "#result.data.label", "#result.data.discountType", "#result.data.value"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<PromotionResponseDTO> execute(PromotionCreateRequestDTO dto) {

        validateDateRange(dto.startDate(), dto.endDate());
        validateValueRange(dto.discountType(), dto.value());

        String candidateName = normalizeAndCheckDuplicate(dto.label());

        Promotion promotion = promotionMapper.toEntity(dto);
        promotion.setName(candidateName);

        Promotion savedPromotion = promotionRepository.save(promotion);

        return new Response<>(true, null, promotionMapper.toDTO(savedPromotion));
    }

    /**
     * Valida coherencia de fechas: startDate < endDate, y ninguna de las dos en el pasado.
     * Regla: req § 6 + decisión de diseño #2 (no se permite alta retroactiva).
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

        if (!startDate.isBefore(endDate)) {
            throw new BadRequestException(
                    "exception.createPromotionUseCase.invalidDateRange.user", null,
                    "exception.createPromotionUseCase.invalidDateRange.log", new Object[]{startDate, endDate, "CreatePromotionUseCase", "execute"},
                    LogLevel.ERROR);
        }

        if (startDate.isBefore(today)) {
            throw new BadRequestException(
                    "exception.createPromotionUseCase.startDateInPast.user", null,
                    "exception.createPromotionUseCase.startDateInPast.log", new Object[]{startDate, "CreatePromotionUseCase", "execute"},
                    LogLevel.ERROR);
        }

        if (endDate.isBefore(today)) {
            throw new BadRequestException(
                    "exception.createPromotionUseCase.endDateInPast.user", null,
                    "exception.createPromotionUseCase.endDateInPast.log", new Object[]{endDate, "CreatePromotionUseCase", "execute"},
                    LogLevel.ERROR);
        }
    }

    /**
     * Valida el rango de value según discountType: PERCENTAGE en [1,100], FIXED >= 1.
     * Regla: req § 6.
     */
    private void validateValueRange(DiscountType discountType, BigDecimal value) {
        if (discountType == DiscountType.PERCENTAGE) {
            if (value.compareTo(BigDecimal.ONE) < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException(
                        "exception.createPromotionUseCase.percentageOutOfRange.user", null,
                        "exception.createPromotionUseCase.percentageOutOfRange.log", new Object[]{value, "CreatePromotionUseCase", "execute"},
                        LogLevel.ERROR);
            }
        } else {
            if (value.compareTo(BigDecimal.ONE) < 0) {
                throw new BadRequestException(
                        "exception.createPromotionUseCase.fixedBelowMinimum.user", null,
                        "exception.createPromotionUseCase.fixedBelowMinimum.log", new Object[]{value, "CreatePromotionUseCase", "execute"},
                        LogLevel.ERROR);
            }
        }
    }

    /**
     * Normaliza el label (trim + lowercase) y valida que no exista ya como clave interna.
     * Regla: req § 6 — unicidad de label, blindada contra la clave estable interna normalizada.
     */
    private String normalizeAndCheckDuplicate(String label) {
        String candidateName = label.trim().toLowerCase();

        if (promotionRepository.existsByName(candidateName)) {
            throw new ConflictException(
                    "exception.createPromotionUseCase.duplicateLabel.user", null,
                    "exception.createPromotionUseCase.duplicateLabel.log", new Object[]{candidateName, "CreatePromotionUseCase", "execute"},
                    LogLevel.ERROR);
        }

        return candidateName;
    }
}