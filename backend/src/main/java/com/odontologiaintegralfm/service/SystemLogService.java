package com.odontologiaintegralfm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odontologiaintegralfm.configuration.appConfig.annotations.LogAction;
import com.odontologiaintegralfm.dto.SystemLogResponseDTO;
import com.odontologiaintegralfm.dto.internal.SchedulerResultDTO;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.enums.LogType;
import com.odontologiaintegralfm.enums.ScheduledTaskKey;
import com.odontologiaintegralfm.enums.SystemParameterKey;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.SystemLog;
import com.odontologiaintegralfm.model.SystemParameter;
import com.odontologiaintegralfm.repository.ISystemLogRepository;
import com.odontologiaintegralfm.service.interfaces.ISystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@Service
public class SystemLogService implements ISystemLogService {
     @Autowired
     private ISystemLogRepository systemLogRepository;

     @Autowired
     private ObjectMapper objectMapper;

     @Autowired
     private SystemParameterService systemParameterService;

     @Override
     public void save(SystemLogResponseDTO dto) {
         SystemLog log = new SystemLog();

         log.setTimestamp(LocalDateTime.now());
         log.setLevel(dto.level());
         log.setType(dto.type());
         log.setUserMessage(dto.userMessage());
         log.setTechnicalMessage(dto.technicalMessage());
         log.setName(dto.name());
         log.setUsername(dto.username());
         log.setMetadata(toJson(dto.metadata()));
         log.setStackTrace(dto.stackTrace());

         systemLogRepository.save(log);
     }


    /**
     * @param page
     * @param size
     * @param sortBy
     * @param direction
     */
    @Override
    public Page<SystemLogResponseDTO> getAll(int page, int size, String sortBy, String direction) {
        try{
            //Define criterio de ordenamiento
            Sort sort = direction.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            //Se define paginación con n°página, cantidad elementos y ordenamiento.
            Pageable pageable = PageRequest.of(page,size, sort);

            Page<SystemLog> systemLog = systemLogRepository.findAll(pageable);

            Page<SystemLogResponseDTO> systemLogResponseDTOPage = systemLog
                    .map(log -> new SystemLogResponseDTO(
                            log.getLevel(),               // level
                            log.getType(),
                            log.getUserMessage(),
                            log.getTechnicalMessage(),
                            log.getName(),
                            log.getUsername(),
                            fromJson(log.getMetadata()),
                            log.getStackTrace()
                    ));

           return systemLogResponseDTOPage;

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "SystemLogService", null, null, "getAll");
        }

    }


    /**
     * Convierte un string JSON a un {@code Map<String, Object>} .
     *
     * @param json el string JSON a convertir. Puede ser {@code null} o vacío.
     * @return un {@code Map<String, Object>} con los datos convertidos, o un mapa vacío si el JSON es {@code null} o vacío.
     * @throws RuntimeException si ocurre un error durante la deserialización del JSON.
     */
    public Map<String, Object> fromJson(String json) {
        try {
            if (json == null || json.trim().isEmpty()) {
                return Collections.emptyMap(); // o null si preferís
            }
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error al convertir JSON a Map: " + json, e);
        }
    }



    /**
     * Convierte un {@code Map<String, Object>} a su representación en formato JSON.
     *
     * @param metadata el mapa que contiene los datos a serializar. Puede ser {@code null} o vacío.
     * @return un string JSON que representa el contenido del mapa, o {@code null} si el mapa es {@code null} o está vacío.
     *         En caso de error durante la serialización, retorna un JSON con un mensaje de error.
     */
    public String toJson(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            return "{\"error\": \"Error serializando metadata\"}";
        }
    }

    /**
     * Elimina logs con una antigüedad mayor a la parametrización de
     * Entidad: {@link SystemParameter}
     * Tarea:  {@link ScheduledTaskKey}
     */
    @Override
    @LogAction(
            value ="systemLogService.systemLogService.delete",
            args =  {"#result.durationSeconds","#result.message","#result.countDeleted" },
            type = LogType.SCHEDULED,
            level = LogLevel.INFO
    )
    public SchedulerResultDTO delete() {
        int countDeleted = 0;
        long start;
        long end;
        double durationSeconds;

        //Inicia tarea programada
        start = System.currentTimeMillis();

        try{
            //Obtiene el parámetro de días.
            int days = Integer.parseInt( systemParameterService.getByKey(SystemParameterKey.LOGS_MIN_DAYS));

            //Establece fecha límite.
            LocalDateTime deadline = LocalDate.now().minusDays(days).atStartOfDay();

            //Elimina logs
            countDeleted = systemLogRepository.deleteBeforeDeadline(deadline);

            //Finaliza tarea programada
            end = System.currentTimeMillis();

            //Convierte milisegundos a segundos.
            durationSeconds = (end - start) / 1000.0;

            SchedulerResultDTO schedulerResultDTO = new SchedulerResultDTO(
                    durationSeconds,
                    "Registros eliminados correctamente",
                    0,
                    countDeleted);
            return schedulerResultDTO;

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "SystemLogService", null, null, "delete");
        }
    }


    /**
     * Construye un mapa de metadatos a partir de un array de argumentos.
     * <p>
     * Cada argumento es mapeado con una clave en formato {@code "arg0"}, {@code "arg1"}, etc.
     *
     * @param args Array de objetos que se desea incluir en el mapa de metadatos.
     * @return Un {@code Map<String, Object>} con las claves generadas y los argumentos como valores,
     *         o {@code null} si el array es {@code null} o está vacío.
     */
    public Map<String, Object> buildMetadataMap(Object[] args) {
        if (args == null || args.length == 0) return null;
        Map<String, Object> metadata = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            metadata.put("arg" + i, args[i]);
        }
        return metadata;
    }



    /**
     * Convierte el Stack Trace de una exception en un string para luego persistir en base de datos.
     * @param e
     * @return
     */
    public String getStackTraceAsString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }


}





