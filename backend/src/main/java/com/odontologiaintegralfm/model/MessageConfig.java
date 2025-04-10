package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa la configuración de mensajes para el usuario y los logs.
 */
@Entity
@Data
@Table(name = "message_config")
public class MessageConfig {

    /** Identificador único del mensaje.*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**Clave única del mensaje.*/
    @Column(name = "message_key", unique = true, nullable = false)
    private String key;

    /**Valor que representa el mensaje*/
    private  String value;

    /**Configuración de la región.*/
    @Column(length = 50)
    private String locale;

}
