package com.odontologiaintegralfm.feature.appointment.catalogs.dto;


import jakarta.validation.constraints.NotNull;

public record CalendarLockTypeCreateRequestDTO(
        @NotNull(message = "calendarLockTypeCreateRequestDTO.name.empty")
        String name
) {
}
