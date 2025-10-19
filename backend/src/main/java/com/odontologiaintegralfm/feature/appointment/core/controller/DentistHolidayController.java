package com.odontologiaintegralfm.feature.appointment.core.controller;


import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationRead;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationUpdate;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayRequestCreateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayRequestUpdateDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IDentistHolidayService;
import com.odontologiaintegralfm.shared.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de la relación de dentista con feriados
 */

@RestController
@RequestMapping("/api/dentist-holiday")
@Validated
public class DentistHolidayController {

    @Autowired
    private IDentistHolidayService dentistHolidayService;


    @Operation(summary = "Crear relación Dentista-Feriado", description = "Permite crear la relación entre un dentista y un feriado feriados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relación creada exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PostMapping("/{idUser}")
    @OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationCreate
    public ResponseEntity<Response<DentistHolidayResponseDTO>> create(@PathVariable("idUser") @NotNull(message = "generic.id.empty") Long id,
                                                                      @Valid @RequestBody DentistHolidayRequestCreateDTO dentistHolidayRequestCreateDTO) {
        Response<DentistHolidayResponseDTO> response = dentistHolidayService.create(id, dentistHolidayRequestCreateDTO);
        return ResponseEntity.ok(response);
    }







    @Operation(summary = "Actualizar relación Dentista-Feriado", description = "Permite actualizar la relación entre un dentista y un feriado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relación actualizada exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PatchMapping("/{idUser}")
    @OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationUpdate
    public ResponseEntity<Response<DentistHolidayResponseDTO>> update(@PathVariable("idUser") @NotNull(message = "generic.id.empty") Long id,
                                                                      @Valid @RequestBody DentistHolidayRequestUpdateDTO dentistHolidayRequestUpdateDTO) {
        Response<DentistHolidayResponseDTO> response = dentistHolidayService.update(dentistHolidayRequestUpdateDTO);
        return ResponseEntity.ok(response);
    }

}
