package com.odontologiaintegralfm.model;

import com.odontologiaintegralfm.enums.ScheduledTaskKey;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa la configuración de las tareas programadas de limpieza.
 */
@Getter
@Setter
@Entity
@Table(name = "schedule_task")
public class ScheduleTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50, updatable = false)
    @Enumerated(EnumType.STRING)
    private ScheduledTaskKey keyName;

    @Column(nullable = false, length = 150)
    private String label;

    @Column(nullable = false, length = 30)
    private String cronExpression;

}
