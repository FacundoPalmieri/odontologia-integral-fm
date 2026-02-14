package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppointmentCreateRequestDTO {
        @NotNull(message = "appointmentCreateRequestDTO.idDentist.empty")
        private Long idDentist;

        @NotNull(message = "appointmentCreateRequestDTO.idPatient.empty")
        private Long idPatient;

        @NotNull(message = "appointmentCreateRequestDTO.dateTime.empty")
        private LocalDateTime dateTime;

}

