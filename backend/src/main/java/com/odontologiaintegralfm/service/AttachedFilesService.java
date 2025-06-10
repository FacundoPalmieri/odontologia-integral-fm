package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.configuration.securityConfig.AuthenticatedUserService;
import com.odontologiaintegralfm.dto.AttachedFileResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.AttachedFiles;
import com.odontologiaintegralfm.model.Person;
import com.odontologiaintegralfm.repository.IAttachedFilesRepository;
import com.odontologiaintegralfm.service.interfaces.IAttachedFilesService;
import com.odontologiaintegralfm.service.interfaces.IFileStorageService;
import com.odontologiaintegralfm.service.interfaces.IPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio encargado de gestionar los archivos adjuntos (documentos PDF) asociados a personas.
 * Incluye operaciones de almacenamiento, recuperación y obtención de metadata.
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



    /**
     * Método para guardar un documento en formato PDF.
     *
     * @param file
     * @param personId
     * @return
     * @throws IOException
     */
    @Override
    public Response<String> saveDocument(MultipartFile file, Long personId) throws IOException {

        //Verifica si existe el archivo.
        if(file.isEmpty()){
            return null;
        }

        //Obtiene la persona
        Person person = personService.getById(personId);

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

        return new Response<>(true, null,attachedFiles.getStoredFileName());
    }

    /**
     * Método para obtener un documento PDF
     *
     * @param id Id del documento
     * @return
     * @throws IOException
     */
    @Override
    public UrlResource getByIdDocumentResource(Long id) throws IOException {
        try{
            AttachedFiles file = attachedFilesRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("exception.file.attachedFilesNotFound.user", null, "exception.file.attachedFilesNotFound.log", new Object[]{id, "AttachedFilesService", "getByIdDocumentResource"}, LogLevel.ERROR));

            return fileStorageService.getDocument(file);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AttachedService", id,"<-  Id del documento", "getByIdDocumentResource");
        }
    }

    /**
     * Método para obtener la metadata de un documento PDF
     *
     * @param id Id del documento
     * @return
     * @throws IOException
     */
    @Override
    public Response<AttachedFileResponseDTO> getByIdDocumentMetaData(Long id) throws IOException {
        try{
            AttachedFiles file = attachedFilesRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("exception.file.attachedFilesNotFound.user", null, "exception.file.attachedFilesNotFound.log", new Object[]{id, "AttachedFilesService", "getDocumentById"}, LogLevel.ERROR));

            AttachedFileResponseDTO attachedFileResponseDTO = new AttachedFileResponseDTO(
                    file.getId(),
                    file.getFileName(),
                    file.getStoredFileName(),
                    file.getCreatedAt(),
                    file.getCreatedBy().getUsername(),
                    buildDownloadPath(file)
            );
            return new Response<>(true, null,attachedFileResponseDTO);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AttachedService", id,"<-  Id del documento", "getByIdDocumentMetaData");
        }
    }

    /**
     * Método para obtener la información de todos los documentos adjuntos.
     *
     * @return Response<List < AttachedFileResponseDTO>>
     * @throws IOException
     */
    @Override
    public Response<List<AttachedFileResponseDTO>> getAllDocumentMetaData() throws IOException {
        try{
            List<AttachedFiles> attachedFiles = attachedFilesRepository.findByEnabledTrue();

            List<AttachedFileResponseDTO> attachedFileResponseDTOS = attachedFiles.stream()
                    .map(attachedFile -> new AttachedFileResponseDTO(
                            attachedFile.getId(),
                            attachedFile.getFileName(),
                            attachedFile.getStoredFileName(),
                            attachedFile.getCreatedAt(),
                            attachedFile.getCreatedBy().getUsername(),
                            buildDownloadPath(attachedFile)
                    ))
                    .toList();

            return new Response<>(true, null,attachedFileResponseDTOS);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "AttachedService", null,null, "getAllDocumentMetaData");
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
}
