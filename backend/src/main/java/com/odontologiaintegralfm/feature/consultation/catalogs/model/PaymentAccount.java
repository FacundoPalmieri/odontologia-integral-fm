package com.odontologiaintegralfm.feature.consultation.catalogs.model;

import com.odontologiaintegralfm.shared.model.Auditable;
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
public class PaymentAccount extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
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



    private PaymentAccount(String alias, String accountIdentifier, String holderName, PaymentProvider provider) {
        this.alias = alias;
        this.accountIdentifier = accountIdentifier;
        this.holderName = holderName;
        this.provider = provider;
    }

    public static PaymentAccount build(String alias, String accountIdentifier, String holderName, PaymentProvider provider) {
        return new PaymentAccount(alias, accountIdentifier, holderName, provider);
    }
}