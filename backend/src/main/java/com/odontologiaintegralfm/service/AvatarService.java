package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.enums.SystemParameterKey;
import com.odontologiaintegralfm.exception.ConflictException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.Person;
import com.odontologiaintegralfm.service.interfaces.IAvatarService;
import com.odontologiaintegralfm.service.interfaces.IFileStorageService;
import com.odontologiaintegralfm.service.interfaces.ISystemParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Servicio encargado de gestionar las operaciones relacionadas con el avatar (imagen de perfil) de una persona.
 * <p>
 * Este servicio se encarga de validar, guardar, obtener y eliminar la imagen de perfil asociada a una persona,
 * delegando el almacenamiento físico al {@link FileStorageService}. También maneja la devolución de imágenes por defecto
 * en caso de que la persona no tenga un avatar personalizado.
 * </p>
 */
@Service
public class AvatarService implements IAvatarService {

    @Autowired
    private ISystemParameterService systemParameterService;

    @Autowired
    private IFileStorageService fileStorageService;




    /**
     * Guarda la imagen de perfil de una persona.
     * <p>
     * El proceso incluye:
     * <ul>
     *     <li>Validar que el archivo no esté vacío.</li>
     *     <li>Verificar que la extensión del archivo sea válida para imágenes.</li>
     *     <li>Eliminar la imagen anterior asociada a la persona si existe.</li>
     *     <li>Generar un nombre único para el nuevo archivo basado en el ID y nombre de la persona.</li>
     *     <li>Guardar la imagen redimensionada y en formato JPG.</li>
     *     <li>Retornar la ruta relativa que debe almacenarse en la entidad {@code Person}.</li>
     * </ul>
     *
     * @param file  Archivo de imagen recibido (MultipartFile).
     * @param person Persona asociada a la imagen.
     * @return Ruta relativa donde se guardó la imagen (ejemplo: "/avatars/123-Perez-Juan.jpg").
     *         Retorna {@code null} si el archivo está vacío.
     * @throws IOException Si ocurre un error al guardar el archivo en el sistema de archivos.
     * @throws ConflictException Si la extensión del archivo no es válida para imágenes.
     */
    @Override
    public String saveImage(MultipartFile file, Person person) throws IOException {

        //Verifica si existe imagen.
        if(file.isEmpty()){
            return null;
        }

        //Verifica extensión de la imágen.
        extensionVerificationImage(file.getOriginalFilename());

        // Eliminar imagen anterior (si existe)
        String previousAvatar = person.getAvatarUrl();
        if (previousAvatar != null) {

            // Pasa nombre del archivo, no la ruta completa
            String previousFilename = Paths.get(previousAvatar).getFileName().toString();
            fileStorageService.deleteImage(previousFilename);
        }

        // Generar nombre único del archivo
        String filename = person.getId()+"-"+person.getLastName()+"-"+person.getFirstName() + ".jpg";

        // Guardar imagen redimensionada y retorna ruta para guardado lógico.
         fileStorageService.saveImage( file, filename);

         return filename;
    }


    /**
         * Devuelve la imagen correspondiente al avatar de una persona.
         * <p>
         * Si la persona tiene un avatar personalizado y el archivo existe, se devuelve ese archivo.
         * Si no tiene avatar o el archivo no existe, se devuelve una imagen por defecto
         * según el género de la persona ('M', 'F' o cualquier otro).
         * </p>
         *
         * @param person ID de la persona cuyo avatar se desea obtener.
         * @return {@link UrlResource} que representa el archivo de imagen a devolver.
         * @throws IOException si ocurre un error al acceder al sistema de archivos.
         * @throws NotFoundException si la persona con el ID dado no existe.
         */
    @Override
    public UrlResource getImage(Person person) throws IOException {
        if (person.getAvatarUrl() != null) {
            return fileStorageService.getImage(person.getAvatarUrl());
        }

        return null;


    }



    /**
     * Elimina la imagén de perfil de una persona.
     * @param person
     * @throws IOException
     */
    @Override
    public String deleteImage(Person person) throws IOException {
        String previousAvatar = person.getAvatarUrl();
        if (previousAvatar == null) {
            return null;
        }

        return fileStorageService.deleteImage(previousAvatar);

    }


    /**
     * Método para validar la extensión de una imagen
     * @param filename
     */
    private void extensionVerificationImage(String filename){
        if (filename == null || !filename.contains(".")) {
            throw new ConflictException("exception.file.conflictExtensionImage.user", null,"exception.file.conflictExtensionImage.log", new Object[]{filename, "FileStorageService","saveAvatar"}, LogLevel.ERROR);

        }
        //Obtener extensiones desde la base de datos.
        String extensionSystemParameter = systemParameterService.getByKey(SystemParameterKey.EXTENSION_IMAGE);

        //Comparar extensión del archivo recibido vs. base de datos.
        String extensionImage =  filename.substring(filename.lastIndexOf('.') + 1);
        if (!extensionSystemParameter.contains(extensionImage.toLowerCase())) {
            throw new ConflictException("exception.file.conflictExtensionImage.user", null,"exception.file.conflictExtensionImage.log", new Object[]{filename, "FileStorageService","saveAvatar"}, LogLevel.ERROR);
        }

    }
}

