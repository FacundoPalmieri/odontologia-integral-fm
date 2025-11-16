package com.odontologiaintegralfm.feature.appointment.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.CalendarLockRecurrenceName;
import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import com.odontologiaintegralfm.feature.appointment.core.enums.OriginConflict;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO que representa la disponibilidad diaria de un dentista.
 * Es hijo de {@link DentistAvailabilityResponseDTO}
 */
@Getter
@Setter
@AllArgsConstructor
public class WorkingDayDTO {


    private DayName dayName;

    private CalendarLockRecurrenceName recurrence;

    /** Fecha específica de asistencia.(Ej. Concurre una vez por mes, y no la misma fecha */
    private LocalDate specificDate;

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



    /** Solo uso interno: id propio de esta entidad que es la que puede generar conflictos */
    @Null
    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(hidden = true)
    private Long idOriginConflict;

    /** Solo uso interno: motivo/origen del conflicto */
    @Null
    @JsonIgnore
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(hidden = true)
    private OriginConflict originConflict;


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


}
