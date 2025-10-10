package com.odontologiaintegralfm.feature.appointment.catalogs.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CalendarLockTypeUpdateRequestDTO(

        @NotBlank(message = "calendarLockTypeCreateRequestDTO.name.empty")
        String name,

        boolean enabled
) {
}
