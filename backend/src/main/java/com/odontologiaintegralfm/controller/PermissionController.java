package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.dto.PermissionResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.interfaces.IPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controlador encargado de gestionar los permisos en el sistema. Proporciona operaciones para obtener
 * el listado de permisos y un permiso específico por su ID.
 * <p>
 * Este controlador está restringido por el rol <b>DEV</b>, y todos los accesos están bloqueados por defecto
 * debido a la configuración de seguridad.
 * </p>
 * <p>
 * Los métodos disponibles son:
 * <ul>
 *   <li><b>GET /api/permissions/all</b>: Obtiene el listado completo de permisos.</li>
 *   <li><b>GET /api/permissions/{id}</b>: Obtiene un permiso específico por su ID.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Roles necesarios:</b> <b>DEV</b>
 * </p>
 */
@RestController
@PreAuthorize("denyAll()")
@RequestMapping("/api/permission")
public class PermissionController {

    @Autowired
    private IPermissionService permissionService;

    /**
     * Lista todos los permisos disponibles en el sistema.
     * <p>
     * Requiere el rol <b>DEV</b> para acceder.
     * </p>
     *
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Listado de permisos recuperado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener listado de Permisos", description = "Lista todos los permisos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado recuperado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("all")
    @PreAuthorize("hasAnyRole(@userRolesConfig.desarrolladorRole)")
    public ResponseEntity<Response<List<PermissionResponseDTO>>> getAllPermissions() {
        Response<List<PermissionResponseDTO>> response = permissionService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Obtiene un permiso por su ID.
     * <p>
     * Requiere el rol <b>DEV</b> para acceder.
     * </p>
     *
     * @param id ID del permiso a buscar.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Permiso encontrado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Permiso no encontrado.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener Permiso", description = "Obtiene un permiso por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permiso encontrado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Permiso no encontrado.")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole(@userRolesConfig.desarrolladorRole)")
    public ResponseEntity<Response<PermissionResponseDTO>> getPermissionById(@PathVariable Long id) {
        Response<PermissionResponseDTO> response = permissionService.getById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}