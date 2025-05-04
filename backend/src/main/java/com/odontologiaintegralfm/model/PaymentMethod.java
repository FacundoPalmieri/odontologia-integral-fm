package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa el medio de pago.
 * Ej: Efectivo, Transferencia, etc.
 */

@Entity
@Data
@Table(name ="payment_methods")
public class PaymentMethod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, unique = true, nullable = false)
    private String name;

    private Boolean enabled;
}
