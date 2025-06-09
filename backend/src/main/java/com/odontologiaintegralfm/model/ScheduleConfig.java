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

    private String cronExpression;

}
