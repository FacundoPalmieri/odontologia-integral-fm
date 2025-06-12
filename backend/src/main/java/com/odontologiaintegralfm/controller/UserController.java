package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyDeveloperAndAdministrator;
import com.odontologiaintegralfm.dto.*;
import com.odontologiaintegralfm.service.interfaces.IUserService;
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
 * Controlador encargado de gestionar los usuarios en el sistema. Proporciona operaciones para obtener
 * el listado de usuarios, obtener un usuario específico por su ID y crear nuevos usuarios.
 * <p>
 * Este controlador requiere el rol <b>ADMIN</b> para acceder a sus métodos.
 * </p>
 * <p>
 * Los métodos disponibles son:
 * <ul>
 *   <li><b>GET /api/user/all</b>: Obtiene el listado completo de usuarios.</li>
 *   <li><b>GET /api/user/{id}</b>: Obtiene un usuario específico por su ID.</li>
 *   <li><b>POST /api/user/</b>: Crea un nuevo usuario en el sistema.</li>
 *   <li><b>PATCH /api/user/</b>: Actualiza un usuario en el sistema.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Roles necesarios:</b> <b>ADMIN</b>
 * </p>
 */
@RestController
@RequestMapping("/api/user")
@OnlyDeveloperAndAdministrator
public class UserController {

    @Autowired
    private IUserService userService;

    @Value("${pagination.default-page}")
    private int defaultPage;

    @Value("${pagination.default-size}")
    private int defaultSize;


    /**
     * Lista todos los usuarios.
     * <p>Requiere rol <b>ADMIN</b> para acceder.</p>
     * @return ResponseEntity con:
     * <ul>
     *     <li><b>200 OK</b> Lista de usuarios recuperada exitosamente.</li>
     *     <li><b>401 Unauthorized</b>: No autenticado.</li>
     *     <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     * </ul>
     */
    @Operation(summary = "Obtener listado de usuarios", description = "Lista todos los usuarios.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuarios Encontrados."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
    })
    @GetMapping("/all")
    public ResponseEntity<Response<Page<UserSecResponseDTO>>> getAll(@RequestParam(required = false)  Integer page,
                                                                     @RequestParam(required = false)  Integer size
                                                                    ){
        int pageValue = (page != null) ? page : defaultPage;
        int sizeValue = (size != null) ? size : defaultSize;

        Response<Page<UserSecResponseDTO>> response = userService.getAll(pageValue, sizeValue);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    /**
     /**
     * Obtiene un usuario por su ID.
     * <p>
     * Requiere el rol <b>ADMIN</b> para acceder.
     * </p>
     *
     * @param id ID del usuario a buscar.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Usuario encontrado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Usuario no encontrado.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener usuario", description = "Obtiene un usuario por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Response<UserSecResponseDTO>> getById(@PathVariable Long id) {
        Response<UserSecResponseDTO>response = userService.getById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }




    /**
     * Crea un nuevo usuario en el sistema.
     * <p>
     * Requiere el rol <b>ADMIN</b> para acceder.
     * </p>
     *
     * @param userSecCreateDto Datos del usuario a crear.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>201 Created</b>: Usuario creado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Roles y/o permisos requeridos no encontrados.</li>
     *         <li><b>409 Conflict</b>: Usuario existente en el sistema.</li>
     *         </ul>
     */

    @Operation(summary = "Crear usuario", description = "Crea un nuevo usuario en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario Creado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Roles y/o permisos requeridos no encontrados."),
            @ApiResponse(responseCode = "409", description = "Usuario existente en el sistema.")
    })
    @PostMapping
    public  ResponseEntity<Response<UserSecResponseDTO>> create(@Valid @RequestBody UserSecCreateDTO userSecCreateDto) {
        Response<UserSecResponseDTO>response = userService.create(userSecCreateDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }




    /**
     * Actualiza la información de un usuario.
     * Este endpoint permite a los usuarios con rol de "ADMIN" actualizar los datos de un usuario existente.
     * @param userSecUpdateDto Objeto que contiene los datos del usuario a actualizar. Debe ser válido según las
     * restricciones de la clase {@link UserSecUpdateDTO}.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Usuario actualizado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Roles y/o permisos requeridos no encontrados.</li>
     *         <li><b>409 Conflict</b>: Usuario existente en el sistema o se intenta actualizar a un rol DEV.</li>
     *         </ul>
     */
    @Operation(summary = "Actualizar usuario", description = "Actualizar un usuario en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario Actualizado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Roles y/o permisos requeridos no encontrados."),
            @ApiResponse(responseCode = "409", description = "Usuario existente en el sistema o se intenta actualizar a un rol DEV.")
    })
    @PatchMapping
    public ResponseEntity<Response<UserSecResponseDTO>> updateUser(@Valid @RequestBody UserSecUpdateDTO userSecUpdateDto) {
       Response<UserSecResponseDTO> response =  userService.update(userSecUpdateDto);
       return new ResponseEntity<>(response, HttpStatus.OK);

    }

}