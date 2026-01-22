package com.odontologiaintegralfm.feature.appointment.catalogs.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessAppointmentsManagementOrConfigurationRead;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationUpdate;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.CalendarLockModeResponseDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.CalendarLockTypeCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.CalendarLockTypeResponseDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.CalendarLockTypeUpdateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.service.ICalendarLockTypeService;
import com.odontologiaintegralfm.shared.dto.Response;
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
import java.util.Set;

/**
 * Controlador para CRUD de catálogos de bloqueos de agenda.
 */

@RestController
@RequestMapping("/api/calendar-lock-type")
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
    @OnlyAccessAppointmentsManagementOrConfigurationRead
    public ResponseEntity<Response<List<CalendarLockTypeResponseDTO>>> getAll(){
        Response<List<CalendarLockTypeResponseDTO>> response = calendarLockTypeService.getAll();
        return ResponseEntity.ok(response);
    }




    @Operation(summary = "Obtener un de bloqueo de agenda", description = "Obtiene un bloqueo de agenda por su id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bloqueos obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/{id}")
    @OnlyAccessAppointmentsManagementOrConfigurationRead
    public ResponseEntity<Response<CalendarLockTypeResponseDTO>> getById(@PathVariable @NotNull Long id){
        Response<CalendarLockTypeResponseDTO> response = calendarLockTypeService.getById(id);
        return ResponseEntity.ok(response);
    }



    @Operation(summary = "Obtener los modos para un evento de bloqueo", description = "Obtiene todos los modos posibles para un evento de bloqueo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Modos obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/mode")
    @OnlyAccessAppointmentsManagementOrConfigurationRead
    public ResponseEntity<Response<Set<CalendarLockModeResponseDTO>>> getMode(){
        Response<Set<CalendarLockModeResponseDTO>> response = calendarLockTypeService.getModes();
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




    @Operation(summary = "Actualizar un tipo de bloqueo de agenda.", description = "Actualiza un tipo de bloqueo de agenda")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipos de bloqueo actualizado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PatchMapping("/{id}")
    @OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationUpdate
    public ResponseEntity<Response<CalendarLockTypeResponseDTO>> update(@PathVariable @NotNull Long id,
                                                                        @Valid @RequestBody CalendarLockTypeUpdateRequestDTO calendarLockTypeUpdateRequestDTO){
        Response<CalendarLockTypeResponseDTO> response = calendarLockTypeService.update(id,calendarLockTypeUpdateRequestDTO);
        return ResponseEntity.ok(response);

    }





}
