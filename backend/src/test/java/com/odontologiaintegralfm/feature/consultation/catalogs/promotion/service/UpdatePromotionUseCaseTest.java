package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionResponseDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.DiscountType;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.PromotionStatus;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.mapper.PromotionMapper;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model.Promotion;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.repository.IPromotionRepository;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UpdatePromotionUseCaseTest {

    @Mock private PromotionQueryService promotionQueryService;
    @Mock private PromotionDomainService promotionDomainService;
    @Mock private IPromotionRepository promotionRepository;
    @Mock private PromotionMapper promotionMapper;

    private UpdatePromotionUseCase useCase;

    private static final Long PROMOTION_ID = 1L;
    private static final LocalDate ACTIVE_START_DATE = LocalDate.now().minusDays(5);
    private static final LocalDate ACTIVE_END_DATE = LocalDate.now().plusDays(20);
    private static final LocalDate FINISHED_START_DATE = LocalDate.now().minusDays(20);
    private static final LocalDate FINISHED_END_DATE = LocalDate.now().minusDays(1);

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        useCase = new UpdatePromotionUseCase(
                promotionQueryService, promotionDomainService, promotionRepository, promotionMapper);
    }

    private Promotion persistedPromotion(String label, String name, DiscountType discountType,
                                          BigDecimal value, LocalDate startDate, LocalDate endDate) {
        Promotion promotion = Promotion.build(name, discountType, value, startDate, endDate);
        promotion.setId(PROMOTION_ID);
        promotion.setLabel(label);
        return promotion;
    }

    /**
     * CASO: La promoción no existe (o está deshabilitada).
     * Regla: req § 6 — 404 vía PromotionQueryService.findById.
     * Validación: Propaga NotFoundException; save() nunca se invoca.
     */
    @Test
    void execute_whenPromotionNotFound_throwsNotFoundException() {
        PromotionUpdateRequestDTO dto = new PromotionUpdateRequestDTO(
                "Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                LocalDate.now(), LocalDate.now().plusDays(10));

        when(promotionQueryService.findById(PROMOTION_ID))
                .thenThrow(new NotFoundException(
                        "exception.promotionNotFound.user", null,
                        "exception.promotionNotFound.log", new Object[]{PROMOTION_ID, "PromotionQueryService", "findById"},
                        com.odontologiaintegralfm.shared.enums.LogLevel.ERROR));

        assertThatThrownBy(() -> useCase.execute(PROMOTION_ID, dto))
                .isInstanceOf(NotFoundException.class);

        verify(promotionRepository, never()).save(any());
    }

    /**
     * CASO: Promoción NOT_STARTED, DTO llega con startDate distinto y anterior a hoy.
     * Regla: req § 6 — startDate en estado NOT_STARTED no puede quedar en el pasado.
     * Validación: Lanza BadRequestException; save() nunca se invoca.
     */
    @Test
    void execute_whenNotStartedAndStartDateBeforeToday_throwsBadRequestException() {
        LocalDate persistedStartDate = LocalDate.now().plusDays(5);
        LocalDate persistedEndDate = LocalDate.now().plusDays(20);
        Promotion promotion = persistedPromotion("Descuento Verano", "descuento verano",
                DiscountType.PERCENTAGE, BigDecimal.valueOf(10), persistedStartDate, persistedEndDate);

        PromotionUpdateRequestDTO dto = new PromotionUpdateRequestDTO(
                "Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                LocalDate.now().minusDays(1), persistedEndDate);

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(persistedStartDate, persistedEndDate))
                .thenReturn(PromotionStatus.NOT_STARTED);

        assertThatThrownBy(() -> useCase.execute(PROMOTION_ID, dto))
                .isInstanceOf(BadRequestException.class);

        verify(promotionRepository, never()).save(any());
    }

    /**
     * CASO: Promoción NOT_STARTED, DTO llega con endDate distinto y anterior a hoy.
     * Validación: Lanza BadRequestException; save() nunca se invoca.
     */
    @Test
    void execute_whenNotStartedAndEndDateBeforeToday_throwsBadRequestException() {
        LocalDate persistedStartDate = LocalDate.now().plusDays(5);
        LocalDate persistedEndDate = LocalDate.now().plusDays(20);
        Promotion promotion = persistedPromotion("Descuento Verano", "descuento verano",
                DiscountType.PERCENTAGE, BigDecimal.valueOf(10), persistedStartDate, persistedEndDate);

        PromotionUpdateRequestDTO dto = new PromotionUpdateRequestDTO(
                "Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                persistedStartDate, LocalDate.now().minusDays(1));

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(persistedStartDate, persistedEndDate))
                .thenReturn(PromotionStatus.NOT_STARTED);

        assertThatThrownBy(() -> useCase.execute(PROMOTION_ID, dto))
                .isInstanceOf(BadRequestException.class);

        verify(promotionRepository, never()).save(any());
    }

    /**
     * CASO: Promoción NOT_STARTED, DTO llega con discountType=PERCENTAGE, value=101 (distinto del persistido).
     * Regla: delegado a PromotionDomainService.validateValueRange.
     * Validación: Lanza BadRequestException; save() nunca se invoca.
     */
    @Test
    void execute_whenNotStartedAndPercentageOutOfRange_throwsBadRequestException() {
        LocalDate persistedStartDate = LocalDate.now().plusDays(5);
        LocalDate persistedEndDate = LocalDate.now().plusDays(20);
        Promotion promotion = persistedPromotion("Descuento Verano", "descuento verano",
                DiscountType.PERCENTAGE, BigDecimal.valueOf(10), persistedStartDate, persistedEndDate);

        PromotionUpdateRequestDTO dto = new PromotionUpdateRequestDTO(
                "Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(101),
                persistedStartDate, persistedEndDate);

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(persistedStartDate, persistedEndDate))
                .thenReturn(PromotionStatus.NOT_STARTED);
        doThrow(new BadRequestException(
                "exception.promotionDomainService.percentageOutOfRange.user", null,
                "exception.promotionDomainService.percentageOutOfRange.log", new Object[]{},
                com.odontologiaintegralfm.shared.enums.LogLevel.ERROR))
                .when(promotionDomainService).validateValueRange(DiscountType.PERCENTAGE, BigDecimal.valueOf(101));

        assertThatThrownBy(() -> useCase.execute(PROMOTION_ID, dto))
                .isInstanceOf(BadRequestException.class);

        verify(promotionRepository, never()).save(any());
    }

    /**
     * CASO: Promoción NOT_STARTED, DTO llega con discountType=FIXED, value=0 (distinto del persistido).
     * Validación: Lanza BadRequestException; save() nunca se invoca.
     */
    @Test
    void execute_whenNotStartedAndFixedBelowMinimum_throwsBadRequestException() {
        LocalDate persistedStartDate = LocalDate.now().plusDays(5);
        LocalDate persistedEndDate = LocalDate.now().plusDays(20);
        Promotion promotion = persistedPromotion("Descuento Verano", "descuento verano",
                DiscountType.PERCENTAGE, BigDecimal.valueOf(10), persistedStartDate, persistedEndDate);

        PromotionUpdateRequestDTO dto = new PromotionUpdateRequestDTO(
                "Descuento Verano", DiscountType.FIXED, BigDecimal.ZERO,
                persistedStartDate, persistedEndDate);

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(persistedStartDate, persistedEndDate))
                .thenReturn(PromotionStatus.NOT_STARTED);
        doThrow(new BadRequestException(
                "exception.promotionDomainService.fixedBelowMinimum.user", null,
                "exception.promotionDomainService.fixedBelowMinimum.log", new Object[]{},
                com.odontologiaintegralfm.shared.enums.LogLevel.ERROR))
                .when(promotionDomainService).validateValueRange(DiscountType.FIXED, BigDecimal.ZERO);

        assertThatThrownBy(() -> useCase.execute(PROMOTION_ID, dto))
                .isInstanceOf(BadRequestException.class);

        verify(promotionRepository, never()).save(any());
    }

    /**
     * CASO: Promoción NOT_STARTED, DTO idéntico en los 5 campos al persistido.
     * Regla: no-op real — no llama save() aunque el estado permita editar todo.
     * Validación: Response success=true con data igual al estado actual; save() nunca se invoca.
     */
    @Test
    void execute_whenNotStartedAndAllFieldsIdenticalToPersisted_returnsSameDTOAndDoesNotSave() {
        LocalDate persistedStartDate = LocalDate.now().plusDays(5);
        LocalDate persistedEndDate = LocalDate.now().plusDays(20);
        Promotion promotion = persistedPromotion("Descuento Verano", "descuento verano",
                DiscountType.PERCENTAGE, BigDecimal.valueOf(10), persistedStartDate, persistedEndDate);

        PromotionUpdateRequestDTO dto = new PromotionUpdateRequestDTO(
                "Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                persistedStartDate, persistedEndDate);

        PromotionResponseDTO responseDTO = new PromotionResponseDTO(
                PROMOTION_ID, "Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                persistedStartDate, persistedEndDate);

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(persistedStartDate, persistedEndDate))
                .thenReturn(PromotionStatus.NOT_STARTED);
        when(promotionMapper.toDTO(promotion)).thenReturn(responseDTO);

        Response<PromotionResponseDTO> result = useCase.execute(PROMOTION_ID, dto);

        assertThat(result.success()).isTrue();
        assertThat(result.data()).isEqualTo(responseDTO);
        verify(promotionRepository, never()).save(any());
    }

    /**
     * CASO: Promoción NOT_STARTED, label persistido distinto al del DTO, existsByNameAndIdNot devuelve false.
     * Validación: save() se invoca con la entidad cuyo name es el trim+lowercase del nuevo label;
     * response con label actualizado.
     */
    @Test
    void execute_whenNotStartedAndLabelChanged_recalculatesNameAndSaves() {
        LocalDate persistedStartDate = LocalDate.now().plusDays(5);
        LocalDate persistedEndDate = LocalDate.now().plusDays(20);
        Promotion promotion = persistedPromotion("Descuento Verano", "descuento verano",
                DiscountType.PERCENTAGE, BigDecimal.valueOf(10), persistedStartDate, persistedEndDate);

        PromotionUpdateRequestDTO dto = new PromotionUpdateRequestDTO(
                " Descuento Invierno ", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                persistedStartDate, persistedEndDate);

        PromotionResponseDTO responseDTO = new PromotionResponseDTO(
                PROMOTION_ID, " Descuento Invierno ", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                persistedStartDate, persistedEndDate);

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(persistedStartDate, persistedEndDate))
                .thenReturn(PromotionStatus.NOT_STARTED);
        when(promotionRepository.existsByNameAndIdNot("descuento invierno", PROMOTION_ID)).thenReturn(false);
        when(promotionRepository.save(promotion)).thenReturn(promotion);
        when(promotionMapper.toDTO(promotion)).thenReturn(responseDTO);

        Response<PromotionResponseDTO> result = useCase.execute(PROMOTION_ID, dto);

        verify(promotionRepository).save(promotion);
        assertThat(promotion.getName()).isEqualTo("descuento invierno");
        assertThat(promotion.getLabel()).isEqualTo(dto.label());
        assertThat(result.data().label()).isEqualTo(dto.label());
    }

    /**
     * CASO: Promoción NOT_STARTED, label del DTO distinto al persistido, existsByNameAndIdNot devuelve true.
     * Validación: Lanza ConflictException; save() nunca se invoca.
     */
    @Test
    void execute_whenNotStartedAndLabelDuplicatesAnotherPromotion_throwsConflictException() {
        LocalDate persistedStartDate = LocalDate.now().plusDays(5);
        LocalDate persistedEndDate = LocalDate.now().plusDays(20);
        Promotion promotion = persistedPromotion("Descuento Verano", "descuento verano",
                DiscountType.PERCENTAGE, BigDecimal.valueOf(10), persistedStartDate, persistedEndDate);

        PromotionUpdateRequestDTO dto = new PromotionUpdateRequestDTO(
                "Descuento Invierno", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                persistedStartDate, persistedEndDate);

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(persistedStartDate, persistedEndDate))
                .thenReturn(PromotionStatus.NOT_STARTED);
        when(promotionRepository.existsByNameAndIdNot("descuento invierno", PROMOTION_ID)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(PROMOTION_ID, dto))
                .isInstanceOf(ConflictException.class);

        verify(promotionRepository, never()).save(any());
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> activeFieldVariants() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(
                        "label",
                        new PromotionUpdateRequestDTO("Descuento Invierno", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                                ACTIVE_START_DATE, ACTIVE_END_DATE)),
                org.junit.jupiter.params.provider.Arguments.of(
                        "startDate",
                        new PromotionUpdateRequestDTO("Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                                ACTIVE_START_DATE.minusDays(1), ACTIVE_END_DATE)),
                org.junit.jupiter.params.provider.Arguments.of(
                        "discountType",
                        new PromotionUpdateRequestDTO("Descuento Verano", DiscountType.FIXED, BigDecimal.valueOf(10),
                                ACTIVE_START_DATE, ACTIVE_END_DATE)),
                org.junit.jupiter.params.provider.Arguments.of(
                        "value",
                        new PromotionUpdateRequestDTO("Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(20),
                                ACTIVE_START_DATE, ACTIVE_END_DATE))
        );
    }

    /**
     * CASO: Promoción ACTIVE, DTO llega con un campo distinto al persistido (resto idéntico).
     * Regla: en ACTIVE, label/startDate/discountType/value son "prohibidos siempre" (a diferencia de
     * endDate, que tiene su propia regla de boundary — ver tests aparte). No incluye endDate: sería
     * una quinta variante con una regla distinta ("permitido con boundary de fecha"), no un alias
     * de "campo prohibido" — mezclarla acá rompería la simetría con la matriz FINISHED.
     * Regla (ADR-0026): el rechazo depende de mirar el estado ACTIVE derivado del recurso persistido → 409, no 400.
     * Validación: Lanza ConflictException (409) para cada uno de los 4 campos; save() nunca se invoca.
     */
    @ParameterizedTest(name = "campo bajo test: {0}")
    @MethodSource("activeFieldVariants")
    void execute_whenActiveAndFieldDiffers_throwsConflictException(String fieldUnderTest, PromotionUpdateRequestDTO dto) {
        Promotion promotion = persistedPromotion("Descuento Verano", "descuento verano",
                DiscountType.PERCENTAGE, BigDecimal.valueOf(10), ACTIVE_START_DATE, ACTIVE_END_DATE);

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(ACTIVE_START_DATE, ACTIVE_END_DATE))
                .thenReturn(PromotionStatus.ACTIVE);

        assertThatThrownBy(() -> useCase.execute(PROMOTION_ID, dto))
                .isInstanceOf(ConflictException.class);

        verify(promotionRepository, never()).save(any());
    }

    /**
     * CASO: Promoción ACTIVE, DTO llega con endDate distinto y anterior a hoy.
     * Validación: Lanza BadRequestException; save() nunca se invoca.
     */
    @Test
    void execute_whenActiveAndEndDateBeforeToday_throwsBadRequestException() {
        PromotionUpdateRequestDTO dto = new PromotionUpdateRequestDTO(
                "Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                ACTIVE_START_DATE, LocalDate.now().minusDays(1));

        Promotion promotion = persistedPromotion("Descuento Verano", "descuento verano",
                DiscountType.PERCENTAGE, BigDecimal.valueOf(10), ACTIVE_START_DATE, ACTIVE_END_DATE);

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(ACTIVE_START_DATE, ACTIVE_END_DATE))
                .thenReturn(PromotionStatus.ACTIVE);

        assertThatThrownBy(() -> useCase.execute(PROMOTION_ID, dto))
                .isInstanceOf(BadRequestException.class);

        verify(promotionRepository, never()).save(any());
    }

    /**
     * CASO: Promoción ACTIVE con endDate persistido futuro, DTO llega con endDate=hoy (boundary crítico).
     * Regla: endDate == hoy es válido en ACTIVE (acorta hasta hoy inclusive) — bug más probable en prod
     * si se invierte < por <=.
     * Validación: Se aplica el cambio, save() se invoca, response con endDate=hoy.
     */
    @Test
    void execute_whenActiveAndEndDateEqualsToday_appliesChange() {
        Promotion promotion = persistedPromotion("Descuento Verano", "descuento verano",
                DiscountType.PERCENTAGE, BigDecimal.valueOf(10), ACTIVE_START_DATE, ACTIVE_END_DATE);

        LocalDate newEndDate = LocalDate.now();
        PromotionUpdateRequestDTO dto = new PromotionUpdateRequestDTO(
                "Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                ACTIVE_START_DATE, newEndDate);

        PromotionResponseDTO responseDTO = new PromotionResponseDTO(
                PROMOTION_ID, "Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                ACTIVE_START_DATE, newEndDate);

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(ACTIVE_START_DATE, ACTIVE_END_DATE))
                .thenReturn(PromotionStatus.ACTIVE);
        when(promotionRepository.save(promotion)).thenReturn(promotion);
        when(promotionMapper.toDTO(promotion)).thenReturn(responseDTO);

        Response<PromotionResponseDTO> result = useCase.execute(PROMOTION_ID, dto);

        verify(promotionRepository).save(promotion);
        assertThat(result.data().endDate()).isEqualTo(newEndDate);
    }

    /**
     * CASO: Promoción ACTIVE, DTO llega con endDate posterior al persistido.
     * Validación: Se aplica el cambio, save() se invoca, response con el nuevo endDate.
     */
    @Test
    void execute_whenActiveAndEndDateExtended_appliesChange() {
        Promotion promotion = persistedPromotion("Descuento Verano", "descuento verano",
                DiscountType.PERCENTAGE, BigDecimal.valueOf(10), ACTIVE_START_DATE, ACTIVE_END_DATE);

        LocalDate newEndDate = ACTIVE_END_DATE.plusDays(30);
        PromotionUpdateRequestDTO dto = new PromotionUpdateRequestDTO(
                "Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                ACTIVE_START_DATE, newEndDate);

        PromotionResponseDTO responseDTO = new PromotionResponseDTO(
                PROMOTION_ID, "Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                ACTIVE_START_DATE, newEndDate);

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(ACTIVE_START_DATE, ACTIVE_END_DATE))
                .thenReturn(PromotionStatus.ACTIVE);
        when(promotionRepository.save(promotion)).thenReturn(promotion);
        when(promotionMapper.toDTO(promotion)).thenReturn(responseDTO);

        Response<PromotionResponseDTO> result = useCase.execute(PROMOTION_ID, dto);

        verify(promotionRepository).save(promotion);
        assertThat(result.data().endDate()).isEqualTo(newEndDate);
    }

    private static Stream<org.junit.jupiter.params.provider.Arguments> finishedFieldVariants() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(
                        "label",
                        new PromotionUpdateRequestDTO("Otro Label", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                                FINISHED_START_DATE, FINISHED_END_DATE)),
                org.junit.jupiter.params.provider.Arguments.of(
                        "startDate",
                        new PromotionUpdateRequestDTO("Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                                FINISHED_START_DATE.minusDays(1), FINISHED_END_DATE)),
                org.junit.jupiter.params.provider.Arguments.of(
                        "endDate",
                        new PromotionUpdateRequestDTO("Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                                FINISHED_START_DATE, FINISHED_END_DATE.minusDays(1))),
                org.junit.jupiter.params.provider.Arguments.of(
                        "discountType",
                        new PromotionUpdateRequestDTO("Descuento Verano", DiscountType.FIXED, BigDecimal.valueOf(10),
                                FINISHED_START_DATE, FINISHED_END_DATE)),
                org.junit.jupiter.params.provider.Arguments.of(
                        "value",
                        new PromotionUpdateRequestDTO("Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(20),
                                FINISHED_START_DATE, FINISHED_END_DATE))
        );
    }

    /**
     * CASO: Promoción FINISHED (endDate < hoy), DTO llega con un campo distinto al persistido (resto idéntico).
     * Regla: en FINISHED ningún campo puede diferir del persistido.
     * Regla (ADR-0026): el rechazo depende de mirar el estado FINISHED derivado del recurso persistido → 409, no 400.
     * Validación: Lanza ConflictException para cada uno de los 5 campos; save() nunca se invoca.
     */
    @ParameterizedTest(name = "campo bajo test: {0}")
    @MethodSource("finishedFieldVariants")
    void execute_whenFinishedAndFieldDiffers_throwsConflictException(String fieldUnderTest, PromotionUpdateRequestDTO dto) {
        Promotion promotion = persistedPromotion("Descuento Verano", "descuento verano",
                DiscountType.PERCENTAGE, BigDecimal.valueOf(10), FINISHED_START_DATE, FINISHED_END_DATE);

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(FINISHED_START_DATE, FINISHED_END_DATE))
                .thenReturn(PromotionStatus.FINISHED);

        assertThatThrownBy(() -> useCase.execute(PROMOTION_ID, dto))
                .isInstanceOf(ConflictException.class);

        verify(promotionRepository, never()).save(any());
    }

    /**
     * CASO: Promoción NOT_STARTED, DTO llega con startDate distinto y posterior al endDate persistido (sin tocar endDate).
     * Regla: req § 3 — fechaHasta nunca antes del inicio; mismo criterio que CreatePromotionUseCase.validateDateRange,
     * evaluable sin mirar más estado que los dos valores resultantes.
     * Validación: Lanza BadRequestException; save() nunca se invoca.
     */
    @Test
    void execute_whenNotStartedAndStartDateMovedAfterEndDate_throwsBadRequestException() {
        LocalDate persistedStartDate = LocalDate.now().plusDays(5);
        LocalDate persistedEndDate = LocalDate.now().plusDays(20);
        Promotion promotion = persistedPromotion("Descuento Verano", "descuento verano",
                DiscountType.PERCENTAGE, BigDecimal.valueOf(10), persistedStartDate, persistedEndDate);

        PromotionUpdateRequestDTO dto = new PromotionUpdateRequestDTO(
                "Descuento Verano", DiscountType.PERCENTAGE, BigDecimal.valueOf(10),
                LocalDate.now().plusDays(30), persistedEndDate);

        when(promotionQueryService.findById(PROMOTION_ID)).thenReturn(promotion);
        when(promotionDomainService.resolveStatus(persistedStartDate, persistedEndDate))
                .thenReturn(PromotionStatus.NOT_STARTED);

        assertThatThrownBy(() -> useCase.execute(PROMOTION_ID, dto))
                .isInstanceOf(BadRequestException.class);

        verify(promotionRepository, never()).save(any());
    }
}