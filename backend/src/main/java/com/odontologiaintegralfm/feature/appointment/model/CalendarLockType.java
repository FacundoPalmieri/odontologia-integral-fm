package com.odontologiaintegralfm.feature.appointment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa los tipos de bloqueos de calendario.
 * No representa bloqueo por calendario o turnos,
 * sino por eventos propios del profesional.
 */

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "calendar_lock_type")
public class CalendarLockType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
