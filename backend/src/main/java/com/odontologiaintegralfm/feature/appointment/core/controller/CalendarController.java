package com.odontologiaintegralfm.feature.appointment.core.controller;

import com.odontologiaintegralfm.feature.appointment.core.dto.CalendarMonthResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.ICalendarService;
import com.odontologiaintegralfm.shared.response.Response;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController("api/calendar")
@Validated
public class CalendarController {

    @Autowired
    private ICalendarService calendarService;

    @GetMapping("/month")
    public ResponseEntity<Response<CalendarMonthResponseDTO>> getCalendarMonth(@RequestParam @NotNull Long idDentist,
                                                                               @RequestParam @NotNull LocalDate month) {

        Response<CalendarMonthResponseDTO> response = calendarService.getCalendarMonth(idDentist, month);
        return ResponseEntity.ok(response);
    }
}
