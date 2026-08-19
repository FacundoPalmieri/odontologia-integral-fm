package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionResponseDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.DiscountType;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.PromotionStatus;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.mapper.PromotionMapper;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model.Promotion;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.repository.IPromotionRepository;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class FinishPromotionUseCaseTest {

    @Mock private PromotionQueryService promotionQueryService;
    @Mock private PromotionDomainService promotionDomainService;
    @Mock private IPromotionRepository promotionRepository;
    @Mock private PromotionMapper promotionMapper;

    private FinishPromotionUseCase useCase;

    private static final Long PROMOTION_ID = 1L;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        useCase = new FinishPromotionUseCase(
                promotionQueryService, promotionDomainService, promotionRepository, promotionMapper);
    }

    private Promotion persistedPromotion(LocalDate startDate, LocalDate endDate, LocalDateTime finishedAt) {
        Promotion promotion = Promotion.build("descuento verano", DiscountType.PERCENTAGE,
                BigDecimal.valueOf(10), startDate, endDate);
        promotion.setId(PROMOTION_ID);
        promotion.setLabel("Descuento Verano");
        promotion.setFinishedAt(finishedAt);
        return promotion;
    }

    /**
     * CASO: La promoción no existe (o está deshabilitada).
     * Regla: req § 6 — 404 vía PromotionQueryService.findById.
     * Validación: Propaga NotFoundException; save() nunca se invoca.
     */
    @Test
    void execute_promotionNotFound_returnNotFound() {
        when(promotionQueryService.findById(PROMOTION_ID))
                .thenThrow(new NotFoundException(
                        "exception.promotionNotFound.user", null,
                        "exception.promotionNotFound.log", new Object[]{PROMOTION_ID, "PromotionQueryService", "findById"},
                        LogLevel.ERROR));

        assertThatThrownBy(() -> useCase.execute(PROMOTION_ID))
                .isInstanceOf(NotFoundException.class);

        verify(promotionRepository, never()).save(any());
    }

    /**
     * CASO: Promoción ya FINISHED (corte manual previo o vencimiento natural).
     * Regla: req § 6 — resolveStatus == FINISHED rechaza con 409.
     * Validación: Lanza ConflictException; save() nunca se invoca.
     */
    @Test
    void execute_promotionFinished_returnConflict() {
        LocalDate startDate = LocalDate.now().minusDays(20);
        LocalDate endDate = LocalDate.now().minusDays(1);
        Promotion promotion = persistedPromotion(startDate, endDate, null);

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(startDate, endDate, null))
                .thenReturn(PromotionStatus.FINISHED);

        assertThatThrownBy(() -> useCase.execute(PROMOTION_ID))
                .isInstanceOf(ConflictException.class);

        verify(promotionRepository, never()).save(any());
    }

    /**
     * CASO: Promoción ACTIVE (startDate pasado, endDate futuro, finishedAt = null).
     * Validación: finishedAt queda seteado con timestamp actual (no null, no anterior al momento
     * capturado antes de invocar); save() se invoca una vez; response 200/success=true con DTO mapeado.
     */
    @Test
    void execute_promotionActive_setsFinishedAt() {
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(20);
        Promotion promotion = persistedPromotion(startDate, endDate, null);

        PromotionResponseDTO responseDTO = new PromotionResponseDTO(
                PROMOTION_ID, "Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                startDate, endDate, LocalDateTime.now());

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(startDate, endDate, null))
                .thenReturn(PromotionStatus.ACTIVE);
        when(promotionRepository.save(promotion)).thenReturn(promotion);
        when(promotionMapper.toDTO(promotion)).thenReturn(responseDTO);

        LocalDateTime before = LocalDateTime.now();
        Response<PromotionResponseDTO> result = useCase.execute(PROMOTION_ID);
        LocalDateTime after = LocalDateTime.now();

        assertThat(promotion.getFinishedAt()).isNotNull();
        assertThat(promotion.getFinishedAt()).isAfterOrEqualTo(before);
        assertThat(promotion.getFinishedAt()).isBeforeOrEqualTo(after);
        verify(promotionRepository).save(promotion);
        assertThat(result.success()).isTrue();
        assertThat(result.data()).isEqualTo(responseDTO);
    }

    /**
     * CASO: Promoción NOT_STARTED (startDate futuro, finishedAt = null).
     * Validación: finishedAt queda seteado con timestamp actual; save() se invoca una vez.
     */
    @Test
    void execute_promotionNotStarted_setsFinishedAt() {
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(20);
        Promotion promotion = persistedPromotion(startDate, endDate, null);

        PromotionResponseDTO responseDTO = new PromotionResponseDTO(
                PROMOTION_ID, "Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                startDate, endDate, LocalDateTime.now());

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(startDate, endDate, null))
                .thenReturn(PromotionStatus.NOT_STARTED);
        when(promotionRepository.save(promotion)).thenReturn(promotion);
        when(promotionMapper.toDTO(promotion)).thenReturn(responseDTO);

        LocalDateTime before = LocalDateTime.now();
        useCase.execute(PROMOTION_ID);
        LocalDateTime after = LocalDateTime.now();

        assertThat(promotion.getFinishedAt()).isNotNull();
        assertThat(promotion.getFinishedAt()).isAfterOrEqualTo(before);
        assertThat(promotion.getFinishedAt()).isBeforeOrEqualTo(after);
        verify(promotionRepository).save(promotion);
    }

    /**
     * CASO: Promoción ACTIVE con startDate/endDate conocidos.
     * Regla: Req § 5 — el corte de vigencia no debe modificar startDate/endDate (distingue este REQ
     * de un update genérico).
     * Validación: Tras execute, startDate y endDate de la entidad persistida son idénticos a los originales.
     */
    @Test
    void execute_promotionActive_doesNotModifyStartDateOrEndDate() {
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(20);
        Promotion promotion = persistedPromotion(startDate, endDate, null);

        PromotionResponseDTO responseDTO = new PromotionResponseDTO(
                PROMOTION_ID, "Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                startDate, endDate, LocalDateTime.now());

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(startDate, endDate, null))
                .thenReturn(PromotionStatus.ACTIVE);
        when(promotionRepository.save(promotion)).thenReturn(promotion);
        when(promotionMapper.toDTO(promotion)).thenReturn(responseDTO);

        useCase.execute(PROMOTION_ID);

        assertThat(promotion.getStartDate()).isEqualTo(startDate);
        assertThat(promotion.getEndDate()).isEqualTo(endDate);
    }
}