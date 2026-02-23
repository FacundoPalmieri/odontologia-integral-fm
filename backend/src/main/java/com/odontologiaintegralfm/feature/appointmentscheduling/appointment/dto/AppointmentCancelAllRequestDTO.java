package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotEmpty;

public record AppointmentCancelAllRequestDTO(

        @Lob
        @NotEmpty
        String observation
) {
}
