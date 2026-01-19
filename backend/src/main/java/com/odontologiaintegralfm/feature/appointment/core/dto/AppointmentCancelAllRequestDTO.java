package com.odontologiaintegralfm.feature.appointment.core.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotEmpty;

public record AppointmentCancelAllRequestDTO(

        @Lob
        @NotEmpty
        String observation
) {
}
