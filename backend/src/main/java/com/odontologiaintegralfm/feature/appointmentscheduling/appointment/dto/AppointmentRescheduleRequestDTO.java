package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto;


import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentActionRequester;
import jakarta.validation.constraints.NotNull;

public record AppointmentRescheduleRequestDTO(
        AppointmentCreateRequestDTO appointment,

        @NotNull(message = "appointmentRequestDTO.requestSource.empty")
        AppointmentActionRequester requestSource,

        @NotNull(message = "appointmentRescheduleRequestDTO.observation.empty")
        String observation

) {
}
