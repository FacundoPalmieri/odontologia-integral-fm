package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessAppointmentsManagementCreate;
import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationCreateResponseDTO;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controlador que representa la gestión de consultas.
 */



@RestController
@RequestMapping("/api/consultation")
@Validated
public class ConsultationController {

    @Autowired
    private IConsultationService consultationService;

    /**
     * Endpoint que permite crear una nueva consulta.
     * <p>Requiere permiso de <b>Consulta creación</b> para acceder.</p>
     * @param idAppointment
     * @return ResponseEntity con:
     * <ul>
     *     <li><b>200 OK</b> Consulta creada exitosamente.</li>
     *     <li><b>401 Unauthorized</b>: No autenticado.</li>
     *     <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     * </ul>
     */
    @Operation(summary = "Crear consulta", description = "Crea una consulta.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta creada"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @PostMapping("/{idAppointment}")
    @OnlyAccessAppointmentsManagementCreate
    private ResponseEntity<Response<ConsultationCreateResponseDTO>> create (@NotNull Long idAppointment){
        Response<ConsultationCreateResponseDTO> response = consultationService.create(idAppointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
