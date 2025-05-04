package com.odontologiaintegralfm.controller;
import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyDevelopers;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.dto.RoleRequestDTO;
import com.odontologiaintegralfm.dto.RoleResponseDTO;
import com.odontologiaintegralfm.model.Role;
import com.odontologiaintegralfm.service.interfaces.IPermissionService;
import com.odontologiaintegralfm.service.interfaces.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * Controlador encargado de gestionar los roles en el sistema. Proporciona operaciones para obtener
 * el listado de roles, obtener un rol específico por su ID y crear nuevos roles.
 * <p>
 * Este controlador está restringido por el rol <b>DEV</b>, y todos los accesos están bloqueados por defecto
 * debido a la configuración de seguridad.
 * </p>
 * <p>
 * Los métodos disponibles son:
 * <ul>
 *   <li><b>GET /api/role/all</b>: Obtiene el listado completo de roles.</li>
 *   <li><b>GET /api/role/{id}</b>: Obtiene un rol específico por su ID.</li>
 *   <li><b>POST/api/roles</b>: Crea un nuevo rol en el sistema.</li>
 *   <li><b>PATCH/api/roles</b>: Actualiza un rol en el sistema..</li>
 * </ul>
 * </p>
 * <p>
 * <b>Roles necesarios:</b> <b>DEV</b>
 * </p>
 */

@RestController
@PreAuthorize("denyAll()")
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IPermissionService permiService;


    /**
     * Lista todos los roles disponibles en el sistema.
     * <p>
     * Requiere el rol <b>DEV</b> para acceder.
     * </p>
     *
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Listado de roles recuperado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         </ul>
     */

    @Operation(summary = "Obtener listado de roles", description = "Lista todos los roles.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado recuperado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    @OnlyDevelopers
    public ResponseEntity<Response<List<Role>>> getAllRoles() {
        Response<List<Role>> response = roleService.getAll();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Obtiene un rol por su ID.
     * <p>
     * Requiere el rol <b>DEV</b> para acceder.
     * </p>
     *
     * @param id ID del rol a buscar.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Rol encontrado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Rol no encontrado.</li>
     *         </ul>
     */

    @Operation(summary = "Obtener Rol", description = "Obtiene un Rol por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol encontrado."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado."),
    })
    @GetMapping("/{id}")
    @OnlyDevelopers
    public ResponseEntity<Response<RoleResponseDTO>> getRoleById(@Valid @PathVariable Long id) {
        Response<RoleResponseDTO> response = roleService.getById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Crea un nuevo rol en el sistema.
     * <p>
     * Requiere el rol <b>DEV</b> para acceder.
     * </p>
     *
     * @param roleRequestDto Datos del rol a crear.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Rol creado exitosamente.</li>
     *         <li><b>400 Bad Request</b>: No se encuentran los permisos asociados.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>409 Conflict</b>: Rol existente en el sistema.</li>
     *         </ul>
     */

    @Operation(summary = "Crear Rol", description = "Crea un nuevo rol en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "rol Creado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Permisos requeridos no encontrados."),
            @ApiResponse(responseCode = "409", description = "Rol existente en el sistema.")
    })
    @PostMapping
    @OnlyDevelopers
    public ResponseEntity<Response<RoleResponseDTO>>createRole(@Valid @RequestBody RoleRequestDTO roleRequestDto) {
        Response<RoleResponseDTO> response = roleService.save(roleRequestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }




    /**
     * Actualiza un rol en el sistema.
     * <p>
     * Requiere el rol <b>DEV</b> para acceder.
     * </p>
     *
     * @param roleRequestDto Datos del rol a crear.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Rol actualizado exitosamente.</li>
     *         <li><b>400 Bad Request</b>: No se encuentran los permisos asociados.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>409 Conflict</b>: Rol existente en el sistema.</li>
     *         </ul>
     */

    @Operation(summary = "Actualizar Rol", description = "Actualiza un nuevo rol en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "rol actualizado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Permisos requeridos no encontrados."),
            @ApiResponse(responseCode = "409", description = "Rol existente en el sistema.")
    })
    @PatchMapping
    @OnlyDevelopers
    public ResponseEntity<Response<RoleResponseDTO>> updateRole(@Valid @RequestBody RoleRequestDTO roleRequestDto) {
        Response<RoleResponseDTO> response = roleService.update(roleRequestDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
