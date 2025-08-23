package com.odontologiaintegralfm.infrastructure.email.service;

import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
/**
 * Servicio encargado del envío de correos electrónicos.
 * <p>
 * Este servicio utiliza {@link JavaMailSender} para enviar correos electrónicos simples con un destinatario, un asunto y un cuerpo.
 * El método {@link #sendEmail(String, String, String)} permite configurar los parámetros del correo y enviarlo a través del servicio de correo.
 * </p>
 */
@Service
public class EmailService implements IEmailService {

    @Autowired
    private JavaMailSender  mailSender;

    /**
     * Envía un correo electrónico simple.
     * <p>
     * Este método crea un mensaje de correo utilizando los parámetros proporcionados (destinatario, asunto y cuerpo),
     * y luego lo envía utilizando el {@link JavaMailSender}.
     * </p>
     * @param to La dirección de correo electrónico del destinatario.
     * @param subject El asunto del correo.
     * @param body El cuerpo del correo.
     */
    @LogAction(
            value = "emailService.systemLogService.sendEmail",
            args = {"#to", "#subject", "#body"},
            level = LogLevel.INFO,
            type = LogType.SYSTEM
    )
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
