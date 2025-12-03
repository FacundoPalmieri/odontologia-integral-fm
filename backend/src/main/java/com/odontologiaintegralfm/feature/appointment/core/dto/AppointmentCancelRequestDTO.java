package com.odontologiaintegralfm.feature.appointment.core.dto;

import com.odontologiaintegralfm.feature.appointment.core.enums.AppointmentActionRequester;
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
