package com.odontologiaintegralfm.feature.consultation.prestationinstance.model;

import com.odontologiaintegralfm.feature.consultation.prestation.model.PrestationType;
import com.odontologiaintegralfm.feature.consultation.promotion.enums.DiscountType;
import com.odontologiaintegralfm.feature.consultation.promotion.model.Promotion;
import com.odontologiaintegralfm.feature.consultation.prestationinstance.enums.PrestationInstanceStatus;
import com.odontologiaintegralfm.feature.consultation.scope.model.Scope;
import com.odontologiaintegralfm.feature.consultation.treatmentinstance.model.TreatmentInstance;
import com.odontologiaintegralfm.feature.patient.model.Patient;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Checks;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Entidad que representa una instancia real de una prestación sobre un paciente.
 * La consulta donde avanzó cada step vive en {@link PrestationStepInstanceHistory} (D-14).
 */
@Entity
@Getter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "prestations_instances", indexes = {
        @Index(name = "idx_prestation_instance_patient_id", columnList = "patient_id"),
        @Index(name = "idx_prestation_instance_treatment_instance_id", columnList = "treatment_instance_id"),
        @Index(name = "idx_prestation_instance_origin_treatment_instance_id", columnList = "origin_treatment_instance_id")
})
@Checks({
        @Check(constraints = "price > 0"),
        @Check(constraints = "promotion_amount IS NULL OR promotion_amount > 0"),
        @Check(constraints = "discount_value   IS NULL OR discount_value   > 0"),
        @Check(constraints = "discount_amount  IS NULL OR discount_amount  > 0"),
        @Check(constraints = "final_amount     >= 0")
})
@Where(clause = "enabled = true")
@Audited
public class PrestationInstance extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prestation_type_id", nullable = false)
    private PrestationType type;

    /** Tratamiento sobre el que se aplica; excluyente con {@link #scope} propio (D-24, D-25). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "treatment_instance_id")
    private TreatmentInstance treatmentInstance;

    /** FK de pertenencia "en estrella" a la prótesis de origen; convive con treatmentInstance (D-43). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_treatment_instance_id")
    private TreatmentInstance originTreatmentInstance;

    /** Ubicación propia; nunca null (vacía cuando la ubicación viene del tratamiento o del origen). */
    @Embedded
    @AssociationOverride(name = "tooth", joinTable = @JoinTable(name = "prestations_instances_tooth",
            joinColumns = @JoinColumn(name = "prestation_instance_id")))
    private Scope scope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrestationInstanceStatus status;

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



    private PrestationInstance(Patient patient, PrestationType type, TreatmentInstance treatmentInstance,
                               TreatmentInstance originTreatmentInstance, Scope scope, PrestationInstanceStatus status,
                               BigDecimal price, Promotion promotion, BigDecimal promotionAmount, DiscountType discountType,
                               BigDecimal discountValue, BigDecimal discountAmount, BigDecimal finalAmount) {
        this.patient = patient;
        this.type = type;
        this.treatmentInstance = treatmentInstance;
        this.originTreatmentInstance = originTreatmentInstance;
        this.scope = scope;
        this.status = status;
        this.price = price;
        this.promotion = promotion;
        this.promotionAmount = promotionAmount;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
    }

    /**
     * Única vía para fijar la ubicación y las FKs de pertenencia (D-55). El monto de la promoción llega
     * ya calculado por el use case (ADR-0023). Un {@code scope} null se guarda como {@link Scope#empty()}.
     */
    public static PrestationInstance build(Patient patient, PrestationType type, TreatmentInstance treatmentInstance,
                                           TreatmentInstance originTreatmentInstance, Scope scope, PrestationInstanceStatus status,
                                           BigDecimal price, Promotion promotion, BigDecimal promotionAmount,
                                           DiscountType discountType, BigDecimal discountValue) {
        BigDecimal discountAmount  = discountType != null ? calculateDiscountAmount(price, discountType, discountValue) : null;
        BigDecimal finalAmount     = calculateFinalAmount(price, promotionAmount, discountAmount);
        return new PrestationInstance(patient, type, treatmentInstance, originTreatmentInstance,
                scope != null ? scope : Scope.empty(), status, price, promotion, promotionAmount,
                discountType, discountValue, discountAmount, finalAmount);
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

    /** Único setter: el estado se deriva de los steps, nunca lo manda el front (D-27, D-55). */
    public void applyStatus(PrestationInstanceStatus status) {
        this.status = status;
    }
}
