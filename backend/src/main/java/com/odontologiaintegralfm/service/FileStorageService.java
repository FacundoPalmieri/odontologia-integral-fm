package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.ConflictException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.AttachedFiles;
import com.odontologiaintegralfm.model.Person;
import com.odontologiaintegralfm.repository.IPersonRepository;
import com.odontologiaintegralfm.service.interfaces.IFileStorageService;
import jakarta.annotation.PostConstruct;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author [Facundo Palmieri]
 */
@Service
public class FileStorageService implements IFileStorageService {

    @Value("${file.upload-dir.image}")
    private String uploadDirImage;

    @Value("${file.upload-dir.document}")
    private String uploadDirDocument;

    @Value("${file.default-avatar-m}")
    private String defaultAvatarM;

    @Value("${file.default-avatar-f}")
    private String defaultAvatarF;

    @Value("${file.default-avatar-x}")
    private String defaultAvatarX;

    @Value("${file.size}")
    private long maxFileSize;

    @Value("${file.image.extensions}")
    private String allowedExtensionsImagesRaw;

    @Value("${file.document.extensions}")
    private String allowedExtensionsDocumentsRaw;

    private Set<String> allowedExtensionsImages;

    private Set<String> allowedExtensionsDocuments;



    @PostConstruct
    public void init() {
        this.allowedExtensionsImages = Arrays.stream(allowedExtensionsImagesRaw.split(","))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        this.allowedExtensionsDocuments = Arrays.stream(allowedExtensionsDocumentsRaw.split(","))
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

    }

    /**
     * Método para actualizar la imágen de perfil de la persona.
     *
     * @param file
     * @param person
     * @return
     */
    @Override
    public String saveImage(MultipartFile file, Person person)throws IOException {

        //Verifica si existe imagen.
        if(file.isEmpty()){
            return null;
        }

        //Verifica extensión de la imágen.
        extensionVerificationImage(file.getOriginalFilename());

        //Verifica tamaño de la imágen
        sizeVerification(file);


        //Crea directorio si no existe.
        Path uploadPath = Paths.get(uploadDirImage);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        //Elimina imagen anterior si existe.
        String previousAvatar = person.getAvatarUrl();
        if (previousAvatar != null) {
            Path previousPath = Paths.get(uploadDirImage, Paths.get(previousAvatar).getFileName().toString());
            Files.deleteIfExists(previousPath);
        }


        // Generar nombre único del archivo
        String filename = person.getId()+"-"+person.getLastName()+"-"+person.getFirstName() + ".jpg";
        Path filePath = uploadPath.resolve(filename);

        // Redimensionar y guardar (300x300, calidad 0.8)
        Thumbnails.of(file.getInputStream())
                .size(300, 300)
                .outputFormat("jpg")
                .outputQuality(0.8)
                .toFile(filePath.toFile());


        // Guardar ruta en la entidad
        String avatarUrl = "/avatars/" + filename;
        return avatarUrl;

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
        Path avatarPath;

        if (person.getAvatarUrl() != null) {
            avatarPath = Paths.get(uploadDirImage, Paths.get(person.getAvatarUrl()).getFileName().toString());
            if (Files.exists(avatarPath)) {
                return new UrlResource(avatarPath.toUri());
            }
        }

        // Si no tiene avatar o el archivo no existe
        switch (person.getGender().getAlias()){
            case 'M':
                avatarPath = Paths.get(defaultAvatarM);
            break;

            case 'F':
                avatarPath = Paths.get(defaultAvatarF);
                break;

            default:
                avatarPath = Paths.get(defaultAvatarX);
                break;
        }
        return new UrlResource(avatarPath.toUri());
    }

    /**
     * Método para guardar un documento en formato PDF.
     *
     * @param file
     * @param person
     * @return
     * @throws IOException
     */
    @Override
    public String saveDocument(MultipartFile file, Person person) throws IOException {

        //Verifica si existe el documento.
        if(file.isEmpty()){
            return null;
        }

        //Verifica extensión de del documento.
        extensionVerificationDocument(file.getOriginalFilename());

        //Verifica tamaño del documento
        sizeVerification(file);

        //Crea directorio si no existe.
        Path uploadPath = Paths.get(uploadDirDocument);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Sanitiza el nombre original
        String originalFilename = Paths.get(file.getOriginalFilename()).getFileName().toString();
        String sanitizedOriginalName = originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

        // Crea nombre único
        String filename = person.getId() + "-" + person.getLastName()+person.getFirstName() + "-" + sanitizedOriginalName;
        Path filePath = uploadPath.resolve(filename);

        // Guarda archivo
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Retorna el nombre físico para persistir
        return filename;
    }

    /**
     * Método para obtener un documento en formato PDF.
     *
     * @param file
     * @return
     * @throws IOException
     */
    @Override
    public UrlResource getDocument(AttachedFiles file) throws IOException {

        Path path = Paths.get(uploadDirDocument, file.getStoredFileName());
        if (!Files.exists(path)) {
            throw new NotFoundException("exception.attachedFilesNotFound.user", null, "exception.attachedFilesNotFound.log", new Object[]{file.getStoredFileName(), "FileStorageService", "getDocument"}, LogLevel.ERROR);
        }

        return new UrlResource(path.toUri());
    }

    /**
     * Método para validar la extensión de una imagen
     * @param filename
     */
    public void extensionVerificationImage(String filename){
        if (filename == null || !filename.contains(".")) {
            throw new ConflictException("exception.file.conflictExtensionImage.user", null,"exception.file.conflictExtensionImage.log", new Object[]{filename, "FileStorageService","saveAvatar"}, LogLevel.ERROR);

        }

        String extension =  filename.substring(filename.lastIndexOf('.') + 1);
         if (!allowedExtensionsImages.contains(extension.toLowerCase())) {
            throw new ConflictException("exception.file.conflictExtensionImage.user", null,"exception.file.conflictExtensionImage.log", new Object[]{filename, "FileStorageService","saveAvatar"}, LogLevel.ERROR);
        }

    }




    /**
     * Método para validar la extensión de un documento.
     * @param filename
     */
    public void extensionVerificationDocument(String filename){
        if (filename == null || !filename.contains(".")) {
            throw new ConflictException("exception.file.conflictExtensionDocument.user", null,"exception.file.conflictExtensionDocument.log", new Object[]{filename, "FileStorageService","saveDocument"}, LogLevel.ERROR);

        }

        String extension =  filename.substring(filename.lastIndexOf('.') + 1);
        if (!allowedExtensionsDocuments.contains(extension.toLowerCase())) {
            throw new ConflictException("exception.file.conflictExtensionDocument.user", null,"exception.file.conflictExtensionDocument.log", new Object[]{filename, "FileStorageService","saveDocument"}, LogLevel.ERROR);
        }

    }


    /**
     * Método para validar el tamaño de un archivo.
     *
     * @param file
     */
    @Override
    public void sizeVerification(MultipartFile file) {
        if (file.getSize() > maxFileSize) {
            throw new ConflictException("exception.file.tooLarge.user", null, "exception.file.tooLarge.log",
                    new Object[]{file.getOriginalFilename(), "FileStorageService", "saveAvatar"}, LogLevel.ERROR);
        }
    }


}
