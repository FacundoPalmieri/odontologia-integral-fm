package com.odontologiaintegralfm.service;


import com.odontologiaintegralfm.dto.FailedLoginAttemptsDTO;
import com.odontologiaintegralfm.dto.MessageDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.dto.TokenConfigDTO;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.MessageConfig;
import com.odontologiaintegralfm.service.interfaces.IConfigService;
import com.odontologiaintegralfm.service.interfaces.IFailedLoginAttemptsService;
import com.odontologiaintegralfm.service.interfaces.IMessageService;
import com.odontologiaintegralfm.service.interfaces.ITokenService;
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
 * y {@link ITokenService} para interactuar con las configuraciones subyacentes, construyendo respuestas que incluyen mensajes
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
    private ITokenService tokenService;


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
     * Este método valida la existencia de un mensaje a partir del ID proporcionado en el DTO {@link MessageDTO},
     * luego actualiza la configuración de ese mensaje en la base de datos. Si la actualización es exitosa,
     * se construye un mensaje para el usuario y se devuelve junto con el mensaje actualizado.
     *</p>
     * @param messageDto El DTO que contiene la información para actualizar el mensaje.
     * @return Una respuesta que contiene el mensaje actualizado y un mensaje de éxito para el usuario.
     * @throws DataBaseException Si ocurre un error al acceder a la base de datos o durante la transacción.
     */
    @Override
    @Transactional
    public Response<MessageConfig> updateMessage(MessageDTO messageDto) {
        try{
            // valída y Obtiene mensaje de BD
            MessageConfig message = messageService.getById(messageDto.id());

            //Actualiza campo
            message = messageService.updateMessage(message);

            //Prepara y envía respuesta.
            String userMessage = messageService.getMessage("config.updateMessage.ok",null,LocaleContextHolder.getLocale());

            return new Response<>(true, userMessage,message);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "configRepository",messageDto.id(), "", "updateMessage");
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
     * @param failedLoginAttemptsDTO El DTO que contiene el nuevo valor de intentos fallidos a actualizar.
     * @return Una respuesta que contiene el número actualizado de intentos fallidos y un mensaje de éxito para el usuario.
     */
    @Override
    public Response<Integer> updateAttempts(FailedLoginAttemptsDTO failedLoginAttemptsDTO) {

        //Actualiza valor
        failedLoginAttemptsService.update(failedLoginAttemptsDTO.value());

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
     * utilizando el servicio {@link ITokenService}, la convierte a minutos y construye
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
     * actualiza el valor de expiración utilizando el servicio {@link ITokenService},
     * recupera el valor actualizado, lo convierte de nuevo a minutos y construye
     * un mensaje para el usuario. Luego retorna una respuesta con el valor actualizado
     * de la expiración en minutos y el mensaje correspondiente.
     *</p>
     * @param tokenConfigDTO Objeto que contiene el valor de la expiración en minutos a actualizar.
     * @return Una respuesta que contiene el valor actualizado de la expiración del token en minutos y un mensaje de éxito para el usuario.
     */
    @Override
    public Response<Long> updateTokenExpiration(TokenConfigDTO tokenConfigDTO) {

        //Convertir minutos a milisegundos
        Long milliseconds = (tokenConfigDTO.expiration() * 60) * 1000;

        //Actualizar tiempo de expiración.
        tokenService.updateExpiration(milliseconds);

        //Recuperar valor actualizado y convertirlo a minutos
        Long expiration = tokenService.getExpiration();
        Long expirationMinutes = (expiration / 1000) / 60;

        //Se construye Mensaje para usuario.
        String userMessage = messageService.getMessage("config.updateExpirationToken.ok", new Object[]{expirationMinutes}, LocaleContextHolder.getLocale());
        return new Response<>(true, userMessage,expirationMinutes);
    }


}
