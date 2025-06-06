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
     * Método para guardar un documento en formato PDF.
     * @param file
     * @param personId
     * @return
     * @throws IOException
     */
    Response<String> saveDocument (MultipartFile file, Long personId) throws IOException;


    /**
     * Método para obtener un documento PDF
     * @param id Id del documento
     * @return
     * @throws IOException
     */
    UrlResource getByIdDocumentResource(Long id) throws IOException;


    /**
     * Método para obtener la metadata de un documento PDF
     * @param id Id del documento
     * @return
     * @throws IOException
     */
    Response<AttachedFileResponseDTO> getByIdDocumentMetaData(Long id) throws IOException;

    /**
     * Método para obtener la información de todos los documentos adjuntos.
     * @return  Response<List<AttachedFileResponseDTO>>
     * @throws IOException
     */
    Response<List<AttachedFileResponseDTO>> getAllDocumentMetaData() throws IOException;
}
