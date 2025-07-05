package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAdmistratorAndSecretary;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.interfaces.IPersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author [Facundo Palmieri]
 */
@RestController
@OnlyAdmistratorAndSecretary
@RequestMapping("/api/person")
public class PersonController {

    @Autowired
    private IPersonService personService;


    /**
     * Actualiza la imágen de perfil de la persona.
     * <p>
     * Requiere el rol <b>AdmistratorAndSecretary</b> para acceder.
     * </p>
     *
     * @param id de la persona.
     * @param file imágen.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Imágen actualizada exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Roles y/o permisos requeridos no encontrados.</li>
     *         <li><b>409 Conflict</b>: Error en la extensión o tamaño del archivo.</li>
     *         </ul>
     */

    @Operation(summary = "Actualizar Imágen", description = "Actualiza la imágen de la persona.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imágen actualizada exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Roles y/o permisos requeridos no encontrados."),
            @ApiResponse(responseCode = "409", description = "Error en la extensión o tamaño del archivo.")
    })
    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<String>> uploadAvatar(@PathVariable Long id,
                                                         @RequestParam("file") MultipartFile file) throws IOException {
        Response<String> response = personService.saveAvatar(file, id);
        return new ResponseEntity<>(response, HttpStatus.OK );
    }





    /**
     * Obtiene la imágen de perfil de la persona.
     * <p>
     * Requiere el rol <b>AdmistratorAndSecretary</b> para acceder.
     * </p>
     *
     * @param id de la persona.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Imágen recuperada exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Roles y/o permisos requeridos no encontrados.</li>
     *         </ul>
     */

    @Operation(summary = "Obtener Imágen", description = "Obtener la imágen de la persona.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imágen recuperada exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Persona o imagen no encontrada."),
    })
    @GetMapping("/{id}/avatar")
    public ResponseEntity<UrlResource> getAvatar(@PathVariable Long id) throws IOException {

        UrlResource avatar = personService.getAvatar(id);

        //Detecta automáticamente el tipo MIME del archivo
        String contentType = Files.probeContentType(Paths.get(avatar.getFile().getAbsolutePath()));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType)) // Le indica al cliente el tipo MIME del archivo.
                .body(avatar);
    }



    /**
     * Elimina la imágen de perfil de la persona.
     * <p>
     * Requiere el rol <b>AdmistratorAndSecretary</b> para acceder.
     * </p>
     *
     * @param id de la persona.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Imágen Eliminada exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Roles y/o permisos requeridos no encontrados.</li>
     *         </ul>
     */

    @Operation(summary = "Eliminar Imágen", description = "Elimina la imágen de la persona.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imágen Eliminada exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Persona o imagen no encontrada."),
    })
    @DeleteMapping("/{id}/avatar")
    public ResponseEntity<Response<String>> deleteAvatar(@PathVariable Long id) throws IOException {
        Response<String> response = personService.deleteAvatar(id);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }

}
