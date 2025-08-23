package com.odontologiaintegralfm.infrastructure.email.service;

/** Interfaz que proporciona un método para enviar un correo electrónico a un destinatario con un asunto y cuerpo especificados.*/
public interface IEmailService {

    /**
     * Envía un correo electrónico a la dirección proporcionada con el asunto y cuerpo especificados.
     * @param to La dirección de correo electrónico del destinatario.
     * @param subject El asunto del correo electrónico.
     * @param body El cuerpo del correo electrónico.
     */
    void sendEmail(String to, String subject, String body);
}
