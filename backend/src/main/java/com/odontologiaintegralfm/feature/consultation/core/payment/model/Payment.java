package com.odontologiaintegralfm.feature.consultation.core.payment.model;

import com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.enums.PaymentMethods;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method",nullable = false)
    private PaymentMethods method;

    private Payment(Patient patient, LocalDateTime date, BigDecimal totalAmount, PaymentMethods method) {
        this.patient = patient;
        this.date = date;
        this.totalAmount = totalAmount;
        this.method = method;
    }

    public static Payment build(Patient patient, LocalDateTime date, BigDecimal totalAmount, PaymentMethods method) {
        return new Payment(patient, date, totalAmount, method);
    }
}

