package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.feature.appointment.core.dto.CalendarMonthResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.SlotResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.ICalendarService;
import com.odontologiaintegralfm.shared.response.Response;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CalendarService implements ICalendarService {


    /**
     * @param idDentist
     * @param month
     * @return
     */
    @Override
    public Response<CalendarMonthResponseDTO> getCalendarMonth(Long idDentist, LocalDate month) {

        //Obtener

    Response<CalendarMonthResponseDTO> response = null;

    return response;

    }



}
