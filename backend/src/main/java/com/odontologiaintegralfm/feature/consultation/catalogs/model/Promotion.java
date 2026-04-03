package com.odontologiaintegralfm.feature.consultation.catalogs.model;

import com.odontologiaintegralfm.feature.consultation.catalogs.enums.DiscountType;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad que representa una promoción aplicable a prestaciones.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "promotions")
@Where(clause = "enabled = true")
public class Promotion extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    /** Tipo de descuento: Porcentaje o valor fijo */
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(nullable = false)
    private BigDecimal value;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private Promotion(String name, DiscountType discountType, BigDecimal value, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.discountType = discountType;
        this.value = value;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static Promotion build(String name, DiscountType discountType, BigDecimal value, LocalDate startDate, LocalDate endDate) {
        return new Promotion(name, discountType, value, startDate, endDate);
    }
}

