package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;


import com.odontologiaintegralfm.feature.appointment.core.dto.CalendarMonthResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;

import java.time.LocalDate;

public interface ICalendarService {

    Response<CalendarMonthResponseDTO> getCalendarMonth(Long idDentist, LocalDate month);


}
