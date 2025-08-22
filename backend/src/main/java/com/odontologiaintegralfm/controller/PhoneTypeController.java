package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAccessConfigurationOrPatientsRead;
import com.odontologiaintegralfm.dto.PhoneTypeResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.interfaces.IPhoneTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/phone-type")
public class PhoneTypeController {

    @Autowired
    private IPhoneTypeService phoneTypeService;


    /**
     * Lista todos los tipos de teléfonos "habilitados".
     * <p>Requiere rol <b>AdmistratorAndSecretary</b> para acceder.</p>
     * @return ResponseEntity con:
     * <ul>
     *     <li><b>200 OK</b> Lista de tipo teléfonos recuperados exitosamente.</li>
     *     <li><b>401 Unauthorized</b>: No autenticado.</li>
     *     <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     * </ul>
     */
    @Operation(summary = "Obtener listado de tipos de teléfono", description = "Lista todos los tipos de teléfono.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipos teléfono recuperados exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    @OnlyAccessConfigurationOrPatientsRead
    public ResponseEntity<Response<Set<PhoneTypeResponseDTO>>> getAll() {
        Response<Set<PhoneTypeResponseDTO>>response = phoneTypeService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
