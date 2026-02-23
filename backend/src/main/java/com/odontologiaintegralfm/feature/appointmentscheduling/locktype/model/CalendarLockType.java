package com.odontologiaintegralfm.feature.appointmentscheduling.locktype.model;

import com.odontologiaintegralfm.feature.appointmentscheduling.locktype.enums.CalendarLockMode;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.HashSet;
import java.util.Set;

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
@Where(clause = "enabled = true")
public class CalendarLockType extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;


    /**
     * Modos de bloqueo habilitados para este tipo de evento.
     * Ej: CURSO → POINTUAL, RECURRENT_PATTERN
     *     VACACIONES → DAILY_CONTINUOUS
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "calendar_lock_type_modes",
            joinColumns = @JoinColumn(name = "calendar_lock_type_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    private Set<CalendarLockMode> modes = new HashSet<>();


    private CalendarLockType(String name,Set<CalendarLockMode> modes ) {
        this.name = name;
        this.modes = modes;

    }

    public static CalendarLockType build (String name, Set<CalendarLockMode> modes) {
        return new CalendarLockType(name, modes);
    }
}
