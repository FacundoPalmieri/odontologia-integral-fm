package com.odontologiaintegralfm.feature.appointment.core.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DentistHolidayResponseDTO {
    private Long idDentist;
    private List<DentistHolidayListResponseDTO> holiday;
}
