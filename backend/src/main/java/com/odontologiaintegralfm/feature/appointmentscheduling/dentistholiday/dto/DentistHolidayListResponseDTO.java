package com.odontologiaintegralfm.feature.appointmentscheduling.dentistholiday.dto;

import java.time.LocalTime;

/**
 * DTO que se usa dentro de la lista en {@link  DentistHolidayResponseDTO}
 */
public record DentistHolidayListResponseDTO(
        Long id,
        Long idHoliday,
        LocalTime startTime,
        LocalTime endTime
) {
}