package com.odontologiaintegralfm.feature.appointment.core.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileOrConfigurationRead;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistAvailabilityResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.WorkingDayDTO;
import com.odontologiaintegralfm.feature.appointment.core.service.impl.DentistAvailabilityService;
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

import java.util.List;

@RestController
@RequestMapping("/api/dentist-availability")
public class DentistAvailabilityController {
    @Autowired
    private DentistAvailabilityService dentistAvailabilityService;


    /**
     * Endpoint para la creación/actualización de disponibilidad de jornada laboral de un dentista.
     * Luego de actualizar se valida que no exista conflictos con turnos asignados.
     * En caso de existir conflictos se listan los mismos.
     *
     * @param days DTO con los datos de disponibilidad.
     */

    @Operation(summary = "Actualizar disponibilidad para dentista", description = "Permite crear o actualizar la disponibilidad diaria de un dentista")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disponibilidad actualizada exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Dentista no encontrado.")
    })
    @PatchMapping("/{id}")
    @OnlyAccessUserProfileOrConfigurationRead
    public ResponseEntity<Response<DentistAvailabilityResponseDTO>> update(@Validated @PathVariable Long id,
                                                                           @Valid @RequestBody List<WorkingDayDTO> days ) {

        Response<DentistAvailabilityResponseDTO> response  =  dentistAvailabilityService.update(id, days);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }




    /**
     * Endpoint para la visualización de disponibilidad de jornada laboral de un dentista.
     * @param id Id del dentista
     */

    @Operation(summary = "Visualizar disponibilidad de dentista", description = "Permite visualizar la disponibilidad de un dentista")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disponibilidad obtenida exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Dentista no encontrado.")
    })
    @GetMapping("/{id}")
    @OnlyAccessUserProfileOrConfigurationRead
    public ResponseEntity<Response<DentistAvailabilityResponseDTO>> get(@PathVariable Long id){
        Response<DentistAvailabilityResponseDTO> response = dentistAvailabilityService.get(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
