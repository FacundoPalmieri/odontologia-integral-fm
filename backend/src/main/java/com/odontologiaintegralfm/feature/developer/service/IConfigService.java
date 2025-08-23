package com.odontologiaintegralfm.feature.developer.service;

import com.odontologiaintegralfm.infrastructure.message.dto.MessageRequestDTO;
import com.odontologiaintegralfm.infrastructure.message.model.MessageConfig;
import com.odontologiaintegralfm.infrastructure.systemparameter.dto.SystemParameterRequestDTO;
import com.odontologiaintegralfm.infrastructure.systemparameter.dto.SystemParameterResponseDTO;
import com.odontologiaintegralfm.infrastructure.logging.dto.SystemLogResponseDTO;
import com.odontologiaintegralfm.infrastructure.logging.service.SystemLogService;
import com.odontologiaintegralfm.infrastructure.scheduler.dto.ScheduleRequestDTO;
import com.odontologiaintegralfm.infrastructure.scheduler.dto.ScheduleResponseDTO;
import com.odontologiaintegralfm.infrastructure.scheduler.service.IScheduleService;
import com.odontologiaintegralfm.shared.response.Response;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Interfaz que define los métodos para la gestión de configuraciones del sistema.
 * Proporciona métodos para obtener y actualizar configuraciones relacionadas con mensajes,
 * intentos de inicio de sesión y expiración de tokens.
 */
public interface IConfigService {
    /**
     * Obtiene la lista de configuraciones de mensajes.
     * @return Una respuesta que contiene una lista de objetos {@link MessageConfig} con la configuración de mensajes.
     */
    Response<List<MessageConfig>> getMessage();

    /**
     * Actualiza la configuración de un mensaje.
     * @param messageRequestDto El objeto {@link MessageRequestDTO} que contiene los detalles del mensaje a actualizar.
     * @return Una respuesta que contiene el objeto {@link MessageConfig} actualizado.
     */
    Response<MessageConfig> updateMessage(MessageRequestDTO messageRequestDto);









    /**
     * Obtiene todas las parametrizaciones del sistema.
     * @return SystemParameterResponseDTO
     */
    Response<List<SystemParameterResponseDTO>> getSystemParameter();

    /**
     * Actualiza valor de un parámetro del sistema.
     * @param systemParameterRequestDTO
     * @return
     */
    Response<SystemParameterResponseDTO> updateSystemParameter(SystemParameterRequestDTO systemParameterRequestDTO);











    /**
     * Obtiene un listado de todas las tareas programadas con su expresión cron.

     * Este método se comunica con el servicio de {@link IScheduleService}
     * <p>
     * La expresión se representa en el siguiente formato cron de 6 campos:
     * <ul>
     *     <li><b>Segundo</b> (0-59)</li>
     *     <li><b>Minuto</b> (0-59)</li>
     *     <li><b>Hora</b> (0-23)</li>
     *     <li><b>Día del mes</b> (1-31)</li>
     *     <li><b>Mes</b> (1-12 o JAN-DEC)</li>
     *     <li><b>Día de la semana</b> (0-6 o SUN-SAT)</li>
     * </ul>
     * Por ejemplo: {@code "0 0 21 1 * *"} ejecuta la tarea el día 1 de cada mes a las 21:00 hs.
     *
     * @return {@link Response} que contiene la expresión cron actual configurada.
     */
    Response<List<ScheduleResponseDTO>> getAllSchedule();





    /**
     * Actualiza la regla que define cuándo se ejecutará la tarea programada.
     *
     * @param scheduleRequestDTO regla expresada en:
     *                        <ul>
     *                            <li><b>Segundo</b> (0-59)</li>
     *                            <li><b>Minuto</b> (0-59)</li>
     *                            <li><b>Hora</b> (0-23)</li>
     *                            <li><b>Día del mes</b> (1-31)</li>
     *                            <li><b>Mes</b> (1-12 o JAN-DEC)</li>
     *                            <li><b>Día de la semana</b> (0-6 o SUN-SAT)</li>
     *                        </ul>
     *                        Por ejemplo: {@code "0 0 21 1 * *"} ejecuta la tarea el día 1 de cada mes a las 21:00 hs.
     *
     * @return {@link Response}
     */
    Response<ScheduleResponseDTO> updateSchedule(ScheduleRequestDTO scheduleRequestDTO);





    /**
     * Se comunica con el servicio de {@link SystemLogService} para obtener los logs.
     * @param pageValue
     * @param sizeValue
     * @param sortByValue
     * @param directionValue
     * @return
     */
    Response<Page<SystemLogResponseDTO>> getLogs(int pageValue, int sizeValue, String sortByValue, String directionValue);

}
