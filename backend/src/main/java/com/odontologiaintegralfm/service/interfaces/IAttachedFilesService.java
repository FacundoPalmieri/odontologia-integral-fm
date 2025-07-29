package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.AttachedFileResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

/**
 * Interfaz que define las operaciones relacionadas con la gestión de archivos adjuntos (documentos PDF).
 * Permite guardar, obtener y consultar la metadata de los documentos asociados a personas.
 *
 * @author Facundo Palmieri
 */
public interface IAttachedFilesService {

    /**
     * Método para guardar un documento en formato PDF asociado a un Usuario.
     * @param file
     * @param id
     * @return
     * @throws IOException
     */
    Response<String> saveDocumentUser(MultipartFile file, Long id) throws IOException;



    /**
     * Método para guardar un documento en formato PDF asociado a un Paciente.
     * @param file
     * @param id
     * @return
     * @throws IOException
     */
    Response<String> saveDocumentPatient(MultipartFile file, Long id) throws IOException;



    /**
     * Método para obtener de un usuario un documento PDF por su ID.
     * @param documentId Id del documento
     * @return
     * @throws IOException
     */
    UrlResource getByIdDocumentUserResource(Long documentId) throws IOException;




    /**
     * Método para obtener de un paciente un documento PDF por su ID.
     * @param documentId Id del documento
     * @return
     * @throws IOException
     */
    UrlResource getByIdDocumentPatientResource(Long documentId) throws IOException;




    /**
     * Método para obtener la metadata de todos los documento PDF de un usuario.
     * @param idUser idUser
     * @return Objeto Response, con la lista de la información de todos los documentos.
     * @throws IOException
     */
    Response<List<AttachedFileResponseDTO>> getAllDocumentsMetadataByIdUser(Long idUser) throws IOException;



    /**
     * Método para obtener la metadata de todos los documento PDF de un paciente.
     * @param idPatient id de paciente
     * @return Objeto Response, con la lista de la información de todos los documentos.
     * @throws IOException
     */
    Response<List<AttachedFileResponseDTO>> getAllDocumentsMetadataByIdPatient(Long idPatient) throws IOException;


    /**
     * Método para eliminar un documento asociado a un usuario.
     * El mismo realiza una baja lógica, ya que luego se corre una tarea programada para limpieza de archivos basura.
     * @param idDocument
     * @return
     * @throws IOException
     */
    Response<?> disabledByIdDocumentUser(Long idDocument) throws IOException;


    /**
     * Método para eliminar un documento asociado a un paciente.
     * El mismo realiza una baja lógica, ya que luego se corre una tarea programada para limpieza de archivos basura.
     * @param idDocument
     * @return
     * @throws IOException
     */
    Response<?> disabledByIdDocumentPatient (Long idDocument) throws IOException;

    /**
     /**
     * Método que se ejecuta cuando es programado por Spring.
     * Elimina todos los archivos adjuntos con baja lógica de mas de "x" cantidad de días.
     * @throws IOException
     */
     void deleteAttachedFiles();

}
