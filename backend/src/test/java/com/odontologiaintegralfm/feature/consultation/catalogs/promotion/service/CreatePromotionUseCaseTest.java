package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.service;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionCreateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.dto.PromotionResponseDTO;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.DiscountType;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.mapper.PromotionMapper;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model.Promotion;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.repository.IPromotionRepository;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.exception.BadRequestException;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CreatePromotionUseCaseTest {

    @Mock private IPromotionRepository promotionRepository;
    @Mock private PromotionMapper promotionMapper;
    @Mock private PromotionDomainService promotionDomainService;

    @InjectMocks private CreatePromotionUseCase useCase;

    /**
     * CASO: El label ya existe (normalizado trim+lowercase) en el repositorio.
     * Regla: req § 6 — unicidad de label, chequeada contra la clave interna normalizada.
     * Validación: Lanza ConflictException, existsByName se invoca con el valor normalizado, y save() nunca se invoca.
     */
    @Test
    void execute_whenLabelAlreadyExistsNormalized_throwsConflictException() {
        PromotionCreateRequestDTO dto = new PromotionCreateRequestDTO(
                " Descuento Verano ",
                DiscountType.PERCENTAGE,
                BigDecimal.valueOf(10),
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );

        when(promotionRepository.existsByName("descuento verano")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(ConflictException.class);

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        verify(promotionRepository).existsByName(nameCaptor.capture());
        assertThat(nameCaptor.getValue()).isEqualTo("descuento verano");

        verify(promotionRepository, never()).save(any());
    }

    /**
     * CASO: discountType=PERCENTAGE con value fuera de [1,100] (extremo inferior: 0).
     * Regla: req § 6 — rango válido de porcentaje.
     * Validación: Lanza BadRequestException y save() nunca se invoca.
     */
    @Test
    void execute_whenPercentageValueBelowMinimum_throwsBadRequestException() {
        PromotionCreateRequestDTO dto = new PromotionCreateRequestDTO(
                "Descuento Invierno",
                DiscountType.PERCENTAGE,
                BigDecimal.ZERO,
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );

        doThrow(new BadRequestException(
                "exception.promotionDomainService.percentageOutOfRange.user", null,
                "exception.promotionDomainService.percentageOutOfRange.log", new Object[]{},
                com.odontologiaintegralfm.shared.enums.LogLevel.ERROR))
                .when(promotionDomainService).validateValueRange(DiscountType.PERCENTAGE, BigDecimal.ZERO);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(BadRequestException.class);

        verify(promotionRepository, never()).save(any());
    }

    /**
     * CASO: discountType=PERCENTAGE con value fuera de [1,100] (extremo superior: 101).
     * Regla: req § 6 — rango válido de porcentaje.
     * Validación: Lanza BadRequestException y save() nunca se invoca.
     */
    @Test
    void execute_whenPercentageValueAboveMaximum_throwsBadRequestException() {
        PromotionCreateRequestDTO dto = new PromotionCreateRequestDTO(
                "Descuento Otoño",
                DiscountType.PERCENTAGE,
                BigDecimal.valueOf(101),
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );

        doThrow(new BadRequestException(
                "exception.promotionDomainService.percentageOutOfRange.user", null,
                "exception.promotionDomainService.percentageOutOfRange.log", new Object[]{},
                com.odontologiaintegralfm.shared.enums.LogLevel.ERROR))
                .when(promotionDomainService).validateValueRange(DiscountType.PERCENTAGE, BigDecimal.valueOf(101));

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(BadRequestException.class);

        verify(promotionRepository, never()).save(any());
    }

    /**
     * CASO: discountType=FIXED con value menor al mínimo permitido (0).
     * Regla: req § 6 — monto fijo mínimo.
     * Validación: Lanza BadRequestException y save() nunca se invoca.
     */
    @Test
    void execute_whenFixedValueBelowMinimum_throwsBadRequestException() {
        PromotionCreateRequestDTO dto = new PromotionCreateRequestDTO(
                "Descuento Fijo",
                DiscountType.FIXED,
                BigDecimal.ZERO,
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );

        doThrow(new BadRequestException(
                "exception.promotionDomainService.fixedBelowMinimum.user", null,
                "exception.promotionDomainService.fixedBelowMinimum.log", new Object[]{},
                com.odontologiaintegralfm.shared.enums.LogLevel.ERROR))
                .when(promotionDomainService).validateValueRange(DiscountType.FIXED, BigDecimal.ZERO);

        assertThatThrownBy(() -> useCase.execute(dto))
                .isInstanceOf(BadRequestException.class);

        verify(promotionRepository, never()).save(any());
    }

    /**
     * CASO: Datos válidos, label no duplicado, value dentro de rango según discountType.
     * Validación: Se invoca save() exactamente una vez con una entidad cuyo name (clave interna)
     * es el trim+lowercase del label del DTO. El Response devuelto tiene success=true y data
     * con los valores del DTO de entrada.
     */
    @Test
    void execute_withValidData_createsPromotionAndReturnsResponseDTO() {
        PromotionCreateRequestDTO dto = new PromotionCreateRequestDTO(
                " Descuento Verano ",
                DiscountType.PERCENTAGE,
                BigDecimal.valueOf(15),
                LocalDate.now(),
                LocalDate.now().plusDays(10)
        );

        Promotion mappedEntity = Promotion.build(
                null, dto.discountType(), dto.value(), dto.startDate(), dto.endDate());
        Promotion savedEntity = Promotion.build(
                "descuento verano", dto.discountType(), dto.value(), dto.startDate(), dto.endDate());
        savedEntity.setId(1L);

        PromotionResponseDTO responseDTO = new PromotionResponseDTO(
                1L, dto.label(), dto.discountType(), dto.value(), dto.startDate(), dto.endDate());

        when(promotionRepository.existsByName("descuento verano")).thenReturn(false);
        when(promotionMapper.toEntity(dto)).thenReturn(mappedEntity);
        when(promotionRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(promotionMapper.toDTO(savedEntity)).thenReturn(responseDTO);

        Response<PromotionResponseDTO> result = useCase.execute(dto);

        verify(promotionRepository).save(mappedEntity);
        assertThat(mappedEntity.getName()).isEqualTo("descuento verano");

        assertThat(result.success()).isTrue();
        assertThat(result.data().label()).isEqualTo(dto.label());
        assertThat(result.data().discountType()).isEqualTo(dto.discountType());
        assertThat(result.data().value()).isEqualTo(dto.value());
        assertThat(result.data().startDate()).isEqualTo(dto.startDate());
        assertThat(result.data().endDate()).isEqualTo(dto.endDate());
    }
}