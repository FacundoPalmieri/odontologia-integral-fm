package com.odontologiaintegralfm.feature.consultation.core.model;

import com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.enums.PaymentMethods;
import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
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
 * Entidad que representa un pago realizado en una consulta.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "payments")
@Where(clause = "enabled = true")
@Audited
public class Payment extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_instance_id", nullable = false)
    private ConsultationInstance consultationInstance;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private PaymentMethods paymentMethod;

    private Payment(ConsultationInstance consultationInstance, BigDecimal totalAmount, PaymentMethods paymentMethod) {
        this.consultationInstance = consultationInstance;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
    }

    public static Payment build(ConsultationInstance consultationInstance, BigDecimal totalAmount, PaymentMethods paymentMethod) {
        return new Payment(consultationInstance, totalAmount, paymentMethod);
    }
}

