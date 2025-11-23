package com.odontologiaintegralfm.feature.appointment.core.dto;


import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentRequestSource;
import jakarta.validation.constraints.NotNull;

public record AppointmentRescheduleRequestDTO(
        AppointmentCreateRequestDTO appointment,

        @NotNull(message = "appointmentRescheduleRequestDTO.requestSource.empty")
        AppointmentRequestSource requestSource,

        @NotNull(message = "appointmentRescheduleRequestDTO.observation.empty")
        String observation

) {
}
