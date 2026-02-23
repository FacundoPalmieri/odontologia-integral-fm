package com.odontologiaintegralfm.feature.appointmentscheduling.calendar.dto;


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
    private Long dentistHolidayId;
    private LocalDate day;
    private CalendarDayStatusResponseDTO calendarDayStatus; //Se devuelve para luego sacar el estado del mes.
    private CalendarHolidayResponseDTO holiday;
    private List<CalendarDentistLock> dentistLock;
    private List<SlotResponseDTO> slots;


    private CalendarDayResponseDTO (Long dentistId,Long dentistHolidayId, LocalDate day, CalendarDayStatusResponseDTO calendarDayStatus, CalendarHolidayResponseDTO holiday,List<CalendarDentistLock> dentistLock, List<SlotResponseDTO> slots){
        this.dentistId = dentistId;
        this.dentistHolidayId = dentistHolidayId;
        this.day = day;
        this.calendarDayStatus = calendarDayStatus;
        this.holiday = holiday;
        this.dentistLock = dentistLock;
        this.slots = slots;
    }

    public static CalendarDayResponseDTO build(Long dentistId,Long dentistHolidayId, LocalDate day, CalendarDayStatusResponseDTO calendarDayStatus, CalendarHolidayResponseDTO holiday,List <CalendarDentistLock> dentistLock, List<SlotResponseDTO> slots){
        return new CalendarDayResponseDTO(
                dentistId,
                dentistHolidayId,
                day,
                calendarDayStatus,
                holiday,
                dentistLock,
                slots
        );
    }
}
