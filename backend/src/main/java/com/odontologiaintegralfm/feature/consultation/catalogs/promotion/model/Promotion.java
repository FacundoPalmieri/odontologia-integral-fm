package com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model;

import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.enums.DiscountType;
import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa una promoción aplicable a prestaciones.
 */
@Entity
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "promotions")
@Where(clause = "enabled = true")
public class Promotion extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 50)
    private String label;

    @Column(nullable = false, unique = true, length = 50)
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

    private LocalDateTime finishedAt;

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

    public BigDecimal calculateAmount(BigDecimal price) {
        if(this.discountType == DiscountType.PERCENTAGE) {
            return price.multiply(this.value).divide(BigDecimal.valueOf(100));
        }
        return this.value;
    }
}

