package com.odontologiaintegralfm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa la parametrización de días de baja lógica mínima para que la tarea programada {@link ScheduleConfig}
 * efectúe la limpieza
 */

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class AttachedFileConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Días mínimos permitidos sin borrar físicamente */
    @Column(nullable = false)
    private Long days;

}
