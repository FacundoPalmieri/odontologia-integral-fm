package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.configuration.securityConfig.AuthenticatedUserService;
import com.odontologiaintegralfm.dto.AttachedFileResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.*;
import com.odontologiaintegralfm.model.AttachedFiles;
import com.odontologiaintegralfm.model.Patient;
import com.odontologiaintegralfm.model.Person;
import com.odontologiaintegralfm.model.UserSec;
import com.odontologiaintegralfm.repository.IAttachedFilesRepository;
import com.odontologiaintegralfm.service.interfaces.IAttachedFilesService;
import com.odontologiaintegralfm.service.interfaces.IFileStorageService;
import com.odontologiaintegralfm.service.interfaces.IPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Servicio encargado de gestionar los archivos adjuntos (documentos PDF) asociados a personas.
 * Incluye operaciones de almacenamiento, recuperación y obtención de metadatos,
 * pero no accede directamente al sistema de archivos.
 *
 * Todo lo relacionado con los archivos físicos está encapsulado en {@link FileStorageService}.
 * Esa clase es responsable de determinar la ubicación de los archivos, validar su existencia y manejar errores de acceso.
 *
 * @author Facundo Palmieri
 */



@Service
public class AttachedFilesService implements IAttachedFilesService {

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
    public Response<String> saveDocumentUser(MultipartFile file, Long id) throws IOException {

        //Verifica si existe el archivo.
        if(file.isEmpty()){
            return null;
        }

        //Valída que Id recibido corresponda a un usuario.
        UserSec userSec = userService.getByIdInternal(id);

        if(userSec.getPerson() != null){
            //Obtiene la persona.
            Person person = personService.getById(userSec.getPerson().getId());

            //Guarda documento.
            String storedFileName = fileStorageService.saveDocument(file, person);

            AttachedFiles attachedFiles = new AttachedFiles();
            attachedFiles.setFileName(file.getOriginalFilename());
            attachedFiles.setStoredFileName(storedFileName);
            attachedFiles.setFileType(file.getContentType());
            attachedFiles.setPerson(person);
            attachedFiles.setCreatedAt(LocalDateTime.now());
            attachedFiles.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
            attachedFiles.setEnabled(true);

            attachedFiles = attachedFilesRepository.save(attachedFiles);


            String userMessage = messageService.getMessage("attachedFileService.saveDocumentUser.ok.user", null, LocaleContextHolder.getLocale());
            return new Response<>(true, userMessage,attachedFiles.getStoredFileName());

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
    public Response<String> saveDocumentPatient(MultipartFile file, Long id) throws IOException {

        //Verifica si existe el archivo.
        if(file.isEmpty()){
            return null;
        }

        //Valída que Id recibido corresponda a un paciente.
        Patient patient = patientService.getByIdInternal(id);

        //Obtiene la persona.
        Person person = personService.getById(patient.getId());

        //Guarda documento.
        String storedFileName = fileStorageService.saveDocument(file, person);

        AttachedFiles attachedFiles = new AttachedFiles();
        attachedFiles.setFileName(file.getOriginalFilename());
        attachedFiles.setStoredFileName(storedFileName);
        attachedFiles.setFileType(file.getContentType());
        attachedFiles.setPerson(person);
        attachedFiles.setCreatedAt(LocalDateTime.now());
        attachedFiles.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        attachedFiles.setEnabled(true);

        attachedFiles = attachedFilesRepository.save(attachedFiles);

        String userMessage = messageService.getMessage("attachedFileService.saveDocumentUser.ok.user", null, LocaleContextHolder.getLocale());
        return new Response<>(true, userMessage,attachedFiles.getStoredFileName());
    }





    /**
     * Método para obtener de un usuario un documento PDF por su ID.
     * Primero se recupera la metadata mediante y luego se recupera el archivo físico mediante el servicio de fileStorage.

     * @param documentId Id del documento
     * @return
     * @throws IOException
     */
    @Override
    public UrlResource getByIdDocumentUserResource(Long documentId) throws IOException {
        try{

            //Recupera la metadata del archivo.
            AttachedFiles file = attachedFilesRepository.findById(documentId)
                    .orElseThrow(() -> new NotFoundException("exception.file.attachedFilesNotFound.user", null, "exception.file.attachedFilesNotFound.log", new Object[]{documentId, "AttachedFilesService", "getByIdDocumentResource"}, LogLevel.ERROR));


            //Control de acceso a la descarga de archivo.
            UserSec userSec =  userService.getByIdInternal(authenticatedUserService.getAuthenticatedUser().getId());
            boolean havePermission = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(auth -> auth.equals("PERMISO_CONFIGURATION_UPLOAD"));

            if(userSec.getPerson() != null){
                if (!havePermission) {
                    if( !Objects.equals(userSec.getPerson().getId(), file.getPerson().getId())){
                        throw new ForbiddenException("exception.accessDenied.user", null, "exception.accessDenied.log", new Object[]{userSec.getPerson().getId(),"user/{documentId}/download", "AttachedFilesService", "getByIdDocumentResource"},LogLevel.ERROR);
                    }
                }
            }else{
                throw new ConflictException("exception.update.validateNotDevRole.user",null,"exception.update.validateNotDevRole.log",new Object[]{documentId,"AttachedFileService", "getByIdDocumentUserResource"},LogLevel.INFO);
            }
            //Recupera el archivo físico y retorna
            return fileStorageService.getDocument(file);

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
    public UrlResource getByIdDocumentPatientResource(Long documentId) throws IOException {
        try{

            //Recupera la metadata del archivo.
            AttachedFiles file = attachedFilesRepository.findById(documentId)
                    .orElseThrow(() -> new NotFoundException("exception.file.attachedFilesNotFound.user", null, "exception.file.attachedFilesNotFound.log", new Object[]{documentId, "AttachedFilesService", "getByIdDocumentResource"}, LogLevel.ERROR));

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


    private String buildDownloadPath(AttachedFiles attachedFile){
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/files/")
                .path(attachedFile.getId().toString())
                .path("/download")
                .toUriString();

    }

    private List<AttachedFileResponseDTO> buildFileMetadataByPerson(Person person){

        //Recupera metadata por Id Person
        List<AttachedFiles> files = attachedFilesRepository.findAllByPersonIdAndEnabledTrue(person.getId())
                .orElseThrow(() -> new NotFoundException("exception.file.attachedFilesNotFound.user", null, "exception.file.attachedFilesNotFound.log", new Object[]{person.getId(), "AttachedFilesService", "getDocumentById"}, LogLevel.ERROR));


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
}
