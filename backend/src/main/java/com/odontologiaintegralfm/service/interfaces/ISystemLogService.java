package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.SystemLogResponseDTO;
import com.odontologiaintegralfm.dto.internal.SchedulerResultDTO;
import org.springframework.data.domain.Page;
import java.util.Map;


public interface ISystemLogService {

    /**
     * Método para persistir logs.
     * @param dto
     */
    void save(SystemLogResponseDTO dto);


    /**
     * Método para obtener el listado paginado de los logs.
     * @param pageValue
     * @param sizeValue
     * @param sortByValue
     * @param directionValue
     * @return
     */
    Page<SystemLogResponseDTO> getAll(int pageValue, int sizeValue, String sortByValue, String directionValue);


    /**
     * Convierte el Stack Trace de una exception en un string para luego persistir en base de datos.
     * @param e
     * @return
     */
    String getStackTraceAsString(Throwable e);



    /**
     * Construye un mapa de metadatos a partir de un array de argumentos.
     * <p>
     * Cada argumento es mapeado con una clave en formato {@code "arg0"}, {@code "arg1"}, etc.
     *
     * @param args Array de objetos que se desea incluir en el mapa de metadatos.
     * @return Un {@code Map<String, Object>} con las claves generadas y los argumentos como valores,
     *         o {@code null} si el array es {@code null} o está vacío.
     */
    Map<String, Object> buildMetadataMap(Object[] args);



    /**
     * Convierte un {@code Map<String, Object>} a su representación en formato JSON.
     *
     * @param metadata el mapa que contiene los datos a serializar. Puede ser {@code null} o vacío.
     * @return un string JSON que representa el contenido del mapa, o {@code null} si el mapa es {@code null} o está vacío.
     *         En caso de error durante la serialización, retorna un JSON con un mensaje de error.
     */
    String toJson(Map<String, Object> metadata);


    /**
     * Elimina logs con una antigüedad mayor a la parametrización de
     * Entidad: {@link com.odontologiaintegralfm.model.SystemParameter}
     * Tarea:  {@link com.odontologiaintegralfm.enums.ScheduledTaskKey}
     */
    SchedulerResultDTO delete();



}
