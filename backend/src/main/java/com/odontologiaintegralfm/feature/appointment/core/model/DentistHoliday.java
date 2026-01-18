package com.odontologiaintegralfm.feature.appointment.core.model;

import com.odontologiaintegralfm.feature.appointment.catalogs.model.Holiday;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayRequestCreateDTO;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.shared.model.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

import java.time.LocalTime;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

/**
 * Entidad que representa la relación entre un Dentista y un día Feriado o no laborable
 */
@Entity
@Audited
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "dentists_holidays",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"dentist_id","holiday_id"})
        }
)
@Where(clause = "enabled = true")
public class DentistHoliday extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dentist_id", nullable = false, updatable = false)
    private Dentist dentist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holiday_id", nullable = false, updatable = false)
    @Audited(targetAuditMode = NOT_AUDITED)
    private Holiday holiday;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer appointmentDuration; // en minutos


    /** Campos que representar un break dentro de la jornada laboral. */
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;


    private DentistHoliday(Dentist dentist, Holiday holiday, LocalTime startTime, LocalTime endTime, Integer appointmentDuration, LocalTime breakStartTime, LocalTime breakEndTime) {
        this.dentist = dentist;
        this.holiday = holiday;
        this.startTime = startTime;
        this.endTime = endTime;
        this.appointmentDuration = appointmentDuration;
        this.breakStartTime = breakStartTime;
        this.breakEndTime = breakEndTime;

    }

    public static DentistHoliday build (Dentist dentist, Holiday holiday, DentistHolidayRequestCreateDTO dentistHolidayRequestCreateDTO) {
        return new DentistHoliday(
                dentist,
                holiday,
                dentistHolidayRequestCreateDTO.startTime(),
                dentistHolidayRequestCreateDTO.endTime(),
                dentistHolidayRequestCreateDTO.appointmentDuration(),
                dentistHolidayRequestCreateDTO.breakStartTime(),
                dentistHolidayRequestCreateDTO.breakEndTime()
        );
    }


}

