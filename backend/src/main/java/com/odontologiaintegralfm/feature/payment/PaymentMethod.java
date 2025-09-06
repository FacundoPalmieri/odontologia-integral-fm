package com.odontologiaintegralfm.feature.payment;

import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa el medio de pago.
 * Ej: Efectivo, Transferencia, etc.
 */

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name ="payment_methods")
public class PaymentMethod extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 20, unique = true, nullable = false)
    private String name;

}
