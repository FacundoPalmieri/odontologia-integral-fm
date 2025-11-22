package com.odontologiaintegralfm.feature.appointment.core.controller;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessAppointmentsManagementCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationRead;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentCreateResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentConflictService;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentService;
import com.odontologiaintegralfm.shared.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *  Controlador de turnos
 */

@RestController
@RequestMapping("api/appointment")
public class AppointmentController {

    @Autowired
    private IAppointmentService appointmentService;

    @Autowired
    private IAppointmentConflictService appointmentConflictService;

    @Operation(summary = "Listar turnos en conflicto por dentista", description = "Lista los turnos en conflictos por Id de dentista")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/conflict/all/{idDentist}")
    @OnlyAccessUserProfileAndAppointmentsManagementOrConfigurationRead
    public ResponseEntity<Response<List<AppointmentConflictResponseDTO>>> getConflictAll(@PathVariable Long idDentist){
        Response<List<AppointmentConflictResponseDTO>> response = appointmentConflictService.getConflict(idDentist);
        return ResponseEntity.ok(response);
    }


    @PostMapping()
    @OnlyAccessAppointmentsManagementCreate
    public ResponseEntity<Response<AppointmentCreateResponseDTO>> create(@RequestBody @Valid AppointmentCreateRequestDTO appointmentCreateRequestDTO){

        Response<AppointmentCreateResponseDTO> response = appointmentService.create(appointmentCreateRequestDTO);

        return ResponseEntity.ok(response);
    }
}
