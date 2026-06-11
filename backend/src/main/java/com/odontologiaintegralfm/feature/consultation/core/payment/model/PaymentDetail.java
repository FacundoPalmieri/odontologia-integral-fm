package com.odontologiaintegralfm.feature.consultation.core.payment.model;

import com.odontologiaintegralfm.feature.consultation.core.prestation.model.PrestationInstance;
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

/**
 * Entidad que representa el detalle de pago por prestación.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "payments_details")
@Where(clause = "enabled = true")
@Audited
public class PaymentDetail extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestation_instance_id", nullable = false)
    private PrestationInstance prestationInstance;

    /** Puede ser parcial o total. */
    @Column(nullable = false)
    private BigDecimal amount;

    private PaymentDetail(Payment payment, PrestationInstance prestationInstance, BigDecimal amount) {
        this.payment = payment;
        this.prestationInstance = prestationInstance;
        this.amount = amount;
    }

    public static PaymentDetail build(Payment payment, PrestationInstance prestationInstance, BigDecimal amount) {
        return new PaymentDetail(payment, prestationInstance, amount);
    }
}

