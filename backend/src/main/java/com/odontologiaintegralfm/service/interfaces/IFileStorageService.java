package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.model.AttachedFile;
import com.odontologiaintegralfm.model.Person;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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
     * @param filename
     * @return
     */
    void saveImage(MultipartFile file, String filename) throws IOException;


    /**
     * Método para eliminar una imagen
     * @param filename nombre archivo.
     * @throws IOException
     */
    String deleteImage(String filename) throws IOException;

    /**
     * Obtiene la ruta completa (Estructura carpetas + nombre archivo) de la imágen.
     * @param avatarUrl
     * @return
     * @throws IOException
     */
    UrlResource getImage(String avatarUrl) throws IOException;

    /**
     * Obtiene una imágen por defecto.
     * @param defaultFilename
     * @return
     * @throws IOException
     */
    UrlResource getDefaultImage(String defaultFilename) throws IOException;

    /**
     * Método para guardar un documento en formato PDF.
     * @param file
     * @param
     * @return
     * @throws IOException
     */
    void saveDocument(MultipartFile file, String filename) throws IOException;

    /**
     * Método para obtener el documento físico en formato PDF.
     * @param file
     * @return
     * @throws IOException
     */
    UrlResource getDocument(AttachedFile file) throws IOException;



    /**
     * Eliminación física de archivos adjuntos.
     * Esté método es llamado desde {@link com.odontologiaintegralfm.service.AttachedFilesService} el cual es invocado por la tarea programada.
     * @param files
     */
    List<AttachedFile>  delete(List<AttachedFile> files);




}
