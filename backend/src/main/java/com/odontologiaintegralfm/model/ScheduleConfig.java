package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa la configuración de las tareas programadas de limpieza.
 */
@Getter
@Setter
@Entity
@Table(name = "schedule_config")
public class ScheduleConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, updatable = false)
    private String name;

    @Column(nullable = false, length = 150)
    private String label;

    @Column(nullable = false, length = 30)
    private String cronExpression;

}
