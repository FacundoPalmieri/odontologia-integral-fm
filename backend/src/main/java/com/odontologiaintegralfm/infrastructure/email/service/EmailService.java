package com.odontologiaintegralfm.infrastructure.email.service;


import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
    private JavaMailSender mailSender;

    @Value("${email.template.path}")
    private String templatePath;

    @Value("${email.logo.path}")
    private String logoPath;

    @Value("${email.logo.cid}")
    private String logoCid;

    /**
     * Envía un correo electrónico simple.
     * <p>
     * Este método crea un mensaje de correo utilizando los parámetros proporcionados (destinatario, asunto y cuerpo),
     * y luego lo envía utilizando el {@link JavaMailSender}.
     * </p>
     *
     * @param to      La dirección de correo electrónico del destinatario.
     * @param subject El asunto del correo.
     * @param body    El cuerpo del correo.
     */
    @LogAction(
            value = "emailService.logAction.sendEmail",
            args = {"#to", "#subject", "#body"},
            level = LogLevel.INFO,
            type = LogType.SYSTEM
    )
    @Async("mailExecutor")
    public void sendEmail(List<String> to, String subject, String body) {
        to.forEach(
                destination -> {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(destination);
                    message.setSubject(subject);
                    message.setText(body);
                    mailSender.send(message);
                });
    }


    /**
     * Envía un correo electrónico a un solo destinatario.
     *
     * @param to      La dirección de correo electrónico del destinatario.
     * @param subject El asunto del correo electrónico.
     * @param body    El cuerpo del correo electrónico.
     */
    @Override
    @Async("mailExecutor")
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);

    }

    /**
     * @param to
     * @param subject
     */
    @Override
    @Async("mailExecutor")
    public void sendTemplateEmail(List <String> to, String subject, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setSubject(subject);
            helper.setBcc(to.toArray(new String[0]));



            // Cargar template desde resources
            ClassPathResource resource = new ClassPathResource(templatePath);
            String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);


            // Reemplazar placeholders
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                html = html.replace("{{" + entry.getKey() + "}}", entry.getValue().toString());
            }

            helper.setText(html, true);

            // Logo inline (ruta configurable)
            helper.addInline(
                    logoCid,
                    new ClassPathResource(logoPath)
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error enviando email", e);
        }
    }
}

