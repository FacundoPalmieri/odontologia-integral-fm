package com.odontologiaintegralfm.feature.consultation.core.prestation.model;

import com.odontologiaintegralfm.feature.consultation.catalogs.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.DiscountType;
import com.odontologiaintegralfm.feature.consultation.catalogs.promotion.model.Promotion;
import com.odontologiaintegralfm.feature.consultation.core.prestation.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.ToothFace;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.Maxillary;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import com.odontologiaintegralfm.feature.consultation.core.odontogram.model.Odontogram;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Checks;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Entidad que representa una instancia real de una prestación en una consulta.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "prestations_instances")
@Checks({
        @Check(constraints = "price > 0"),
        @Check(constraints = "promotion_amount IS NULL OR promotion_amount > 0"),
        @Check(constraints = "discount_value   IS NULL OR discount_value   > 0"),
        @Check(constraints = "discount_amount  IS NULL OR discount_amount  > 0"),
        @Check(constraints = "final_amount     >= 0")
})
@Where(clause = "enabled = true")
@Audited
public class PrestationInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consultation_instance_id", nullable = false)
    private ConsultationInstance consultationInstance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prestation_type_id", nullable = false)
    private PrestationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "odontogram_detail_id")
    private Odontogram odontogram;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrestationInstanceStatus status;

    /** Solo si Odontogram es null (prestación sin ubicación específica) */
    @Enumerated(EnumType.STRING)
    private PrestationScopeType scope;

    @Enumerated(EnumType.STRING)
    private Tooth tooth;

    @Enumerated(EnumType.STRING)
    private ToothFace toothFace;

    @Enumerated(EnumType.STRING)
    private Quadrant quadrant;

    @Enumerated(EnumType.STRING)
    private Maxillary maxillary;

    @Column(nullable = false, updatable = false)
    private BigDecimal price;



    /** Promoción
     * - Si hay promoción no puede haber descuento manual, y viceversa. El sistema debe validar esto.
     * - siempre en porcentaje
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    @Audited(targetAuditMode = org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED)
    private Promotion promotion;

    /** Valor calculado */
    @Column(name = "promotion_amount")
    private BigDecimal promotionAmount;


    /** Descuento manual
     * - Si el descuento viene en porcentaje, se calcula y se aplica
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type")
    private DiscountType discountType;

    /** Input usuario*/
    @Column(name = "discount_value")
    private BigDecimal discountValue;


    /** Valor calculado */
    @Column(name = "discount_amount")
    private BigDecimal discountAmount;

    /** Monto final después de aplicar promoción y descuentos */
    @Column(name = "final_amount",nullable = false)
    private BigDecimal finalAmount;

    private PrestationInstance(ConsultationInstance consultationInstance, PrestationType type, Odontogram odontogram,
                               PrestationInstanceStatus status, PrestationScopeType scope, Tooth tooth, ToothFace toothFace,
                               Quadrant quadrant, Maxillary maxillary, BigDecimal price, Promotion promotion,
                               BigDecimal promotionAmount, DiscountType discountType, BigDecimal discountValue,
                               BigDecimal discountAmount, BigDecimal finalAmount) {
        this.consultationInstance = consultationInstance;
        this.type = type;
        this.odontogram = odontogram;
        this.status = status;
        this.scope = scope;
        this.tooth = tooth;
        this.toothFace = toothFace;
        this.quadrant = quadrant;
        this.maxillary = maxillary;
        this.price = price;
        this.promotion = promotion;
        this.promotionAmount = promotionAmount;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
    }

    public static PrestationInstance build(ConsultationInstance consultationInstance, PrestationType type, Odontogram odontogram,
                                           PrestationInstanceStatus status, PrestationScopeType scope, Tooth tooth, ToothFace toothFace,
                                           Quadrant quadrant, Maxillary maxillary, BigDecimal price, Promotion promotion,
                                           DiscountType discountType, BigDecimal discountValue) {
        BigDecimal promotionAmount = promotion != null ? promotion.calculateAmount(price) : null;
        BigDecimal discountAmount  = discountType != null ? calculateDiscountAmount(price, discountType, discountValue) : null;
        BigDecimal finalAmount     = calculateFinalAmount(price, promotionAmount, discountAmount);
        return new PrestationInstance(consultationInstance, type, odontogram, status, scope, tooth, toothFace,
                                     quadrant, maxillary, price, promotion, promotionAmount, discountType, discountValue,
                                     discountAmount, finalAmount);
    }

    private static BigDecimal calculateDiscountAmount(BigDecimal price, DiscountType discountType, BigDecimal discountValue) {
        if (discountType == DiscountType.PERCENTAGE) {
            return price.multiply(discountValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return discountValue;
    }

    private static BigDecimal calculateFinalAmount(BigDecimal price, BigDecimal promotionAmount, BigDecimal discountAmount) {
        BigDecimal total = price;
        if (promotionAmount != null) total = total.subtract(promotionAmount);
        if (discountAmount  != null) total = total.subtract(discountAmount);

        if(total.compareTo(BigDecimal.ZERO) < 0){
            throw new ConflictException("exception.prestationInstance.totalInvalid.user",null,"exception.prestationInstance.totalInvalid.log",new Object[]{total, "prestationInstance","calculateFinalAmount"}, LogLevel.ERROR);
        }

        return total;
    }

    public void complete() {
        this.status = PrestationInstanceStatus.COMPLETED;
    }

    public String getScopeDetail() {
        if (scope == null) return null;
        return switch (scope) {
            case TOOTH      -> tooth != null ? tooth.name() : null;
            case TOOTH_FACE -> tooth + " - " + toothFace;
            case QUADRANT   -> quadrant != null ? quadrant.name() : null;
            case MAXILLARY  -> maxillary != null ? maxillary.name() : null;
            default         -> null;
        };
    }

}

