package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.AttachedFileResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author [Facundo Palmieri]
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
    UrlResource getDocumentResourceById(Long id) throws IOException;


    /**
     * Método para obtener la metadata de un documento PDF
     * @param id Id del documento
     * @return
     * @throws IOException
     */
    Response<AttachedFileResponseDTO> getDocumentMetaDataById(Long id) throws IOException;
}
