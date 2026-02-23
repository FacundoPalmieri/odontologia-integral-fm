package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto;

import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentActionRequester;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;


public record AppointmentCancelRequestDTO(

        @NotNull(message = "appointmentRequestDTO.requestSource.empty")
        AppointmentActionRequester requestSource,

        @Lob
        @NotEmpty
        String observation
) {
}
