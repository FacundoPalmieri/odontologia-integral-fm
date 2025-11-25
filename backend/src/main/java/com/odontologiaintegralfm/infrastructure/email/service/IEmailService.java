package com.odontologiaintegralfm.infrastructure.email.service;

import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Map;

/** Interfaz que proporciona un método para enviar un correo electrónico a un destinatario con un asunto y cuerpo especificados.*/
public interface IEmailService {

    /**
     * Envía un correo electrónico a múltiples destinatarios.
     * @param to La dirección de correo electrónico del destinatario.
     * @param subject El asunto del correo electrónico.
     * @param body El cuerpo del correo electrónico.
     */
    void sendEmail(List<String> to, String subject, String body);


    /**
     * Envía un correo electrónico a un solo destinatario.
     * @param to La dirección de correo electrónico del destinatario.
     * @param subject El asunto del correo electrónico.
     * @param body El cuerpo del correo electrónico.
     */
    void sendEmail(String to, String subject, String body);


    void sendTemplateEmail(List <String> to, String subject, Map<String, Object> variables);



}
