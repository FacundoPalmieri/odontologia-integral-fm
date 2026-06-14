package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.TemplateEmail;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.person.core.model.ContactEmail;
import com.odontologiaintegralfm.infrastructure.email.service.IEmailService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@Service
public class AppointmentNotificationService {

    private final IEmailService emailService;
    private final MessageSource messageSource;

    public AppointmentNotificationService(
            IEmailService emailService,
            MessageSource messageSource) {
        this.emailService = emailService;
        this.messageSource = messageSource;
    }

    /**
     * Envía email a todos los contactos asociados al paciente del turno recibido, utilizando un template HTML.
     *
     * <p>Este método construye el correo a partir de la información del turno (`Appointment`):
     * <ul>
     *     <li>Nombre del paciente</li>
     *     <li>Fecha y hora del turno</li>
     *     <li>Profesional asignado</li>
     * </ul>
     * <p>El correo se envía a todos los emails registrados en el paciente
     *
     * <p>Este método encapsula la lógica común utilizada tanto para cancelación,
     * creación o reprogramación de turnos
     *
     * @param appointment         Turno del cual se extraerá la información para completar el template del correo.
     * @param subjectMessageKey   Key del archivo de mensajes para obtener el asunto del correo.
     * @param titleMessageKey     Key del archivo de mensajes para completar el título del template.
     * @param bodyMessageKey      Key del archivo de mensajes para completar el cuerpo principal del correo.
     *
     * @throws org.springframework.context.NoSuchMessageException
     *         Si alguna de las keys provistas no existe en el archivo de mensajes.
     *
     * @implNote Este método no maneja excepciones del envío de correo, dado que el
     *           `emailService.sendTemplateEmail()` es asíncrono por diseño.
     */
    public void sendAppointmentEmail(
            Appointment appointment,
            String subjectMessageKey,
            String titleMessageKey,
            String bodyMessageKey
    ) {

        // Obtener emails destino
        List<String> emails = appointment.getPatient()
                .getPerson()
                .getContactEmails()
                .stream()
                .map(ContactEmail::getEmail)
                .toList();

        // Construir el cuerpo del mail
        Map<String, Object> templateData = Map.of(
                TemplateEmail.title.toString(),
                messageSource.getMessage(titleMessageKey, null, LocaleContextHolder.getLocale()),

                TemplateEmail.patient.toString(),
                appointment.getPatient().getPerson().getFirstName() + ", " +
                        appointment.getPatient().getPerson().getLastName(),

                TemplateEmail.message.toString(),
                messageSource.getMessage(bodyMessageKey, null, LocaleContextHolder.getLocale()),

                TemplateEmail.date.toString(),
                appointment.getDate().toLocalDate()
                        .format(DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy", new Locale("es", "ES"))),

                TemplateEmail.time.toString(),
                appointment.getDate().toLocalTime()
                        .format(DateTimeFormatter.ofPattern("HH:mm")) + " hs",

                TemplateEmail.dentist.toString(),
                appointment.getDentist().getPerson().getLastName() + ", " +
                        appointment.getDentist().getPerson().getFirstName()
        );

        // Envio
        emailService.sendTemplateEmail(
                emails,
                messageSource.getMessage(subjectMessageKey, null, LocaleContextHolder.getLocale()),
                templateData
        );
    }
}
