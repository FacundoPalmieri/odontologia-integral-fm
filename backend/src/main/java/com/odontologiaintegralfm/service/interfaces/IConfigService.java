package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.*;
import com.odontologiaintegralfm.model.MessageConfig;

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
     * Obtiene el número de intentos fallidos de inicio de sesión permitidos.
     * @return Una respuesta que contiene el número máximo de intentos de inicio de sesión.
     */
    Response<Integer> getAttempts();

    /**
     * Actualiza el número de intentos fallidos de inicio de sesión permitidos.
     *
     * @param failedLoginAttemptsRequestDTO El objeto {@link FailedLoginAttemptsRequestDTO} que contiene la nueva configuración de intentos fallidos.
     * @return Una respuesta que contiene el número actualizado de intentos permitidos.
     */
    Response<Integer> updateAttempts(FailedLoginAttemptsRequestDTO failedLoginAttemptsRequestDTO);

    /**
     * Obtiene la duración en minutos del tiempo de expiración del token de autenticación.
     * @return Una respuesta que contiene la duración del token en minutos.
     */
    Response<Long> getTokenExpiration();

    /**
     * Actualiza la duración de expiración del token de autenticación.
     *
     * @param tokenConfigRequestDTO El objeto {@link TokenConfigRequestDTO} que contiene la nueva configuración de expiración del token.
     * @return Una respuesta que contiene el nuevo valor de expiración del token en minutos.
     */
    Response<Long> updateTokenExpiration(TokenConfigRequestDTO tokenConfigRequestDTO);



    /**
     * Obtiene la duración en días del tiempo de expiración del Refresh Token
     * @return Una respuesta que contiene la duración del token en días
     */
    Response<Long> getRefreshTokenExpiration();


    /**
     * Actualiza la duración de expiración del Refresh Token
     * @param refreshTokenConfigRequestDTO
     * @return Una respuesta que contiene el nuevo valor de expiración del token en días.
     */
    Response<Long> updateRefreshTokenExpiration(RefreshTokenConfigRequestDTO refreshTokenConfigRequestDTO);

    /**
     * Obtiene la regla que define cuándo se ejecutará la tarea programada.
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
    Response<String> getSchedule();


    /**
     * Actualiza la regla que define cuándo se ejecutará la tarea programada.
     *
     * @param scheduleConfigRequestDTO regla expresada en:
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
    Response<String> updateSchedule(ScheduleConfigRequestDTO scheduleConfigRequestDTO);


}
