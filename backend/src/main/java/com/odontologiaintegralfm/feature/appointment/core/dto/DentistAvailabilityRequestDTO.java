package com.odontologiaintegralfm.feature.appointment.core.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO que representa la disponibilidad de la jornada de trabajo de un dentista
 */
public record DentistAvailabilityRequestDTO(
        @NotNull
        Long idDentist,

        @NotEmpty
        List<WorkingDayDTO> days
) {
}
