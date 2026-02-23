package com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.dto;


import com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.model.DentistHoliday;

import java.time.LocalDate;
import java.time.LocalTime;
public record DentistHolidayResponseDTO(
        Long id,
        Long idDentist,
        Long idHoliday,
        LocalDate date,
        String name,
        LocalTime startTime,
        LocalTime endTime,
        boolean enabled
) {
    public static DentistHolidayResponseDTO build(DentistHoliday dentistHoliday) {
        return new DentistHolidayResponseDTO(
                dentistHoliday.getId(),
                dentistHoliday.getDentist().getId(),
                dentistHoliday.getHoliday().getId(),
                dentistHoliday.getHoliday().getDate(),
                dentistHoliday.getHoliday().getName(),
                dentistHoliday.getStartTime(),
                dentistHoliday.getEndTime(),
                dentistHoliday.isEnabled()
        );
    }
}
