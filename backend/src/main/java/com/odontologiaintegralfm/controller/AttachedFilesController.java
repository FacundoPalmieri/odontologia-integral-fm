package com.odontologiaintegralfm.controller;

import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAccessPatientsRead;
import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyPatientsUpload;
import com.odontologiaintegralfm.dto.AttachedFileResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.service.interfaces.IAttachedFilesService;
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
import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
@RestController
@RequestMapping("/api/files")
public class AttachedFilesController {

    @Autowired
    private IAttachedFilesService attachedFilesService;


    /**
     * Guarda un documento asociado a la persona.
     * <p>
     * Requiere el rol <b>AdmistratorAndSecretary</b> para acceder.
     * </p>
     *
     * @param id de la persona.
     * @param file documento.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Documento guardado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Roles y/o permisos requeridos no encontrados.</li>
     *         <li><b>409 Conflict</b>: Error en la extensión o tamaño del archivo.</li>
     *         </ul>
     */

    @Operation(summary = "Guardar documento", description = "Guarda un documento asociado a la persona.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento guardado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Roles y/o permisos requeridos no encontrados."),
            @ApiResponse(responseCode = "409", description = "Error en la extensión o tamaño del archivo.")
    })
    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @OnlyPatientsUpload
    public ResponseEntity<Response<String>> saveDocument(@PathVariable Long id,
                                                         @RequestParam("file") MultipartFile file) throws IOException {
        Response<String> response = attachedFilesService.saveDocument(file, id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    /**
     * Obtiene el documento asociado a una persona.
     * <p>
     * Requiere el rol <b>AdmistratorAndSecretary</b> para acceder.
     * </p>
     *
     * @param id de la persona.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Documento recuperado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Roles y/o permisos requeridos no encontrados.</li>
     *         </ul>
     */

    @Operation(summary = "Obtener documento", description = "Obtener recurso por ID documento asociado a una persona.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento recuperado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Persona o documento no encontrados."),
    })
    @GetMapping("/{id}/download")
    @OnlyAccessPatientsRead
    protected ResponseEntity<UrlResource> getByIdDocumentAsResource(Long id) throws IOException {
        UrlResource document = attachedFilesService.getByIdDocumentResource(id);

        //Detecta automáticamente el tipo MIME del archivo
        String contentType = Files.probeContentType(Paths.get(document.getFile().getAbsolutePath()));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType)) // Le indica al cliente el tipo MIME del archivo.
                .body(document);

    }


    /**
     * Obtiene los datos del documento asociado a una persona.
     * <p>
     * Requiere el rol <b>AdmistratorAndSecretary</b> para acceder.
     * </p>
     *
     * @param id de la persona.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Datos del cocumento recuperado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Roles y/o permisos requeridos no encontrados.</li>
     *         </ul>
     */

    @Operation(summary = "Obtener datos de documento", description = "Obtener datos por ID de documento asociado a una persona.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Datos recuperados exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Persona o documento no encontrados."),
    })
    @GetMapping("/{id}/metadata")
    @OnlyAccessPatientsRead
    public ResponseEntity<Response<AttachedFileResponseDTO>> getByIdDocumentMetadata(Long id) throws IOException {
        Response<AttachedFileResponseDTO> response= attachedFilesService.getByIdDocumentMetaData(id);
         return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/all/metadata")
    @OnlyAccessPatientsRead
    public ResponseEntity<Response<List<AttachedFileResponseDTO>>> getAllDocumentMetadata() throws IOException {
        Response<List<AttachedFileResponseDTO>> response = attachedFilesService.getAllDocumentMetaData();
        return new ResponseEntity<>(response, HttpStatus.OK);

    }



}
