package com.odontologiaintegralfm.infrastructure.logging.model;

import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad para representar en una vista:
 * - Logs
 * - Exceptions
 */
@Getter
@Setter
@Entity
@Table(name = "system_logs")
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Fecha de log */
    private LocalDateTime timestamp;

    /** Nivel de Log ( INFO, WARN, ERROR, etc.) */
    @Enumerated(EnumType.STRING)
    private LogLevel level; //

    /** Tipo de Log (EXCEPTION, TASK, SYSTEM,) */
    @Enumerated(EnumType.STRING)
    private LogType type;

    /** Mensaje para el usuario */
    private String userMessage;

    /** Mensaje técnico*/
    @Column(columnDefinition = "TEXT")
    private String technicalMessage;

    /** Nombre de la exception o LogActionAspect */
    private String name;

    /** Usuario autenticado */
    private String username;

    /** json de argumentos dinámicos (opcional)*/
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /** Detalle del stackTrace (Opcional - Solo para exception */
    @Column(columnDefinition = "TEXT")
    private String stackTrace;

}
