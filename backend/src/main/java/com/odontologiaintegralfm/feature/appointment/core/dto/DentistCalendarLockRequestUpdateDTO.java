package com.odontologiaintegralfm.feature.appointment.core.dto;


import jakarta.validation.constraints.NotNull;

public record DentistCalendarLockRequestUpdateDTO(

        @NotNull(message = "dentistCalendarLockCreateRequestDTO.lockType.empty")
        Long idDentistCalendarLock,

        String observationUpdate

) {
}
