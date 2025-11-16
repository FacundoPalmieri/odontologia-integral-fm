package com.odontologiaintegralfm.feature.appointment.core.model;

import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * @author [Facundo Palmieri]
 */
@Entity
@Getter
@Setter
@Table(name = "dentist_calendar_lock_details")
public class DentistCalendarLockDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lock_id", nullable = false)
    private DentistCalendarLock lock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayName dayName;
}
