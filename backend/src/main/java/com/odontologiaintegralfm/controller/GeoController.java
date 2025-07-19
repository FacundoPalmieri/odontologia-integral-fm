package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAccessConfigurationOrPatientsRead;
import com.odontologiaintegralfm.dto.CountryResponseDTO;
import com.odontologiaintegralfm.dto.LocalityResponseDTO;
import com.odontologiaintegralfm.dto.ProvinceResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.GeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/geo")
public class GeoController {
    @Autowired
    private GeoService geoService;



    /**
     * Obtiene la lista de Países habilitados de la aplicación.
     * <p>
     * Requiere rol <b>administrador y Secretaría</b> para acceder.
     * </p>
     *
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Listado de países recuperado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener listado de países", description = "Obtiene todos los países.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de países recuperados exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping ("/countries/all")
    @OnlyAccessConfigurationOrPatientsRead
    public ResponseEntity<Response<List<CountryResponseDTO>>> getAllCountries() {
      Response<List<CountryResponseDTO>>response = geoService.getAllCountries();
      return new ResponseEntity<>(response, HttpStatus.OK);
    }







    /**
     * Obtiene la lista de Provincias habilitadas de acuerdo al ID de País recibido.
     * <p>
     * Requiere rol <b>administrador y Secretaría</b> para acceder.
     * </p>
     *
     * @param idCountry ID del país.
     *
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Listado de provincias recuperadas exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener listado de Provincias por ID de País", description = "Obtiene todos las Provincias por ID de País.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de provincias recuperadas exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/provinces/{id}")
    @OnlyAccessConfigurationOrPatientsRead
    public ResponseEntity<Response<List<ProvinceResponseDTO>>> getProvincesByIdCountry(@NotNull @PathVariable("id") Long idCountry) {
        Response<List<ProvinceResponseDTO>>response = geoService.getProvincesByIdCountry(idCountry);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Obtiene la lista de Localidades habilitadas de acuerdo al ID de la provincia recibida.
     * <p>
     * Requiere rol <b>administrador y Secretaría</b> para acceder.
     * </p>
     * @param idProvinces ID de la provincia
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Listado de localidades recuperadas exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener listado de Localidades por ID de Provincia", description = "Obtiene todos las Localidades por ID de Provincia.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado de localidades recuperadas exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/localities/{id}")
    @OnlyAccessConfigurationOrPatientsRead
    public ResponseEntity<Response<List<LocalityResponseDTO>>> getLocalitiesByIdProvinces(@NotNull @PathVariable("id") Long idProvinces) {
        Response<List<LocalityResponseDTO>>response = geoService.getLocalitiesByIdProvinces(idProvinces);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}
