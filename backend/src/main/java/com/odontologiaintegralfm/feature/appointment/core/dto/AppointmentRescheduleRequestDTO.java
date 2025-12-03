package com.odontologiaintegralfm.feature.appointment.core.dto;


import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentActionRequester;
import jakarta.validation.constraints.NotNull;

public record AppointmentRescheduleRequestDTO(
        AppointmentCreateRequestDTO appointment,

        @NotNull(message = "appointmentRequestDTO.requestSource.empty")
        AppointmentActionRequester requestSource,

        @NotNull(message = "appointmentRescheduleRequestDTO.observation.empty")
        String observation

) {
}
