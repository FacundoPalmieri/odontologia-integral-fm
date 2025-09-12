package com.odontologiaintegralfm.feature.appointment.core.dto;

import com.odontologiaintegralfm.feature.appointment.catalogs.enums.DayName;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

/**
 * DTO que representa la disponibilidad diaria de un dentista.
 * Es hijo de {@link DentistAvailabilityRequestDTO} y {@link DentistAvailabilityResponseDTO}
 */
public record WorkingDayDTO(
        @NotNull
        DayName dayName,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime,

        @NotNull
        Integer appointmentDuration // en minutos
) {
}
