package com.odontologiaintegralfm.feature.consultation.core.service.impl;


import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentService;
import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationCreateResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.enums.ConsultationStatus;
import com.odontologiaintegralfm.feature.consultation.core.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationHistory;
import com.odontologiaintegralfm.feature.consultation.core.repository.IConsultationHistoryRepository;
import com.odontologiaintegralfm.feature.consultation.core.repository.IConsultationRepository;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.infrastructure.websocket.enums.WebSocketEventType;
import com.odontologiaintegralfm.infrastructure.websocket.service.IWebSocketEventPublisher;
import com.odontologiaintegralfm.infrastructure.websocket.service.WebSocketEventPublisher;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Servicio que se encarga de gestionar las consultas.
 */
@Service
public class ConsultationService implements IConsultationService {

    @Autowired
    private IAppointmentService appointmentService;

    @Autowired
    private IConsultationRepository consultationRepository;

    @Autowired
    private IConsultationHistoryRepository consultationHistoryRepository;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private IWebSocketEventPublisher webSocketEventPublisher;


    /**
     * Crea una nueva consulta a partir de un turno existente.
     * <p>
     * Este método implementa el flujo principal de admisión del paciente:
     * <ul>
     *     <li>Valida que el turno exista.</li>
     *     <li>Verifica que el turno corresponda al día actual.</li>
     *     <li>Construye una nueva {@link Consultation} en estado {@code WAITING_ROOM} utilizando factory.</li>
     *     <li>Persiste la consulta recién creada.</li>
     *     <li>Registra la entrada inicial en el historial de estados mediante {@link ConsultationHistory#build}.</li>
     *     <li>Retorna información relevante al cliente mediante un DTO simplificado.</li>
     * </ul>
     *
     * Además, genera un log estructurado mediante la anotación {@link LogAction},
     * permitiendo auditoría y trazabilidad del evento.
     *
     * @param idAppointment ID del turno sobre el cual se crea la consulta.
     * @return {@link Response} conteniendo un {@link ConsultationCreateResponseDTO}
     * con la información básica de la consulta recién creada.
     * @throws ConflictException si el turno no corresponde al día de la fecha.
     */

    @Override
    @LogAction(
            value = "consultationService.logAction.create.ok",
            args = {"#idAppointment", "#result.data.patientName", "#result.data.dentistName"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO

    )
    public Response<ConsultationCreateResponseDTO> create(Long idAppointment) {

        //Buscamos y validamos la existencia del turno.
        Appointment appointment = appointmentService.getById(idAppointment);

        //Validamos que el turno corresponde al día de la fecha.
        if (!appointment.getDate().toLocalDate().equals(LocalDate.now())) {
            throw new ConflictException("exception.appointmentNotEqualsNow.user", null, "exception.appointmentNotEqualsNow.log", new Object[]{idAppointment, appointment.getDate(), "ConsultationService", "create"}, LogLevel.ERROR);
        }

        //Creamos la consulta
        Consultation consultation  = Consultation.build(appointment);
        Consultation consultationSaved = consultationRepository.save(consultation);

        //Creamos la Historia de la consulta
        ConsultationHistory consultationHistory = ConsultationHistory.build(consultationSaved, authenticatedUserService.getAuthenticatedUser());
        consultationHistoryRepository.save(consultationHistory);

        //Mapea respuesta
        ConsultationCreateResponseDTO consultationCreateResponseDTO = new ConsultationCreateResponseDTO(
                consultationSaved.getId(),
                consultationSaved.getPatient().getPerson().getLastName() + "," + consultationSaved.getPatient().getPerson().getFirstName(),
                consultationSaved.getDentist().getPerson().getLastName() + "," + consultationSaved.getDentist().getPerson().getFirstName(),
                consultationSaved.getStatus().getLabel()
        );

        //Envío webSocket
        webSocketEventPublisher.publish(
                WebSocketEventType.PATIENT_RECEIVED,
                consultationCreateResponseDTO
        );

        return new Response<>(
                true,
                "consultationServices.create.ok",
                consultationCreateResponseDTO
        );
    }
}
