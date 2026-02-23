package com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointmentscheduling.shared.DayName;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.enums.OriginConflict;
import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.model.DentistAvailability;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO que representa un día laboral del dentista.
 * Reglas de validación:
 * <ul>
 *   <li>specificDate XOR dayName  (uno solo, no ambos)
 *   <li>specificDate XOR recurrence (si hay fecha específica no puede haber recurrencia)
 * </ul>
 *
 * Lógica de negocio relevante:
 * <ul>
 *   <li>Cuando viene specificDate -> entrada única.
 *   <li>Cuando viene dayName + recurrence -> entrada recurrente.
 *   <li>effectiveDate nunca viene en el request: lo calcula el backend.
 * </ul>
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkingDayDTO {


    // ---------------------------------------------------//
    /** Día de trabajo + recurrencia */
    private DayName dayName;

    private CalendarLockRecurrenceName recurrence;



    // ---------------------------------------------------//
    /** Fecha específica de asistencia.(Ej. Concurre una vez por mes, y no la misma fecha */
    @Future(message = "generic.date.futureOrPresent")
    private LocalDate specificDate;

    // ---------------------------------------------------//


    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    private Integer appointmentDuration; // en minutos

    /** Solo para respuesta. Fecha real en la que entra en vigencia el primer día laboral*/
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(hidden = true)
    private LocalDate effectiveDate;


    /** Campos que representar un break dentro de la jornada laboral. */
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;



    /** Solo uso interno: motivo/origen del conflicto */
    @Null
    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(hidden = true)
    private OriginConflict originConflict;

    /** Solo uso interno: id propio de esta entidad que es la que puede generar conflictos */
    @Null
    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(hidden = true)
    private Long idOriginConflict;


    @JsonIgnore
    @AssertTrue(message = "dentistWorkingDayDto.isExclusiveChoice")
    public boolean isExclusiveChoice() {
        boolean hasSpecificDate = specificDate != null;
        boolean hasWorkingDays = dayName != null;
        return hasSpecificDate ^ hasWorkingDays;
    }

    @JsonIgnore
    @AssertTrue(message = "dentistWorkingDayDto.isRecurrenceValid")
    private boolean isRecurrenceValid() {
        boolean hasSpecificDate = specificDate != null;
        boolean hasRecurrence = recurrence != null;
        return hasRecurrence ^ hasSpecificDate;
    }


    public WorkingDayDTO(
            DayName dayName,
            CalendarLockRecurrenceName recurrence,
            LocalDate specificDate,
            LocalTime startTime,
            LocalTime endTime,
            Integer appointmentDuration,
            LocalDate effectiveDate,
            LocalTime breakStartTime,
            LocalTime breakEndTime
    ) {
        this.dayName = dayName;
        this.recurrence = recurrence;
        this.specificDate = specificDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.appointmentDuration = appointmentDuration;
        this.effectiveDate = effectiveDate;
        this.breakStartTime = breakStartTime;
        this.breakEndTime = breakEndTime;
    }


    public static WorkingDayDTO from (DentistAvailability dentistAvailability){
       return new WorkingDayDTO(
               dentistAvailability.getKeyName(),
               dentistAvailability.getRecurrence(),
               dentistAvailability.getSpecificDate(),
               dentistAvailability.getStartTime(),
               dentistAvailability.getEndTime(),
               dentistAvailability.getAppointmentDuration(),
               dentistAvailability.getEffectiveDate(),
               dentistAvailability.getBreakStartTime(),
               dentistAvailability.getBreakEndTime()
       );
    }
}
