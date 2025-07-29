package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.model.AttachedFile;
import com.odontologiaintegralfm.model.Person;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

/**
 * Interfaz que define las operaciones para el almacenamiento y recuperación de archivos físicos.
 * <p>
 * Las implementaciones deben encargarse de manejar imágenes de perfil y documentos,
 * incluyendo validaciones de tamaño y extensión, así como la gestión de rutas físicas.
 * </p>
 */
public interface IFileStorageService {
    /**
     * Método para actualizar una imagen
     * @param file
     * @param person
     * @return
     */
    String saveImage(MultipartFile file, Person person) throws IOException;

    /**
     * Método para obtener una imagen.
     * @param person Id de la persona.
     * @return
     * @throws IOException
     */
    UrlResource getImage(Person person) throws IOException;

    /**
     * Método para eliminar una imagen
     * @param person Id de la persona.
     * @throws IOException
     */
    void deleteImage(Person person) throws IOException;

    /**
     * Método para guardar un documento en formato PDF.
     * @param file
     * @param person
     * @return
     * @throws IOException
     */
    String saveDocument(MultipartFile file, Person person) throws IOException;

    /**
     * Método para obtener el documento físico en formato PDF.
     * @param file
     * @return
     * @throws IOException
     */
    UrlResource getDocument(AttachedFile file) throws IOException;


    /**
     * Método para validar la extensión de un archivo.
     * @param filename
     * @return
     */
    void extensionVerificationImage(String filename);



    /**
     * Método para validar el tamaño de un archivo.
     * @param file
     */
    void sizeVerification(MultipartFile file);

    /**
     * Eliminación física de archivos adjuntos.
     * Esté método es llamado desde {@link com.odontologiaintegralfm.service.AttachedFilesService} el cual es invocado por la tarea programada.
     * @param files
     */
    List<AttachedFile>  delete(List<AttachedFile> files);


}
