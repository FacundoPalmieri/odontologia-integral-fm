package com.odontologiaintegralfm.feature.appointment.catalogs.controller;

import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConfigurationCreate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessConfigurationUpdate;
import com.odontologiaintegralfm.configuration.securityconfig.annotations.OnlyAccessUserProfileOrConfigurationRead;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.HolidayCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.HolidayResponseDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.dto.HolidayUpdateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.catalogs.service.HolidayService;
import com.odontologiaintegralfm.shared.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author [Facundo Palmieri]
 */

@RestController
@RequestMapping("/api/holiday")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    @Value("${pagination.default-page}")
    private int defaultPage;

    @Value("${pagination.default-size}")
    private int defaultSize;

    @Value("${pagination.default-direction}")
    private String defaultDirection;

    @Value("${pagination.default.holiday-sortBy}")
    private String defaultHolidaySortBy;


    /**
     * Obtiene todos los feriados por año.
     * @param year      : Año a buscar
     * @param page      : N° página
     * @param size      : Tamaño de registros por página.
     * @param sortBy    : Columna de ordenamiento
     * @param direction :Dirección de ordenamiento.
     */
    @Operation(summary = "Obtiene todos los feriados por año", description = "Obtiene la lista de feriados por año")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feriados obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all/")
    @OnlyAccessUserProfileOrConfigurationRead
    public ResponseEntity<Response<Page<HolidayResponseDTO>>> getAll(@RequestParam int year,
                                                                     @RequestParam(required = false) Integer page,
                                                                     @RequestParam(required = false) Integer size,
                                                                     @RequestParam(required = false) String sortBy,
                                                                     @RequestParam(required = false) String direction
                                                                             ) {

        int pageValue = (page == null) ? defaultPage : page;
        int sizeValue = (size == null) ? defaultSize : size;
        String sortByValue = (sortBy == null) ? defaultHolidaySortBy : sortBy;
        String directionValue = (direction == null) ? defaultDirection  : direction;

        Response<Page<HolidayResponseDTO>> response = holidayService.getAll(year, pageValue, sizeValue, sortByValue, directionValue);
        return ResponseEntity.ok(response);

    }





    /**
     * Crea un nuevo feriado.
     * <p>
     * Requiere permisos <b>Configuración Creación</b> para acceder.
     * </p>
     *
     * @param holidayCreateRequestDTO ID del usuario a buscar.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Feriado creado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Agregar un nuevo feriado", description = "Agregar un nuevo feriado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feriado cargado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
    })
    @PostMapping()
    @OnlyAccessConfigurationCreate
    public ResponseEntity<Response<HolidayResponseDTO>> create(@RequestBody HolidayCreateRequestDTO holidayCreateRequestDTO) {
        Response<HolidayResponseDTO> response = holidayService.create(holidayCreateRequestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }




    /**
     * Actualiza un feriado.
     * <p>
     * Requiere permisos <b>Configuración Actualización</b> para acceder.
     * </p>
     * @param holidayUpdateRequestDTO
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Feriado actualizado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Actualizar datos de un feriado", description = "Actualizar fecha, tipo o nombre de un feriado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Feriado actualizado correctamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Feriado no encontrado")
    })
    @PatchMapping()
    @OnlyAccessConfigurationUpdate
    public ResponseEntity<Response<HolidayResponseDTO>>update(@RequestBody @Valid HolidayUpdateRequestDTO holidayUpdateRequestDTO) {
      Response<HolidayResponseDTO> response  =  holidayService.update(holidayUpdateRequestDTO);
      return ResponseEntity.ok(response);
    }









}
