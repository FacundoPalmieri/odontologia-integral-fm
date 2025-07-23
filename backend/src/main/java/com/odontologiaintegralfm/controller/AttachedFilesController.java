package com.odontologiaintegralfm.controller;


import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAccessPatientsUpload;
import com.odontologiaintegralfm.configuration.securityConfig.annotations.OnlyAccessUserProfile;
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


@RestController
@RequestMapping("/api/files")
public class AttachedFilesController {

    @Autowired
    private IAttachedFilesService attachedFilesService;


    /**
     * Guarda un documento asociado a un usuario.
     * <p>
     * Requiere "PERMISO_CONFIGURATION_READ o bien se el mismo usuario autenticado" para acceder.
     * </p>
     *
     * @param id de persona.
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

    @Operation(summary = "Guardar documento de usuario", description = "Guarda un documento asociado a un usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento guardado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Roles y/o permisos requeridos no encontrados."),
            @ApiResponse(responseCode = "409", description = "Error en la extensión o tamaño del archivo.")
    })
    @PostMapping(value = "/user/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @OnlyAccessUserProfile
    public ResponseEntity<Response<String>> saveDocumentUser(@PathVariable Long id,
                                                             @RequestParam("file") MultipartFile file) throws IOException {
        Response<String> response = attachedFilesService.saveDocumentUser(file, id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * Guarda un documento asociado a un paciente.
     * <p>
     * Requiere permisos de "PATIENTS UPLOAD" para acceder.
     * </p>
     *
     * @param id del paciente.
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

    @Operation(summary = "Guardar documento de paciente", description = "Guarda un documento asociado al paciente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento guardado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Roles y/o permisos requeridos no encontrados."),
            @ApiResponse(responseCode = "409", description = "Error en la extensión o tamaño del archivo.")
    })
    @PostMapping(value = "/patient/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @OnlyAccessPatientsUpload
    public ResponseEntity<Response<String>> saveDocumentPatient(@PathVariable Long id,
                                                             @RequestParam("file") MultipartFile file) throws IOException {
        Response<String> response = attachedFilesService.saveDocumentPatient(file, id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }




    /**
     * Obtiene el documento asociado a un usuario por ID de documento.
     * La validación de acceso al requerir lógica compleja se realiza en el servicio.
     * <p>
     * Requiere permisos <b>CONFIGURATION_UPLOAD ó bien ser el mismo usuario</b> para acceder.
     * </p>
     *
     * @param documentId del documento.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Documento recuperado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Roles y/o permisos requeridos no encontrados.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener documento de usuario", description = "Obtener recurso de usuario por ID documento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento recuperado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Persona o documento no encontrados."),
    })
    @GetMapping("user/{documentId}/download")
    protected ResponseEntity<UrlResource> getByIdDocumentUserDownload(@PathVariable("documentId") Long documentId) throws IOException {
        UrlResource document = attachedFilesService.getByIdDocumentUserResource(documentId);

        //Detecta automáticamente el tipo MIME del archivo
        String contentType = Files.probeContentType(Paths.get(document.getFile().getAbsolutePath()));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType)) // Le indica al cliente el tipo MIME del archivo.
                .body(document);

    }





    /**
     * Obtiene el documento asociado a un paciente por ID de documento.
     * La validación de acceso al requerir lógica compleja se realiza en el servicio.
     * <p>
     * Requiere permisos <b>PATIENTS_UPLOAD </b> para acceder.
     * </p>
     *
     * @param documentId del documento.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Documento recuperado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Roles y/o permisos requeridos no encontrados.</li>
     *         </ul>
     */
    @Operation(summary = "Obtener documento de paciente", description = "Obtener recurso de paciente por ID documento")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Documento recuperado exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Persona o documento no encontrados."),
    })
    @GetMapping("patient/{documentId}/download")
    @OnlyAccessPatientsUpload
    protected ResponseEntity<UrlResource> getByIdDocumentPatientDownload(@PathVariable("documentId") Long documentId) throws IOException {
        UrlResource document = attachedFilesService.getByIdDocumentPatientResource(documentId);

        //Detecta automáticamente el tipo MIME del archivo
        String contentType = Files.probeContentType(Paths.get(document.getFile().getAbsolutePath()));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType)) // Le indica al cliente el tipo MIME del archivo.
                .body(document);

    }







    /**
     * Obtiene los datos de todos los documento de un usuario.
     * <p>
     * Requiere permisos <b>CONFIGURATION_UPLOAD ó bien ser el mismo usuario</b> para acceder.
     * </p>
     *
     * @param id de la persona.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Datos del documento recuperado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Roles y/o permisos requeridos no encontrados.</li>
     *         </ul>
     */

    @Operation(summary = "Obtener datos de todos los documentos de un usuario", description = "Obtener datos de todos los documentos por ID de usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Datos recuperados exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Persona o documento no encontrados."),
    })
    @GetMapping("user/all/{id}/metadata")
    @OnlyAccessUserProfile
    public ResponseEntity<Response<List<AttachedFileResponseDTO>>> getAllDocumentsMetadataByIdUser(@PathVariable("id") Long id) throws IOException {
        Response<List<AttachedFileResponseDTO>> response= attachedFilesService.getAllDocumentsMetadataByIdUser(id);
         return new ResponseEntity<>(response, HttpStatus.OK);
    }





    /**
     * Obtiene los datos de todos los documento de un paciente.
     * <p>
     * Requiere permisos de "PATIENTS UPLOAD" para acceder.
     * </p>
     *
     * @param id de la persona.
     * @return ResponseEntity con:
     *         <ul>
     *         <li><b>200 OK</b>: Datos del documento recuperado exitosamente.</li>
     *         <li><b>401 Unauthorized</b>: No autenticado.</li>
     *         <li><b>403 Forbidden</b>: No autorizado para acceder a este recurso.</li>
     *         <li><b>404 Not Found</b>: Roles y/o permisos requeridos no encontrados.</li>
     *         </ul>
     */

    @Operation(summary = "Obtener datos de todos los documentos de un paciente", description = "Obtener datos de todos los documentos por ID de paciente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Datos recuperados exitosamente."),
            @ApiResponse(responseCode = "401", description = "No autenticado."),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder a este recurso."),
            @ApiResponse(responseCode = "404", description = "Persona o documento no encontrados."),
    })
    @GetMapping("patient/all/{id}/metadata")
    @OnlyAccessPatientsUpload
    public ResponseEntity<Response<List<AttachedFileResponseDTO>>> getAllDocumentsMetadataByIdPatient(@PathVariable("id") Long id) throws IOException {
        Response<List<AttachedFileResponseDTO>> response= attachedFilesService.getAllDocumentsMetadataByIdPatient(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }













}
