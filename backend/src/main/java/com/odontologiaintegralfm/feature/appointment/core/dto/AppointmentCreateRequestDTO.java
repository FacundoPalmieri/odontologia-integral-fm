package com.odontologiaintegralfm.feature.appointment.core.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * @author [Facundo Palmieri]
 */
public record AppointmentCreateRequestDTO(
        @NotNull(message = "appointmentCreateRequestDTO.idDentist.empty")
        Long idDentist,

        @NotNull(message = "appointmentCreateRequestDTO.idPatient.empty")
        Long idPatient,

        @NotNull(message = "appointmentCreateRequestDTO.dateTime.empty")
        LocalDateTime dateTime
) {
}
