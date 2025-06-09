package com.odontologiaintegralfm.service;


import com.odontologiaintegralfm.dto.*;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.MessageConfig;
import com.odontologiaintegralfm.service.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Servicio encargado de gestionar la configuración de la aplicación relacionada con:
 * <ul>
 *     <li>Intentos fallidos de inicio de sesión. </li>
 *     <li>Mensajes configurables </li>
 *     <li>Expiración del token de autenticación. </li>
 * </ul>
 * <p>
 * Este servicio proporciona métodos para obtener y actualizar configuraciones como los intentos fallidos de inicio de sesión,
 * la expiración del token y los mensajes almacenados. Utiliza los servicios {@link IMessageService}, {@link IFailedLoginAttemptsService},
 * , {@link ITokenConfigService}  y  {@link IRefreshTokenConfigService} para interactuar con las configuraciones subyacentes, construyendo respuestas que incluyen mensajes
 * para el usuario y los valores actualizados de las configuraciones.
 * </p>
 */
@Service
public class ConfigService implements IConfigService {

    @Autowired
    private IMessageService messageService;

    @Autowired
    private IFailedLoginAttemptsService failedLoginAttemptsService;

    @Autowired
    private ITokenConfigService tokenService;

    @Autowired
    private IRefreshTokenConfigService refreshTokenConfigService;

    @Autowired
    private IScheduleConfigService scheduleConfigService;


    /**
     * Obtiene un listado de configuraciones de mensajes.
     * <p>Este método recupera una lista de configuraciones de mensajes desde el servicio {@link IMessageService},
     *y construye un mensaje para el usuario usando la clave "config.getMessage.ok".</p>
     * @return Una respuesta que contiene un mensaje de éxito y la lista de configuraciones de mensajes obtenida.
     */
    @Override
    public Response<List<MessageConfig>>getMessage(){

        //Obtiene el listado.
        List<MessageConfig> listMessage = messageService.listMessage();

        //Se construye Mensaje para usuario.
        String userMessage = messageService.getMessage("config.getMessage.ok", null, LocaleContextHolder.getLocale());
        return new Response<>(true, userMessage, listMessage);
    }



    /**
     * Actualiza un mensaje en la base de datos.
     *<p>
     * Este método valida la existencia de un mensaje a partir del ID proporcionado en el DTO {@link MessageRequestDTO},
     * luego actualiza la configuración de ese mensaje en la base de datos. Si la actualización es exitosa,
     * se construye un mensaje para el usuario y se devuelve junto con el mensaje actualizado.
     *</p>
     * @param messageRequestDto El DTO que contiene la información para actualizar el mensaje.
     * @return Una respuesta que contiene el mensaje actualizado y un mensaje de éxito para el usuario.
     * @throws DataBaseException Si ocurre un error al acceder a la base de datos o durante la transacción.
     */
    @Override
    @Transactional
    public Response<MessageConfig> updateMessage(MessageRequestDTO messageRequestDto) {
        try{
            // valída y Obtiene mensaje de BD
            MessageConfig message = messageService.getById(messageRequestDto.id());

            //Actualiza campo
            message.setValue(messageRequestDto.value());

            message  = messageService.updateMessage(message);

            //Prepara y envía respuesta.
            String userMessage = messageService.getMessage("config.updateMessage.ok",null,LocaleContextHolder.getLocale());

            return new Response<>(true, userMessage,message);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "configRepository", messageRequestDto.id(), "", "updateMessage");
        }

    }

    /**
     * Obtiene el número de intentos fallidos de inicio de sesión.
     *<p>
     * Este método consulta el servicio {@link IFailedLoginAttemptsService} para obtener el número actual
     * de intentos fallidos de inicio de sesión. Luego, se construye un mensaje para el usuario y se
     * retorna una respuesta con el valor obtenido y el mensaje correspondiente.
     *</p>
     * @return Una respuesta que contiene el número de intentos fallidos y un mensaje de éxito para el usuario.
     */
    @Override
    public Response<Integer> getAttempts() {

        //Obtiene el valor.
        Integer attempts = failedLoginAttemptsService.get();

        //Se construye Mensaje para usuario.
        String userMessage = messageService.getMessage("config.getAttempts.ok", null, LocaleContextHolder.getLocale());
        return new Response<>(true, userMessage,attempts);

    }



    /**
     * Actualiza el número de intentos fallidos de inicio de sesión.
     *<p>
     * Este método recibe un DTO con el valor a actualizar, utiliza el servicio
     * {@link IFailedLoginAttemptsService} para actualizar el número de intentos fallidos
     * y luego recupera el valor actualizado. Posteriormente, se construye un mensaje para el usuario
     * y se retorna una respuesta con el valor actualizado y el mensaje correspondiente.
     *</p>
     * @param failedLoginAttemptsRequestDTO El DTO que contiene el nuevo valor de intentos fallidos a actualizar.
     * @return Una respuesta que contiene el número actualizado de intentos fallidos y un mensaje de éxito para el usuario.
     */
    @Override
    public Response<Integer> updateAttempts(FailedLoginAttemptsRequestDTO failedLoginAttemptsRequestDTO) {

        //Actualiza valor
        failedLoginAttemptsService.update(failedLoginAttemptsRequestDTO.value());

        //Recupera valor actualizado.
        Integer attempts = failedLoginAttemptsService.get();

        //Se construye Mensaje para usuario.
        String userMessage = messageService.getMessage("config.updateAttempts.ok", new Object[]{attempts}, LocaleContextHolder.getLocale());
        return new Response<>(true, userMessage,attempts);

    }

    /**
     * Obtiene la expiración del token en minutos.
     *<p>
     * Este método obtiene la configuración de expiración del token en milisegundos
     * utilizando el servicio {@link ITokenConfigService}, la convierte a minutos y construye
     * un mensaje para el usuario. Luego retorna una respuesta con el valor de expiración
     * en minutos y el mensaje correspondiente.
     *</p>
     * @return Una respuesta que contiene el valor de la expiración del token en minutos y un mensaje de éxito para el usuario.
     */
    @Override
    public Response<Long> getTokenExpiration() {

        // Obtener el valor en milisegundos
        Long expiration = tokenService.getExpiration();

        //Convertir a minutos
        Long expirationMinutes = (expiration / 1000) / 60;

        //Se construye Mensaje para usuario.
        String userMessage = messageService.getMessage("config.getExpirationToken.ok", null, LocaleContextHolder.getLocale());
        return new Response<>(true, userMessage,expirationMinutes);

    }


    /**
     * Actualiza la expiración del token en minutos.
     *<p>
     * Este método convierte la expiración proporcionada en minutos a milisegundos,
     * actualiza el valor de expiración utilizando el servicio {@link ITokenConfigService},
     * recupera el valor actualizado, lo convierte de nuevo a minutos y construye
     * un mensaje para el usuario. Luego retorna una respuesta con el valor actualizado
     * de la expiración en minutos y el mensaje correspondiente.
     *</p>
     * @param tokenConfigRequestDTO Objeto que contiene el valor de la expiración en minutos a actualizar.
     * @return Una respuesta que contiene el valor actualizado de la expiración del token en minutos y un mensaje de éxito para el usuario.
     */
    @Override
    public Response<Long> updateTokenExpiration(TokenConfigRequestDTO tokenConfigRequestDTO) {

        //Convertir minutos a milisegundos
        Long milliseconds = (tokenConfigRequestDTO.expiration() * 60) * 1000;

        //Actualizar tiempo de expiración.
        int filasAfectadas = tokenService.updateExpiration(milliseconds);

        if(filasAfectadas > 0){

            //Se construye Mensaje para usuario.
            String userMessage = messageService.getMessage("config.updateExpirationToken.ok", new Object[]{tokenConfigRequestDTO.expiration()}, LocaleContextHolder.getLocale());
            return new Response<>(true, userMessage, tokenConfigRequestDTO.expiration());
        }

        throw new NotFoundException("exception.tokenConfigNotFoundException.user",null,"exception.tokenConfigNotFoundException.log",new Object[]{"ConfigService", "updateTokenExpiration"} , LogLevel.ERROR);
    }


    /**
     * Obtiene la expiración del Refresh token en minutos.
     *<p>
     * Este método obtiene la configuración de expiración del Refresh token en días.
     * utilizando el servicio {@link IRefreshTokenConfigService}, obtiene el dato y construye
     * un mensaje para el usuario. Luego retorna una respuesta con el valor de expiración
     * y el mensaje correspondiente.
     *</p>
     * @return Una respuesta que contiene el valor de la expiración del token en días y un mensaje de éxito para el usuario.
     */
    @Override
    public Response<Long> getRefreshTokenExpiration() {

        //Obtiene el valor.
        Long expiration = refreshTokenConfigService.getExpiration();

        //Construye respuesta.
        String userMessege = messageService.getMessage("config.getExpirationRefreshToken.ok", null, LocaleContextHolder.getLocale());
        return new Response<>(true, userMessege,expiration);
    }





    /**
     * Actualiza la expiración del Refresh token en días.
     *<p>
     * Este método actualiza el valor de expiración utilizando el servicio {@link IRefreshTokenConfigService}, y construye
     * un mensaje para el usuario. Luego retorna una respuesta con el valor actualizado
     * de la expiración en días y el mensaje correspondiente.
     *</p>
     * @param refreshTokenConfigRequestDTO Objeto que contiene el valor de la expiración en días a actualizar.
     * @return Una respuesta que contiene el valor actualizado de la expiración del token en días y un mensaje de éxito para el usuario.
     */
    @Override
    public Response<Long> updateRefreshTokenExpiration(RefreshTokenConfigRequestDTO refreshTokenConfigRequestDTO) {

        // Llama al servicio de refresh Token y actualiza el valor
        int filasAfectadas = refreshTokenConfigService.updateExpiration(refreshTokenConfigRequestDTO);

        if(filasAfectadas > 0){
            // Construye respuesta
            String userMessage = messageService.getMessage("config.updateExpirationRefreshToken.ok", new Object[]{refreshTokenConfigRequestDTO.expiration()}, LocaleContextHolder.getLocale());
            return new Response<>(true, userMessage, refreshTokenConfigRequestDTO.expiration());
        }

        throw new NotFoundException("exception.refreshTokenConfigNotFoundException.user",null, "exception.refreshTokenConfigNotFoundException.log",new Object[]{"ConfigService","updateRefreshTokenExpiration"},LogLevel.ERROR);
    }

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
    @Override
    public Response<String> getSchedule() {
       return new Response<>(true, null, scheduleConfigService.getSchedule());
    }




    /**
     * Actualiza la regla que define cuándo se ejecutará la tarea programada.
     *
     * @param scheduleConfigRequestDTO regla expresada en:
     *                       <ul>
     *                           <li><b>Segundo</b> (0-59)</li>
     *                           <li><b>Minuto</b> (0-59)</li>
     *                           <li><b>Hora</b> (0-23)</li>
     *                           <li><b>Día del mes</b> (1-31)</li>
     *                           <li><b>Mes</b> (1-12 o JAN-DEC)</li>
     *                           <li><b>Día de la semana</b> (0-6 o SUN-SAT)</li>
     *                       </ul>
     *                       Por ejemplo: {@code "0 0 21 1 * *"} ejecuta la tarea el día 1 de cada mes a las 21:00 hs.
     * @return {@link Response}
     */
    @Override
    public Response<String> updateSchedule(ScheduleConfigRequestDTO scheduleConfigRequestDTO) {

        int filasAfectadas = scheduleConfigService.updateSchedule(scheduleConfigRequestDTO.cronExpression());

        if(filasAfectadas > 0){
            String userMessage = messageService.getMessage("config.updateSchedule.ok", new Object[]{scheduleConfigRequestDTO.cronExpression()}, LocaleContextHolder.getLocale());
            return new Response<>(true, userMessage,scheduleConfigRequestDTO.cronExpression() );
        }

        throw new NotFoundException("exception.scheduleNotFound.user",null,"exception.scheduleNotFound.log",new Object[]{"ConfigService","updateSchedule"}, LogLevel.ERROR);
    }


}
