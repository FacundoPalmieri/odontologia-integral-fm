package com.odontologiaintegralfm.feature.appointment.core.controller;

import com.odontologiaintegralfm.feature.appointment.core.dto.CalendarDayResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.CalendarMonthResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.CalendarWeekResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.ICalendarService;
import com.odontologiaintegralfm.shared.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;


@RestController
@RequestMapping("/api/calendar")
@Validated
public class CalendarController {

    @Autowired
    private ICalendarService calendarService;



    @Operation(summary = "Obtener calendario día", description = "Obtiene el detalle de los slot del día ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slots recuperados correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "409", description = "Dentista no poseé jornada laboral parametrizada."),
    })
    @GetMapping("/{idDentist}/day")
    public ResponseEntity<Response<CalendarDayResponseDTO>> getCalendarDay(@PathVariable @NotNull Long idDentist,
                                                                           @RequestParam @NotNull LocalDate day) {

        Response<CalendarDayResponseDTO> response = calendarService.getCalendarDay(idDentist, day);
        return ResponseEntity.ok(response);
    }



    @Operation(summary = "Obtener calendario semanal", description = "Obtiene el detalle de los slot para una semana calendario ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slots recuperados correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "409", description = "Dentista no poseé jornada laboral parametrizada."),
    })
    @GetMapping("/{idDentist}/week")
    public ResponseEntity<Response<CalendarWeekResponseDTO>>getCalendarWeek(@PathVariable @NotNull Long idDentist,
                                                                            @RequestParam @NotNull LocalDate day) {

        Response<CalendarWeekResponseDTO> response = calendarService.getCalendarWeek(idDentist, day);
        return ResponseEntity.ok(response);
    }



    @Operation(summary = "Obtener calendario mensual", description = "Obtiene resumen por cada dia de un calendario mensual ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dias recuperados correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "409", description = "Dentista no poseé jornada laboral parametrizada."),
    })
    @GetMapping("/{idDentist}/month")
    public ResponseEntity<Response<CalendarMonthResponseDTO>>getCalendarMonth(@PathVariable @NotNull Long idDentist,
                                                                              @NotNull @Min(2025) Integer year,
                                                                              @NotNull @Min(1) @Max(12)Integer month) {

        Response<CalendarMonthResponseDTO> response = calendarService.getCalendarMonth(idDentist, year, month);
        return ResponseEntity.ok(response);
    }



}
