package com.odontologiaintegralfm.feature.person.core.service.implement;

import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.feature.person.core.model.AttachedFile;
import com.odontologiaintegralfm.feature.person.core.service.intefaces.IFileStorageService;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;


/**
 * Servicio encargado de gestionar el almacenamiento y recuperación de archivos físicos.
 * <p>
 * Soporta dos tipos de archivos:
 * <ul>
 *     <li>Imágenes de perfil de personas (avatares)</li>
 *     <li>Documentos (generalmente en formato PDF) adjuntos a personas</li>
 * </ul>
 *
 * Funcionalidades principales:
 * <ul>
 *     <li>Guardar y obtener imágenes de perfil</li>
 *     <li>Guardar y obtener documentos</li>
 *     <li>Validar extensiones y tamaños permitidos</li>
 * </ul>
 *
 * Las rutas, extensiones y tamaños máximos están configuradas mediante propiedades externas (`application.properties` o `application.yml`).
 *
 * @author Facundo Palmieri
 */
@Service
@Slf4j
public class FileStorageService implements IFileStorageService {


    @Value("${file.upload-dir.image}")
    private String uploadDirImage;

    @Value("${file.upload-dir.document}")
    private String uploadDirDocument;


    /**
     * Guarda o actualiza una imagen de perfil redimensionándola y almacenándola en disco.
     * <p>
     * El archivo se redimensiona a 300x300 píxeles con calidad 0.8 y se guarda en formato JPG.
     * Si la carpeta de destino no existe, se crea automáticamente.
     *
     * @param file     Archivo de imagen recibido (MultipartFile).
     * @param filename Nombre del archivo bajo el cual se guardará la imagen (debe incluir extensión, ej. "123-Perez-Juan.jpg").
     * @return La ruta relativa donde se almacenó la imagen (por ejemplo, "/avatars/123-Perez-Juan.jpg").
     * @throws IOException Si ocurre un error al escribir el archivo en el sistema de archivos.
     */
    @Override
    public void saveImage(MultipartFile file, String filename) throws IOException {

        //Crea directorio si no existe.
        Path uploadPath = Paths.get(uploadDirImage);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(filename);

        // Redimensionar y guardar (300x300, calidad 0.8)
        Thumbnails.of(file.getInputStream())
                .size(300, 300)
                .outputFormat("jpg")
                .outputQuality(0.8)
                .toFile(filePath.toFile());


    }


    /**
     * Método para eliminar una imagen
     * @param filename nombre archivo.
     * @throws IOException
     */
    @Override
    public String deleteImage(String filename) throws IOException {
        Path path = Paths.get(uploadDirImage, filename);
        Files.deleteIfExists(path);

        //Retorna ruta vacía para registro lógico.
        return null;
    }


    /**
     * Obtiene la ruta completa (Estructura carpetas + nombre archivo) de la imágen.
     *
     * @param avatarUrl
     * @return
     * @throws IOException
     */
    @Override
    public UrlResource getImage(String avatarUrl) throws IOException {
        // Se extrae solo el nombre del archivo.
        String filename = Paths.get(avatarUrl).getFileName().toString();

        Path filePath = Paths.get(uploadDirImage).resolve(filename);
        if (!Files.exists(filePath)) {
            throw new NotFoundException(null,null,"exception.file.notFoundImage.log",new Object[]{avatarUrl,"FileStorageService","getImage"},LogLevel.WARN);
        }
        return new UrlResource(filePath.toUri());
    }

    /**
     * Obtiene una imágen por defecto.
     *
     * @param defaultFilename
     * @return
     * @throws IOException
     */
    @Override
    public UrlResource getDefaultImage(String defaultFilename) throws IOException {
        Path filePath = Paths.get(uploadDirImage).resolve(defaultFilename);
        return new UrlResource(filePath.toUri());
    }


    /**
     * Método para guardar un documento en formato PDF.
     *
     * @param file
     * @param
     * @return
     * @throws IOException
     */
    @Override
    public void saveDocument(MultipartFile file, String filename) throws IOException {

        //Crea directorio si no existe.
        Path uploadPath = Paths.get(uploadDirDocument);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        //Concatena el nombre del archivo a la ruta de carpetas
        Path filePath = uploadPath.resolve(filename);

        // Guarda archivo
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Método para obtener un documento físico en formato PDF.
     *
     * @param file
     * @return
     * @throws IOException
     */
    @Override
    public UrlResource getDocument(AttachedFile file) throws IOException {

        Path path = Paths.get(uploadDirDocument, file.getStoredFileName());
        if (!Files.exists(path)) {
            throw new NotFoundException("exception.file.attachedFileNotFound.user", null, "exception.file.attachedFilesNotFound.log", new Object[]{file.getStoredFileName(), "FileStorageService", "getDocument"}, LogLevel.ERROR);
        }

        return new UrlResource(path.toUri());
    }


    /**
     * Elimina físicamente archivos adjuntos del sistema de archivos.
     * Este método es invocado por {@link AttachedFilesService}, que a su vez es utilizado por una tarea programada.
     *
     * @param files lista de archivos adjuntos a eliminar.
     */
    @Override
    public List<AttachedFile> delete(List<AttachedFile> files) {

        List<AttachedFile> deleted = new ArrayList<>();

        for (AttachedFile file : files) {
            try {
                Path path = Paths.get(uploadDirDocument,
                        Paths.get(file.getStoredFileName()).getFileName().toString());

                if (Files.deleteIfExists(path)) {
                    deleted.add(file);
                }
            } catch (IOException e) {
                log.warn("No se pudo eliminar el archivo: {}", file.getStoredFileName(), e);
            }
        }

        return deleted;

    }
}




