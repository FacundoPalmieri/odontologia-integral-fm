package com.odontologiaintegralfm.feature.consultation.core.model;

import com.odontologiaintegralfm.feature.consultation.catalogs.model.PaymentProvider;
import com.odontologiaintegralfm.shared.model.Auditable;
import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

/**
 * Entidad que representa una cuenta asociado a un método de pago electrónico.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Table(name = "payment_accounts")
@Where(clause = "enabled = true")
public class PaymentAccount extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Alias de la cuenta (ej: alias de Mercado Pago o banco)
     */
    @Column(length = 100)
    private String alias;

    /**
     * CBU o CVU según el tipo de proveedor
     */
    @Column(length = 22)


    @EqualsAndHashCode.Include
    private String accountIdentifier;

    /**
     * Titular de la cuenta
     */
    @Column(nullable = false, length = 100)
    private String holderName;

    /**
     * Banco o billetera virtual (ej: Santander, Galicia, Mercado Pago)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name ="provider_id", nullable = false)
    private PaymentProvider provider;

}