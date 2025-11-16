package com.odontologiaintegralfm.feature.appointment.core.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessPersonProfileAndAppointmentsManagementOrConfigurationCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessPersonProfileAndAppointmentsManagementOrConfigurationUpdate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationUpdate;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistCalendarLockRequestUpdateDTO;
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
    @PostMapping("/{idPerson}")
    @OnlyAccessPersonProfileAndAppointmentsManagementOrConfigurationCreate
    public ResponseEntity<Response<DentistCalendarLockResponseDTO>>create(@PathVariable("idPerson")Long idPerson,
                                                                          @Valid @RequestBody DentistCalendarLockRequestCreateDTO dentistCalendarLockRequestCreateDTO) {


        Response<DentistCalendarLockResponseDTO> response = dentistCalendarService.create(idPerson, dentistCalendarLockRequestCreateDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }


    @PatchMapping("/{idPerson}")
    @OnlyAccessPersonProfileAndAppointmentsManagementOrConfigurationUpdate
    public ResponseEntity<Response<DentistCalendarLockResponseDTO>> update(@PathVariable("idPerson")Long idPerson,
                                                                           @Valid @RequestBody DentistCalendarLockRequestUpdateDTO dentistCalendarLockRequestUpdateDTO) {

        Response<DentistCalendarLockResponseDTO> response = dentistCalendarService.update(dentistCalendarLockRequestUpdateDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }



}
