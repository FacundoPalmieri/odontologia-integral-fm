package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa la configuración de mensajes para el usuario y los logs.
 */
@Entity
@Data
@Table(name = "Mensajes")
public class MessageConfig {

    /** Identificador único del mensaje.*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**Clave única del mensaje.*/
    @Column(name="clave", unique = true, nullable = false)
    private String key;

    /**Valor que representa el mensaje*/
    @Column(name = "valor")
    private  String value;

    /**Configuración de la región.*/
    @Column(name="locale")
    private String locale;

}
