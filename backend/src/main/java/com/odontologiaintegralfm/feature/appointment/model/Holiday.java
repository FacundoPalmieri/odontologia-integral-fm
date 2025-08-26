package com.odontologiaintegralfm.feature.appointment.model;

import com.odontologiaintegralfm.feature.appointment.enums.HolidayType;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

/**
 * Entidad que representa Feriados y días no laborables.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "holidays",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"date"})
        }
)
@Where(clause = "enabled = true")
public class Holiday extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HolidayType type;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private Integer year; // Para filtros rápidos.
}
