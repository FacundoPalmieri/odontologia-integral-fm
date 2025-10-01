package com.odontologiaintegralfm.feature.appointment.core.dto;


import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DentistHolidayRequestDTO(

        @NotNull
        Integer year,

        List<DentistHolidayListRequestDTO> holiday

) {
}
