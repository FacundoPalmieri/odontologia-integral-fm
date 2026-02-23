package com.odontologiaintegralfm.feature.appointmentscheduling.locktype.dto;


import jakarta.validation.constraints.NotBlank;

public record CalendarLockTypeUpdateRequestDTO(

        @NotBlank(message = "calendarLockTypeCreateRequestDTO.name.empty")
        String name,

        boolean enabled
) {
}
