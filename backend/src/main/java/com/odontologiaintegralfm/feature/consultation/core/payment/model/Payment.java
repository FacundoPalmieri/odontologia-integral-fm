package com.odontologiaintegralfm.feature.consultation.core.payment.model;

import com.odontologiaintegralfm.feature.payment.catalogs.paymentprovider.enums.PaymentMethods;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
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
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private PaymentMethods method;

    private Payment(Patient patient, LocalDate date, Consultation consultation, BigDecimal totalAmount, PaymentMethods method) {
        this.patient = patient;
        this.date = date;
        this.consultation = consultation;
        this.totalAmount = totalAmount;
        this.method = method;
    }

    public static Payment build(Patient patient, LocalDate date, Consultation consultation, BigDecimal totalAmount, PaymentMethods method) {
        return new Payment(patient, date, consultation, totalAmount, method);
    }
}

