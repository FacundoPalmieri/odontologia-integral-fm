package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAccessConsultationRecordRead;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.dto.TreatmentResponseDTO;
import com.odontologiaintegralfm.service.interfaces.ITreatmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/treatment")
public class TreatmentController {

    @Autowired
    private ITreatmentService treatmentService;

    @Value("${pagination.default-page}")
    private int defaultPage;

    @Value("${pagination.default-size}")
    private int defaultSize;

    @Value("${pagination.default-sortBy}")
    private String defaultTreatmenSortBy;

    @Value("${pagination.default-direction}")
    private String defaultDirection;




    /**
     * Lista todos los tratamientos "habilitadas".
     * <p>Requiere rol <b>Odontólogo</b> para acceder.</p>
     * @return ResponseEntity con:
     * <ul>
     *     <li><b>200 OK</b> Lista los tratamientos recuperadas exitosamente.</li>
     *     <li><b>401 Unauthorized</b>: No autenticado.</li>
     *     <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     * </ul>
     */
    @Operation(summary = "Obtener listado de tratamientos", description = "Lista todos los tratamientos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tratamientos encontrados."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    @OnlyAccessConsultationRecordRead
    public ResponseEntity<Response<Page<TreatmentResponseDTO>>> getAll(@RequestParam (required = false) Integer page,
                                                                      @RequestParam (required = false) Integer size,
                                                                      @RequestParam (required = false) String sortBy,
                                                                      @RequestParam (required = false) String direction) {

        int pageValue = (page != null)? page : defaultPage;
        int sizeValue = (size != null)? size : defaultSize;
        String sortByValue = (sortBy != null)? sortBy : defaultTreatmenSortBy;
        String directionValue = (direction != null)? direction : defaultDirection;

        Response<Page<TreatmentResponseDTO>> response = treatmentService.getAll(pageValue,sizeValue, sortByValue, directionValue);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
