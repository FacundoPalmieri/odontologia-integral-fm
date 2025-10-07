package com.odontologiaintegralfm.feature.appointment.core.controller;


import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileOrConfigurationCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileOrConfigurationRead;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistHolidayRequestDTO;
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


    @Operation(summary = "Actualizar relación Dentista-Feriado", description = "Permite crear o actualizar la relación entre un dentista y una lista de feriados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relaciones actualizadas exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PostMapping("/{idDentist}")
    @OnlyAccessUserProfileOrConfigurationCreate
    public ResponseEntity<Response<DentistHolidayResponseDTO>> update(@PathVariable("idDentist") @NotNull(message = "generic.id.empty") Long id,
                                                                      @Valid @RequestBody DentistHolidayRequestDTO dentistHolidayRequestDTO) {
        Response<DentistHolidayResponseDTO> response = dentistHolidayService.update(id,dentistHolidayRequestDTO);
        return ResponseEntity.ok(response);
    }




    @Operation(summary = "Recuperar relación Dentista-Feriado", description = "Permite obtener un listado de relaciones entre un dentista y una lista de feriados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relaciones listadas exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("{idDentist}")
    @OnlyAccessUserProfileOrConfigurationRead
    public ResponseEntity<Response<DentistHolidayResponseDTO>> get(@PathVariable("idDentist") @NotNull(message = "generic.id.empty") Long id,
                                                                   @RequestParam @NotNull(message = "dentistHolidayRequestDTO.year.empty") Integer year){


        Response<DentistHolidayResponseDTO> response = dentistHolidayService.get(id,year);
        return ResponseEntity.ok(response);
    }
}
