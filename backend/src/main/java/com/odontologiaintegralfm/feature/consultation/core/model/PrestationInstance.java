package com.odontologiaintegralfm.feature.consultation.core.model;

import com.odontologiaintegralfm.feature.consultation.catalogs.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.DiscountType;
import com.odontologiaintegralfm.feature.consultation.catalogs.model.Promotion;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.PrestationStatus;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.PrestationScopeType;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.Tooth;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.ToothFace;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.Quadrant;
import com.odontologiaintegralfm.feature.consultation.catalogs.enums.Maxillary;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

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
    private OdontogramDetail odontogramDetail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrestationStatus status;

    /** Solo si OdontogramDetail es null (prestación sin ubicación específica) */
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



    /** Si hay promoción no puede haber descuento manual, y viceversa. El sistema debe validar esto.*/

    /** Promoción */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    @Audited(targetAuditMode = org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED)
    private Promotion promotion;

    private BigDecimal promotionAmount;


    /** Descuento manual */
    /** Si el descuento viene en porcentaje, se calcula y se aplica*/
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private BigDecimal discountValue;

    private BigDecimal discountAmount;

    /** Monto final después de aplicar promoción y descuentos */
    @Column(nullable = false)
    private BigDecimal finalAmount;

    private PrestationInstance(ConsultationInstance consultationInstance, PrestationType type, OdontogramDetail odontogramDetail,
                               PrestationStatus status, PrestationScopeType scope, Tooth tooth, ToothFace toothFace,
                               Quadrant quadrant, Maxillary maxillary, BigDecimal price, Promotion promotion,
                               BigDecimal promotionAmount, DiscountType discountType, BigDecimal discountValue,
                               BigDecimal discountAmount, BigDecimal finalAmount) {
        this.consultationInstance = consultationInstance;
        this.type = type;
        this.odontogramDetail = odontogramDetail;
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

    public static PrestationInstance build(ConsultationInstance consultationInstance, PrestationType type, OdontogramDetail odontogramDetail,
                                           PrestationStatus status, PrestationScopeType scope, Tooth tooth, ToothFace toothFace,
                                           Quadrant quadrant, Maxillary maxillary, BigDecimal price, Promotion promotion,
                                           BigDecimal promotionAmount, DiscountType discountType, BigDecimal discountValue,
                                           BigDecimal discountAmount, BigDecimal finalAmount) {
        return new PrestationInstance(consultationInstance, type, odontogramDetail, status, scope, tooth, toothFace,
                                     quadrant, maxillary, price, promotion, promotionAmount, discountType, discountValue,
                                     discountAmount, finalAmount);
    }
}

