package com.odontologiaintegralfm.feature.appointment.catalogs.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationRead;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationUpdate;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.CalendarLockTypeCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.CalendarLockTypeResponseDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.CalendarLockTypeUpdateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.service.ICalendarLockTypeService;
import com.odontologiaintegralfm.shared.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controlador para CRUD de catálogos de bloqueos de agenda.
 */

@RestController
@RequestMapping("api/calendar-lock-type")
@Validated
public class CalendarLockTypeController {

    @Autowired
    private ICalendarLockTypeService calendarLockTypeService;

    @Operation(summary = "Obtener tipos de bloqueos de agenda", description = "Obtiene la lista de bloqueos de agenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipos de bloqueos obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    @OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationRead
    public ResponseEntity<Response<List<CalendarLockTypeResponseDTO>>> getAll(){
        Response<List<CalendarLockTypeResponseDTO>> response = calendarLockTypeService.getAll();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    @OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationRead
    public ResponseEntity<Response<CalendarLockTypeResponseDTO>> getById(@PathVariable @NotNull Long id){
        Response<CalendarLockTypeResponseDTO> response = calendarLockTypeService.getById(id);
        return ResponseEntity.ok(response);
    }



    @Operation(summary = "Crear un tipo de bloqueo de agenda.", description = "Crea un nuevo tipo de bloqueo de agenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipos de bloqueo creado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PostMapping
    @OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationCreate
    public ResponseEntity<Response<CalendarLockTypeResponseDTO>> create(@Valid @RequestBody CalendarLockTypeCreateRequestDTO calendarLockTypeCreateRequestDTO){
        Response<CalendarLockTypeResponseDTO> response = calendarLockTypeService.create(calendarLockTypeCreateRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @PatchMapping("/{id}")
    @OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationUpdate
    public ResponseEntity<Response<CalendarLockTypeResponseDTO>> update(@PathVariable @NotNull Long id,
                                                                        @Valid @RequestBody CalendarLockTypeUpdateRequestDTO calendarLockTypeUpdateRequestDTO){
        Response<CalendarLockTypeResponseDTO> response = calendarLockTypeService.update(id,calendarLockTypeUpdateRequestDTO);
        return ResponseEntity.ok(response);

    }



}
