package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.model.MessageConfig;

import java.util.List;
import java.util.Locale;

/**
 * Interfaz que define los métodos para el servicio de gestión de mensajes.
 * Proporciona métodos para obtener mensajes, listar mensajes y actualizar mensajes.
 */
public interface IMessageService {
    /**
     * Obtiene un mensaje según la clave proporcionada, con posibles parámetros dinámicos.
     * @param clave La clave del mensaje que se desea obtener.
     * @param objects Los objetos que serán utilizados como parámetros dinámicos en el mensaje.
     * @param locale El locale que define el idioma y la región del mensaje.
     * @return El mensaje correspondiente a la clave, formateado con los parámetros proporcionados.
     */
    String getMessage(String clave, Object[] objects, Locale locale);

    /**
     * Obtiene una lista de todos los mensajes configurados.
     * @return Una lista de objetos {@link MessageConfig} que contienen los mensajes configurados.
     */
    List<MessageConfig> listMessage();

    /**
     * Obtiene un mensaje por su ID.
     * @param id El ID del mensaje que se desea obtener.
     * @return El objeto {@link MessageConfig} correspondiente al mensaje con el ID especificado.
     */
    MessageConfig getById(Long id);


    /**
     * Actualiza un mensaje existente.
     *
     * @param message El objeto {@link MessageConfig} que contiene los datos del mensaje a actualizar.
     * @return El objeto {@link MessageConfig} actualizado.
     */
    MessageConfig updateMessage(MessageConfig message);


}
