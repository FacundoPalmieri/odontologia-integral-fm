package com.odontologiaintegralfm.feature.appointmentscheduling.calendar.service;


import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.dto.CalendarDayResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.dto.CalendarMonthResponseDTO;
import com.odontologiaintegralfm.feature.appointmentscheduling.calendar.dto.CalendarWeekResponseDTO;
import com.odontologiaintegralfm.shared.dto.Response;

import java.time.LocalDate;

public interface ICalendarService {


    Response<CalendarDayResponseDTO> getCalendarDay(Long idDentist, LocalDate day);

    Response<CalendarWeekResponseDTO> getCalendarWeek(Long idDentist,LocalDate day);

    Response<CalendarMonthResponseDTO> getCalendarMonth(Long idDentist, Integer year, Integer month);





}
