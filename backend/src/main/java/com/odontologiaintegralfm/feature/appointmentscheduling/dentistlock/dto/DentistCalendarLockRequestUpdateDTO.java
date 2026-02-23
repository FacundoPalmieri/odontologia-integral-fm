package com.odontologiaintegralfm.feature.appointmentscheduling.dentistlock.dto;


import jakarta.validation.constraints.NotNull;

public record DentistCalendarLockRequestUpdateDTO(

        @NotNull(message = "dentistCalendarLockCreateRequestDTO.lockType.empty")
        Long idDentistCalendarLock,

        String observationUpdate

) {
}
