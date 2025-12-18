package com.odontologiaintegralfm.feature.dentist.core.controller;


import com.odontologiaintegralfm.feature.dentist.core.dto.DentistResponseDTO;
import com.odontologiaintegralfm.feature.dentist.core.service.interfaces.IDentistService;
import com.odontologiaintegralfm.shared.dto.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Set;

@RestController
@RequestMapping("/api/dentist")
public class DentistController {
    private final IDentistService dentistService;

    public DentistController(IDentistService dentistService) {
        this.dentistService = dentistService;
    }


    @Operation(summary = "Obtener dentistas", description = "Obtiene un set de dentistas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dentistas obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @GetMapping("/all")
    public ResponseEntity<Response<Set<DentistResponseDTO>>> getAllDentists() {
        Response<Set<DentistResponseDTO>> response = dentistService.getAll();
        return ResponseEntity.ok(response);
    }
}
