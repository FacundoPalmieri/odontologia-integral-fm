package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.exception.ConflictException;
import com.odontologiaintegralfm.model.Person;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;


public interface IAvatarService {

    /**
     * Guarda una imagen de perfil para la persona indicada.
     *
     * @param file  El archivo de imagen a guardar (MultipartFile).
     * @param person La persona a la que se asocia la imagen.
     * @return La ruta relativa donde se almacenó la imagen (por ejemplo, "/avatars/123-Perez-Juan.jpg").
     *         Retorna {@code null} si el archivo está vacío.
     * @throws IOException         Si ocurre un error al guardar el archivo en el sistema de archivos.
     * @throws ConflictException   Si la extensión del archivo no es válida para imágenes.
     */
    String saveImage(MultipartFile file, Person person) throws IOException;


    /**
     * Método para obtener la imágen de perfil de una persona.
     * @param person Id de la persona.
     * @return La ruta relativa donde se almacenó la imagen (por ejemplo, "/avatars/123-Perez-Juan.jpg").
     * @throws IOException
     */
    UrlResource getImage(Person person) throws IOException;


    /**
     * Elimina la imagén de perfil de una persona.
     * @param person
     * @throws IOException
     */
    String deleteImage(Person person) throws IOException;

}
