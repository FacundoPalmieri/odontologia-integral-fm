package com.odontologiaintegralfm.service;

import com.mysql.cj.log.Log;
import com.odontologiaintegralfm.configuration.appConfig.annotations.LogAction;
import com.odontologiaintegralfm.configuration.securityConfig.AuthenticatedUserService;
import com.odontologiaintegralfm.dto.AttachedFileResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.dto.internal.SchedulerResultDTO;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.enums.LogType;
import com.odontologiaintegralfm.enums.SystemParameterKey;
import com.odontologiaintegralfm.exception.*;
import com.odontologiaintegralfm.model.AttachedFile;
import com.odontologiaintegralfm.model.Patient;
import com.odontologiaintegralfm.model.Person;
import com.odontologiaintegralfm.model.UserSec;
import com.odontologiaintegralfm.repository.IAttachedFilesRepository;
import com.odontologiaintegralfm.service.interfaces.IAttachedFilesService;
import com.odontologiaintegralfm.service.interfaces.IFileStorageService;
import com.odontologiaintegralfm.service.interfaces.IPersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


/**
 * Servicio encargado de gestionar los archivos adjuntos (documentos PDF) asociados a personas.
 * Incluye operaciones de almacenamiento, recuperación y obtención de metadatos,
 * pero no accede directamente al sistema de archivos.
 *
 * Todo lo relacionado con los archivos físicos está encapsulado en {@link FileStorageService}
 * Esa clase es responsable de determinar la ubicación de los archivos, validar su existencia y manejar errores de acceso.
 */



@Service
@Slf4j
public class AttachedFilesService implements IAttachedFilesService {

    @Autowired
    private SystemParameterService systemParameterService;

    @Autowired
    private IFileStorageService fileStorageService;

    @Autowired
    private IPersonService personService;

    @Autowired
    private IAttachedFilesRepository attachedFilesRepository;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private PatientService patientService;



    /**
     * Método para guardar un documento en formato PDF asociado a un Usuario.
     *
     * @param file
     * @param id
     * @return
     * @throws IOException
     */
    @Override
    @Transactional
    @LogAction(
            value = "attachedFilesService.systemLogService.saveDocumentUser",
            args = {"#id, #filename"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<String> saveDocumentUser(MultipartFile file, Long id) throws IOException {

        //Verifica si existe el archivo.
        if(file.isEmpty()){
            return null;
        }

        //Verifica extensión del documento.
        extensionVerificationDocument(file.getOriginalFilename());

        //Verifica tamaño del documento
        sizeVerification(file);

        //Valída que Id recibido corresponda a un usuario.
        UserSec userSec = userService.getByIdInternal(id);

        if(userSec.getPerson() != null){
            //Obtiene la persona.
            Person person = personService.getById(userSec.getPerson().getId());


            // Sanitiza el nombre original Ej: "mi archivo (versión 2).pdf" → "mi_archivo__versi_n_2_.pdf"
            String originalFilename = Paths.get(file.getOriginalFilename()).getFileName().toString();
            String sanitizedOriginalName = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

            // Crea nombre único
            String filename = person.getId() + "-" + person.getLastName()+person.getFirstName() + "-" + sanitizedOriginalName;

            //Guarda documento físico.
            fileStorageService.saveDocument(file, filename);

            //Persiste metadatos en la base.
            AttachedFile attachedFile = new AttachedFile();
            attachedFile.setFileName(file.getOriginalFilename());
            attachedFile.setStoredFileName(filename);
            attachedFile.setFileType(file.getContentType());
            attachedFile.setPerson(person);
            attachedFile.setCreatedAt(LocalDateTime.now());
            attachedFile.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
            attachedFile.setEnabled(true);

            attachedFile = attachedFilesRepository.save(attachedFile);


            String userMessage = messageService.getMessage("attachedFileService.saveDocument.ok.user", null, LocaleContextHolder.getLocale());
            return new Response<>(true, userMessage, attachedFile.getStoredFileName());

        }else{
            throw new ConflictException("exception.update.validateNotDevRole.user",null,"exception.update.validateNotDevRole.log",new Object[]{id,"AttachedFileService", "saveDocumentUser"},LogLevel.INFO);
        }


    }






    /**
     * Método para guardar un documento en formato PDF asociado a un Paciente.
     *
     * @param file
     * @param id
     * @return
     * @throws IOException
     */
    @Override
    @Transactional
    @LogAction(
            value = "attachedFilesService.systemLogService.saveDocumentPatient",
            args = {"#id, #filename"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO
    )
    public Response<String> saveDocumentPatient(MultipartFile file, Long id) throws IOException {

        //Verifica si existe el archivo.
        if(file.isEmpty()){
            return null;
        }

        //Verifica extensión del documento.
        extensionVerificationDocument(file.getOriginalFilename());

        //Verifica tamaño del documento
        sizeVerification(file);

        //Valída que Id recibido corresponda a un paciente.
        Patient patient = patientService.getByIdInternal(id);

        //Obtiene la persona.
        Person person = personService.getById(patient.getId());

        // Sanitiza el nombre original
        String originalFilename = Paths.get(file.getOriginalFilename()).getFileName().toString();
        String sanitizedOriginalName = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

        // Crea nombre único
        String filename = person.getId() + "-" + person.getLastName()+person.getFirstName() + "-" + sanitizedOriginalName;


        //Guarda documento físico.
        fileStorageService.saveDocument(file, filename);

        AttachedFile attachedFile = new AttachedFile();
        attachedFile.setFileName(file.getOriginalFilename());
        attachedFile.setStoredFileName(filename);
        attachedFile.setFileType(file.getContentType());
        attachedFile.setPerson(person);
        attachedFile.setCreatedAt(LocalDateTime.now());
        attachedFile.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        attachedFile.setEnabled(true);

        attachedFile = attachedFilesRepository.save(attachedFile);

        String userMessage = messageService.getMessage("attachedFileService.saveDocument.ok.user", null, LocaleContextHolder.getLocale());
        return new Response<>(true, userMessage, attachedFile.getStoredFileName());
    }





    /**
     * Método para obtener de un usuario un documento PDF por su ID.
     * Primero se recupera la metadata mediante y luego se recupera el archivo físico mediante el servicio de fileStorage.

     * @param documentId Id del documento
     * @return
     * @throws IOException
     */
    @Override
    @Transactional(readOnly = true)
    public UrlResource getByIdDocumentUserResource(Long documentId) throws IOException {
        try{

            //Recupera la metadata del archivo.
            AttachedFile file = attachedFilesRepository.findByIdAndEnabledTrue(documentId)
                    .orElseThrow(() -> new NotFoundException("exception.file.attachedFileNotFound.user", null, "exception.file.attachedFileNotFound.log", new Object[]{documentId, "AttachedFilesService", "getByIdDocumentResource"}, LogLevel.ERROR));


            //Control de acceso a la descarga de archivo.
            UserSec userSec =  userService.getByIdInternal(authenticatedUserService.getAuthenticatedUser().getId());
            boolean havePermission = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(auth -> auth.equals("PERMISO_CONFIGURATION_UPLOAD"));

            //Control de acceso para Desarrolladores que no tiene ID de persona.
            if(userSec.getId() == 1 || userSec.getId() == 2){
                //Recupera el archivo físico y retorna
                return fileStorageService.getDocument(file);
            }

            // Control de acceso para users.
            if(Objects.equals(userSec.getPerson().getId(), file.getPerson().getId())){
                //Recupera el archivo físico y retorna
                return fileStorageService.getDocument(file);

            }else if(havePermission) {
                //Recupera el archivo físico y retorna
                return fileStorageService.getDocument(file);
            }else{
                throw new ForbiddenException("exception.accessDenied.user", null, "exception.accessDenied.log", new Object[]{userSec.getPerson().getId(),"user/{documentId}/download", "AttachedFilesService", "getByIdDocumentResource"},LogLevel.ERROR);
            }

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AttachedFileService", documentId,"<-  Id del documento", "getByIdDocumentResource");
        }
    }






    /**
     * Método para obtener de un paciente un documento PDF por su ID.
     *
     * @param documentId Id del documento
     * @return
     * @throws IOException
     */
    @Override
    @Transactional(readOnly = true)
    public UrlResource getByIdDocumentPatientResource(Long documentId) throws IOException {
        try{

            //Recupera la metadata del archivo.
            AttachedFile file = attachedFilesRepository.findByIdAndEnabledTrue(documentId)
                    .orElseThrow(() -> new NotFoundException("exception.file.attachedFileNotFound.user", null, "exception.file.attachedFilesNotFound.log", new Object[]{documentId, "AttachedFilesService", "getByIdDocumentResource"}, LogLevel.ERROR));

            //Control de acceso.
            //Se busca si existe el ID Person en el archivo, si no existe no es paciente.
            if(file.getPerson() != null){
                // Validación: verifica que la persona asociada sea un paciente
                patientService.getByIdInternal(file.getPerson().getId());

                //Recupera el archivo físico y retorna
                return fileStorageService.getDocument(file);

            }else{
                throw new ForbiddenException("exception.accessDenied.user", null, "exception.accessDenied.log", new Object[]{authenticatedUserService.getAuthenticatedUser().getId(),"user/{documentId}/download", "AttachedFilesService", "getByIdDocumentResource"},LogLevel.ERROR);
            }
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AttachedFileService", documentId,"<-  Id del documento", "getByIdDocumentResource");
        }
    }


    /**
     * Método para obtener la metadata de todos los documento PDF de un usuario.
     * @param idUser idUser
     * @return Objeto Response, con la lista de la información de todos los documentos.
     * @throws IOException
     */
    @Override
    @Transactional(readOnly = true)
    public Response<List<AttachedFileResponseDTO>> getAllDocumentsMetadataByIdUser(Long idUser) throws IOException {
        try {

            //Valída que Id recibido corresponda a un usuario.
            UserSec userSec = userService.getByIdInternal(idUser);

            if (userSec.getPerson() != null) {
                //Obtiene la persona.
                Person person = personService.getById(userSec.getPerson().getId());

                //Recupera metadata por persona
                List<AttachedFileResponseDTO> attachedFileResponseDTOS = buildFileMetadataByPerson(person);

                return new Response<>(true, null, attachedFileResponseDTOS);
            } else {
                throw new ConflictException("exception.update.validateNotDevRole.user", null, "exception.update.validateNotDevRole.log", new Object[]{idUser, "AttachedFileService", "saveDocumentUser"}, LogLevel.INFO);
            }
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AttachedFileService", idUser,"<-  Id del documento", "getAllDocumentsMetadataByIdUser");
        }
    }

    /**
     * Método para obtener la metadata de todos los documento PDF de un paciente.
     *
     * @param idPatient id de paciente
     * @return Objeto Response, con la lista de la información de todos los documentos.
     * @throws IOException
     */
    @Override
    @Transactional(readOnly = true)
    public Response<List<AttachedFileResponseDTO>> getAllDocumentsMetadataByIdPatient(Long idPatient) throws IOException {
        try{
            //Valída que Id recibido corresponda a un paciente.
            Patient patient = patientService.getByIdInternal(idPatient);

            //Obtiene la persona.
            Person person = personService.getById(patient.getId());

            //Recupera metadata por persona
            List<AttachedFileResponseDTO> attachedFileResponseDTOS = buildFileMetadataByPerson(person);

            return new Response<>(true, null, attachedFileResponseDTOS);


        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AttachedFileService", idPatient,"<-  Id del paciente", "getAllDocumentsMetadataByIdPatient");
        }

    }

    /**
     * Método para eliminar un documento asociado a un usuario.
     * El mismo realiza una baja lógica, ya que luego se corre una tarea programada para limpieza de archivos basura.
     *
     * @param documentId
     * @return
     * @throws IOException
     */
    @Override
    @Transactional
    @LogAction(
            value = "attachedFileService.systemLogService.disabledByIdDocumentUser",
            args = {"#documentId"},
            level = LogLevel.INFO,
            type = LogType.SYSTEM
    )
    public Response<?> disabledByIdDocumentUser(Long documentId) throws IOException {
        try{

            //Recupera la metadata del archivo.
            AttachedFile file = attachedFilesRepository.findByIdAndEnabledTrue(documentId)
                    .orElseThrow(() -> new NotFoundException("exception.file.attachedFileNotFound.user", null, "exception.file.attachedFilesNotFound.log", new Object[]{documentId, "AttachedFilesService", "disabledByIdDocumentUser"}, LogLevel.ERROR));


            //Control de acceso a la eliminación de archivo.
            UserSec userSec =  userService.getByIdInternal(authenticatedUserService.getAuthenticatedUser().getId());
            boolean havePermission = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(auth -> auth.equals("PERMISO_CONFIGURATION_UPLOAD"));

            if(userSec.getPerson() != null){
                if (!havePermission) {
                    if( !Objects.equals(userSec.getPerson().getId(), file.getPerson().getId())){
                        throw new ForbiddenException("exception.accessDenied.user", null, "exception.accessDenied.log", new Object[]{userSec.getPerson().getId(),"user/{documentId}", "AttachedFilesService", "disabledByIdDocumentUser"},LogLevel.ERROR);
                    }
                }
            }else{
                throw new ConflictException("exception.update.validateNotDevRole.user",null,"exception.update.validateNotDevRole.log",new Object[]{documentId,"AttachedFileService", "disabledByIdDocumentUser"},LogLevel.INFO);
            }

            //Realiza la baja lógica.
            file.setEnabled(false);
            file.setDisabledAt(LocalDateTime.now());
            file.setDisabledBy(authenticatedUserService.getAuthenticatedUser());
            attachedFilesRepository.save(file);

            String messageUser = messageService.getMessage("attachedFileService.disabledDocument.ok.user",null,LocaleContextHolder.getLocale());

            return new Response<>(true, messageUser, null);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AttachedFileService", documentId,"<-  Id del documento", "disabledByIdDocumentUser");
        }
    }





    /**
     * Método para eliminar un documento asociado a un paciente.
     * El mismo realiza una baja lógica, ya que luego se corre una tarea programada para limpieza de archivos basura.
     *
     * @param documentId
     * @return
     * @throws IOException
     */
    @Override
    @LogAction(
            value = "attachedFileService.systemLogService.disabledByIdDocumentPatient",
            args = {"#documentId"},
            level = LogLevel.INFO,
            type = LogType.SYSTEM
    )
    public Response<?> disabledByIdDocumentPatient(Long documentId) throws IOException {
        try{
            //Recupera la metadata del archivo.
            AttachedFile file = attachedFilesRepository.findByIdAndEnabledTrue(documentId)
                    .orElseThrow(() -> new NotFoundException("exception.file.attachedFileNotFound.user", null, "exception.file.attachedFilesNotFound.log", new Object[]{documentId, "AttachedFilesService", "disabledByIdDocumentPatient"}, LogLevel.ERROR));

            //Control de acceso.
            //Se busca si existe el ID Person en el archivo, si no existe no es paciente.
            if(file.getPerson() != null){
                // Validación: verifica que la persona asociada sea un paciente
                patientService.getByIdInternal(file.getPerson().getId());

                //Realiza la baja lógica.
                file.setEnabled(false);
                file.setDisabledAt(LocalDateTime.now());
                file.setDisabledBy(authenticatedUserService.getAuthenticatedUser());
                attachedFilesRepository.save(file);

                String messageUser = messageService.getMessage("attachedFileService.disabledDocument.ok.user",null,LocaleContextHolder.getLocale());

                return new Response<>(true, messageUser, null);


            }else{
                throw new ForbiddenException("exception.accessDenied.user", null, "exception.accessDenied.log", new Object[]{authenticatedUserService.getAuthenticatedUser().getId(),"patient/{documentId}", "AttachedFilesService", "disabledByIdDocumentPatient"},LogLevel.ERROR);
            }
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AttachedFileService", documentId,"<-  Id del documento", "disabledByIdDocumentPatient");
        }
    }

    /**
     * Método que se ejecuta cuando es programado por Spring.
     * Elimina todos los archivos adjuntos con baja lógica de mas de "x" cantidad de días.
     *
     * @throws IOException
     */
    @Override
    @LogAction(
            value ="attachedFilesService.systemLogService.deleteAttachedFiles",
            args =  {"#result.durationSeconds","#result.message", "#result.countInit","#result.countDeleted" },
            type = LogType.SCHEDULED,
            level = LogLevel.INFO
    )
    public SchedulerResultDTO deleteAttachedFiles(){
        int countInit = 0;
        int countDeleted = 0;
        long start;
        long end;
        double durationSeconds;

        //Inicia tarea programada
        start = System.currentTimeMillis();
        try{

            //Obtiene el parámetro de días.
            int days = Integer.parseInt(systemParameterService.getByKey(SystemParameterKey.ATTACHMENT_MIN_DAYS));

            //Establece fecha límite.
            LocalDateTime deadline = LocalDate.now().minusDays(days).atStartOfDay();

            //buscar los archivos adjuntos con estado 0 y que su fecha de baja sea mayor a la fecha límite.
            List<AttachedFile> attachedFiles = attachedFilesRepository.findDisabled(deadline);

            //1. SI NO HAY REGISTROS PARA ELIMINAR
            if (attachedFiles.isEmpty()) {
                //Finaliza tarea programada
                end = System.currentTimeMillis();
                //Convierte milisegundos a segundos.
                durationSeconds = (end - start) / 1000.0;

                SchedulerResultDTO schedulerResultDTO = new SchedulerResultDTO(
                        durationSeconds,
                        "No se encontraron registros para eliminar",
                        countInit,
                        countDeleted);
                return schedulerResultDTO;
            }

            //2. SI EXISTEN REGISTROS PARA ELIMINAR
            //Se obtiene total obtenido para loguear.
            countInit =  attachedFiles.size();

            // Eliminar físicamente los registros.
            List<AttachedFile> physicallyDeleted = fileStorageService.delete(attachedFiles);

            //Actualiza en la base la acción de borrado físico.
            for (AttachedFile file : physicallyDeleted) {
                file.setDeletedByScheduler(true);
                file.setDeletedAtScheduler(LocalDateTime.now());
                countDeleted++;
            }
            attachedFilesRepository.saveAll(physicallyDeleted);

            //Finaliza tarea programada
            end = System.currentTimeMillis();

            //Convierte milisegundos a segundos.
            durationSeconds = (end - start) / 1000.0;

            SchedulerResultDTO schedulerResultDTO = new SchedulerResultDTO(
                    durationSeconds,
                    "Registros eliminados correctamente",
                    countInit,
                    countDeleted);
            return schedulerResultDTO;

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AttachedFileService", null,null, "cleanOrphanDataAttachedFiles");
        }
    }


    /**
     * Construye la URL absoluta para descargar un archivo adjunto específico.
     * <p>
     * Utiliza el contexto actual de la aplicación para generar una URI como:
     * <pre>
     * http://[host]/api/files/{id}/download
     * </pre>
     *
     * @param attachedFile el archivo adjunto del cual se desea construir la ruta de descarga.
     *                     Se espera que el objeto contenga un ID no nulo.
     * @return una cadena con la URI completa para acceder al endpoint de descarga del archivo.
     */
    private String buildDownloadPath(AttachedFile attachedFile){
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/files/")
                .path(attachedFile.getId().toString())
                .path("/download")
                .toUriString();

    }


    /**
     * Método para recuperar la información todos los archivo asociados a una persona por su id.
     * El mismo realiza la conversión a un formato de {@link AttachedFileResponseDTO} y devuelve la lista.
     * @param person
     * @return
     */
    private List<AttachedFileResponseDTO> buildFileMetadataByPerson(Person person){

        //Recupera metadata por Id Person
        List<AttachedFile> files = attachedFilesRepository.findAllByPersonIdAndEnabledTrue(person.getId())
                .orElseThrow(() -> new NotFoundException("exception.file.attachedFileNotFound.user", null, "exception.file.attachedFilesNotFound.log", new Object[]{person.getId(), "AttachedFilesService", "getDocumentById"}, LogLevel.ERROR));


        List<AttachedFileResponseDTO> attachedFileResponseDTOS = files
                .stream()
                .map(file -> new AttachedFileResponseDTO(
                        file.getId(),
                        file.getFileName(),
                        file.getStoredFileName(),
                        file.getCreatedAt(),
                        file.getCreatedBy().getUsername(),
                        buildDownloadPath(file)
                ))
                .toList();

        return attachedFileResponseDTOS;
    }


    /**
     * Método para validar la extensión de un documento.
     * @param filename
     */
    public void extensionVerificationDocument(String filename){
        if (filename == null || !filename.contains(".")) {
            throw new ConflictException("exception.file.conflictExtensionDocument.user", null,"exception.file.conflictExtensionDocument.log", new Object[]{filename, "FileStorageService","saveDocument"}, LogLevel.ERROR);

        }
        //Obtener extensiones desde la base de datos.
        String extensionSystemParameter = systemParameterService.getByKey(SystemParameterKey.EXTENSION_DOCUMENTS);

        //Comparar extensión del archivo recibido vs. base de datos.
        String extensionDocument =  filename.substring(filename.lastIndexOf('.') + 1);
        if (!extensionSystemParameter.contains(extensionDocument.toLowerCase())) {
            throw new ConflictException("exception.file.conflictExtensionDocument.user", null,"exception.file.conflictExtensionDocument.log", new Object[]{filename, "FileStorageService","saveDocument"}, LogLevel.ERROR);
        }

    }

    /**
     * Método para validar el tamaño de un archivo.
     *
     * @param file
     */
    private void sizeVerification(MultipartFile file) {
        //Obtiene tamaño máximo parametrizado de la BD
        long maxSizeMb = Long.parseLong(systemParameterService.getByKey(SystemParameterKey.SIZE_FILE));
        long maxSizeBytes = maxSizeMb * 1024 * 1024;

        if (file.getSize() > maxSizeBytes) {
            throw new ConflictException("exception.file.tooLarge.user", null, "exception.file.tooLarge.log",
                    new Object[]{file.getOriginalFilename(), "FileStorageService", "saveAvatar"}, LogLevel.ERROR);
        }
    }
}
