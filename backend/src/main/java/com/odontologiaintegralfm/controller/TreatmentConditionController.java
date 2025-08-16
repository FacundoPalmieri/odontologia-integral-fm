package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAccessConsultationRead;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.model.TreatmentCondition;
import com.odontologiaintegralfm.service.interfaces.ITreatmentConditionService;
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
@RequestMapping("/api/treatment-condition")
public class TreatmentConditionController {

    @Autowired
    private ITreatmentConditionService treatmentConditionService;

    @Value("${pagination.default-page}")
    private int defaultPage;

    @Value("${pagination.default-size}")
    private int defaultSize;

    @Value("${pagination.default-sortBy}")
    private String defaultSortBy;

    @Value("${pagination.default-direction}")
    private String defaultDirection;


    /**
     * Lista todos las condiciones de tratamientos.
     * * <p>Requiere rol <b>Administrator</b> para acceder.</p>
     * @return ResponseEntity con:
     * <ul>
     *     <li><b>200 OK</b> Lista las condiciones de tratamientos recuperadas exitosamente.</li>
     *     <li><b>401 Unauthorized</b>: No autenticado.</li>
     *     <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     * </ul>
     */
    @Operation(summary = "Obtener listado de condiciones de tratamientos", description = "Lista todos las condiciones de tratamientos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Condiciones de tratamientos encontrados."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })

    @GetMapping("/all")
    @OnlyAccessConsultationRead
    public ResponseEntity<Response<Page<TreatmentCondition>>> getAll(@RequestParam (required = false) Integer page,
                                                                     @RequestParam (required = false) Integer size,
                                                                     @RequestParam (required = false) String SortBy,
                                                                     @RequestParam (required = false) String Direction){

        int pageValue = (page != null) ? page : defaultPage;
        int sizeValue = (size != null) ? size : defaultSize;
        String sortByValue = (SortBy != null) ? SortBy : defaultSortBy;
        String directionValue = (Direction != null) ? Direction : defaultDirection;

        Response<Page<TreatmentCondition>> response = treatmentConditionService.getAll(pageValue, sizeValue, sortByValue, directionValue);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
