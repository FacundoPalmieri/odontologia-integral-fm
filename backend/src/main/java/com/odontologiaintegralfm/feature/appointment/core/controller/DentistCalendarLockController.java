package com.odontologiaintegralfm.feature.appointment.core.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileOrAppointmentsManagementCreateOrConfiguration;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IDentistLockCalendarService;
import com.odontologiaintegralfm.shared.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para relación de Dentista con un evento que bloquea el calendario.
 */

@RestController
@RequestMapping("/api/dentist-calendar-lock")
@Validated
public class DentistCalendarLockController {

    @Autowired
    private IDentistLockCalendarService dentistCalendarService;


    @Operation(summary = "Crear bloqueo de Agenda", description = "Permite crear un bloqueo de agenda para un dentista.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bloqueo creado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PostMapping("/{id}")
    @OnlyAccessUserProfileOrAppointmentsManagementCreateOrConfiguration
    public ResponseEntity<Response<DentistCalendarLockResponseDTO>>create(@PathVariable("id")Long idDentist,
                                                                          @Valid @RequestBody DentistCalendarLockCreateRequestDTO dentistCalendarLockCreateRequestDTO) {


        Response<DentistCalendarLockResponseDTO> response = dentistCalendarService.create(idDentist,dentistCalendarLockCreateRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }



}
