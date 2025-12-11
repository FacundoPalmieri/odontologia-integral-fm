package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;


import com.odontologiaintegralfm.feature.appointment.core.dto.CalendarDetailDayResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.CalendarMonthResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.CalendarWeekResponseDTO;
import com.odontologiaintegralfm.shared.dto.Response;

import java.time.LocalDate;

public interface ICalendarService {


    Response<CalendarDetailDayResponseDTO> getCalendarDay(Long idDentist, LocalDate day);

    Response<CalendarWeekResponseDTO> getCalendarWeek(Long idDentist,LocalDate day);

    Response<CalendarMonthResponseDTO> getCalendarMonth(Long idDentist, Integer year, Integer month);





}
