package com.odontologiaintegralfm.feature.consultation.catalogs.paymentprovider.model;

import com.odontologiaintegralfm.feature.consultation.catalogs.paymentprovider.enums.ProviderType;
import com.odontologiaintegralfm.shared.model.AuditableJPA;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidades bancarias o plataformas de pago que se pueden asociar a los métodos de pago (PaymentMethods).
 */
@Getter
@Setter
@Entity
@Table(name = "payment_providers")
public class PaymentProvider extends AuditableJPA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name; // Santander, Mercado Pago


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderType type; // Banco, Billetera virtual.

}