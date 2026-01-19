package com.odontologiaintegralfm.feature.appointment.core.dto;


import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO para respuesta de calendario vista diaria
 */
@Getter
@Setter
public class CalendarDayResponseDTO {
    private Long dentistId;
    private LocalDate day;
    private CalendarDayStatusResponseDTO calendarDayStatus; //Se devuelve para luego sacar el estado del mes.
    private CalendarHolidayResponseDTO holiday;
    private List<SlotResponseDTO> slots;


    private CalendarDayResponseDTO (Long dentistId, LocalDate day, CalendarDayStatusResponseDTO calendarDayStatus, CalendarHolidayResponseDTO holiday, List<SlotResponseDTO> slots){
        this.dentistId = dentistId;
        this.day = day;
        this.calendarDayStatus = calendarDayStatus;
        this.holiday = holiday;
        this.slots = slots;
    }

    public static CalendarDayResponseDTO build(Long dentistId, LocalDate day, CalendarDayStatusResponseDTO calendarDayStatus, CalendarHolidayResponseDTO holiday, List<SlotResponseDTO> slots){
        return new CalendarDayResponseDTO(
                dentistId,
                day,
                calendarDayStatus,
                holiday,
                slots
        );
    }
}
