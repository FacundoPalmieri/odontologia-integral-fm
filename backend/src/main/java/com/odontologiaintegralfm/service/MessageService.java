package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.MessageConfig;
import com.odontologiaintegralfm.repository.IMessageRepository;
import com.odontologiaintegralfm.service.interfaces.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;


/**
 * Servicio para la gestión de mensajes en la base de datos.
 * <p>
 * Esta clase proporciona métodos para recuperar, actualizar y formatear mensajes almacenados en la base de datos.
 * Interactúa con el repositorio {@link IMessageRepository}.
 * </p>
 * <p>
 * Las operaciones incluyen la obtención de mensajes por clave e idioma, la recuperación de mensajes por ID,
 * la actualización de mensajes y la recuperación de todos los mensajes almacenados. En caso de errores de acceso
 * a la base de datos, se manejan excepciones específicas como {@link DataBaseException} y {@link NotFoundException}.
 * </p>
 */

@Service
public class MessageService implements IMessageService {

    @Autowired
    private IMessageRepository messageRepository;

    /**
     * Obtiene el mensaje desde la base de datos según la clave y el idioma.
     * <p>
     * Este método consulta el repositorio {@link IMessageRepository} para recuperar un mensaje
     * basado en una clave específica y un idioma.
     * </p>
     *
     * @param clave   La clave del mensaje que se desea obtener.
     * @param args    Los objetos que serán utilizados como parámetros dinámicos en el mensaje.
     * @param locale  El locale que define el idioma.
     * @return        El mensaje encontrado; en caso contrario, devuelve "Mensaje no encontrado".
     */
    public String getMessage(String clave,Object[] args, Locale locale) {
        MessageConfig message = messageRepository.findByKeyAndLocale(clave, "es_AR");
        if (message != null) {
            return args == null ? message.getValue() : formatMessage(message.getValue(), args);        }
        return "Mensaje no encontrado";
    }


    /**
     * Formatea un mensaje reemplazando los marcadores de posición con los valores proporcionados.
     * <p>
     * Este método utiliza {@link MessageFormat} para insertar dinámicamente los valores de los
     * argumentos en el mensaje dado.
     * </p>
     *
     * @param message El mensaje base que contiene marcadores de posición.
     * @param args    Los valores que reemplazarán los marcadores de posición en el mensaje.
     * @return        El mensaje formateado con los valores proporcionados.
     */
    private String formatMessage(String message,Object[] args) {
        return MessageFormat.format(message,args);
    }


    /**
     * Recupera la lista de mensajes desde la base de datos.
     * <p>
     * Este método obtiene todos los  mensajes desde la base de datos.
     * En caso de error al acceder a la base de datos, lanza una excepción personalizada.
     * </p>
     *
     * @return Una lista de {@link MessageConfig} con los mensajes almacenados en la base de datos.
     * @throws DataBaseException Si ocurre un error de acceso a la base de datos o de transacción.
     */

    @Override
    public List<MessageConfig> listMessage() {
        try {
            return messageRepository.findAll();
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "MessageService", 0L, "", "listMessage");
        }
    }


    /**
     * Obtiene un mensaje por su identificador único.
     * <p>
     * Este método busca un mensaje en la base de datos mediante su ID. Si el mensaje no existe,
     * lanza una excepción {@link NotFoundException}. En caso de error de acceso a la base
     * de datos, lanza una excepción {@link DataBaseException}.
     * </p>
     *
     * @param id El identificador único del mensaje a buscar.
     * @return El mensaje encontrado como una instancia de {@link MessageConfig}.
     * @throws NotFoundException Si no se encuentra un mensaje con el ID especificado.
     * @throws DataBaseException Si ocurre un error de acceso a la base de datos o de transacción.
     */

    @Override
    public MessageConfig getById(Long id) {
        try{
            return messageRepository.findById(id).orElseThrow(()->new NotFoundException("","exception.messageNotFound.user",null,"exception.messageNotFound.log", id,"-", "MessageService", "getById", LogLevel.INFO));
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "MessageService", 0L, "", "getById");
        }
    }




    /**
     * Actualiza un mensaje en la base de datos.
     * <p>
     * Este método actualiza la información del mensaje en la base de datos.
     * </p>
     *
     * @param message El objeto {@link MessageConfig} con los datos actualizados del mensaje.
     * @return El mensaje actualizado después de ser guardado en la base de datos.
     */

    @Override
    public MessageConfig updateMessage(MessageConfig message) {
        return messageRepository.save(message);
    }


}

