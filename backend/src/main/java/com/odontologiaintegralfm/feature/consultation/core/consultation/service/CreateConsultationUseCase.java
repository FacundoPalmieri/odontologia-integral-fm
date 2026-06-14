package com.odontologiaintegralfm.feature.consultation.core.consultation.service;

import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service.AppointmentQueryService;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.ConsultationStatusHistory;
import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationRepository;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class CreateConsultationUseCase {

    private final AppointmentQueryService appointmentQueryService;
    private final ChangeConsultationStatusUseCase changeStatusUseCase;
    private final MessageSource messageSource;
    private final IConsultationRepository consultationRepository;


    public CreateConsultationUseCase(AppointmentQueryService appointmentQueryService,
                               ChangeConsultationStatusUseCase changeConsultationStatusUseCase,
                               @Qualifier("messageSource") MessageSource messageSource,
                                     IConsultationRepository consultationRepository) {
        this.appointmentQueryService = appointmentQueryService;
        this.changeStatusUseCase = changeConsultationStatusUseCase;
        this.messageSource = messageSource;
        this.consultationRepository = consultationRepository;
    }



    /**
     * Crea una nueva consulta a partir de un turno existente.
     * <p>
     * Este método implementa el flujo principal de admisión del paciente:
     * <ul>
     *     <li>Valida que el turno exista.</li>
     *     <li>Verifica que el turno corresponda al día actual.</li>
     *     <li>Construye una nueva {@link Consultation} en estado {@code WAITING_ROOM} utilizando factory.</li>
     *     <li>Persiste la consulta recién creada.</li>
     *     <li>Registra la entrada inicial en el historial de estados mediante {@link ConsultationStatusHistory#build}.</li>
     *     <li>Retorna información relevante al cliente mediante un DTO simplificado.</li>
     * </ul>
     *
     * Además, genera un log estructurado mediante la anotación {@link LogAction},
     * permitiendo auditoría y trazabilidad del evento.
     *
     * @param idAppointment ID del turno sobre el cual se crea la consulta.
     * @return {@link Response} conteniendo un {@link ConsultationResponseDTO}
     * con la información básica de la consulta recién creada.
     * @throws ConflictException si el turno no corresponde al día de la fecha.
     */

    @LogAction(
            value = "createConsultationUseCase.logAction.create.ok",
            args = {"#idAppointment", "#result.data.patientName", "#result.data.dentistName"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO

    )
    public Response<ConsultationResponseDTO> execute(Long idAppointment) {

        //Buscamos y validamos la existencia del turno.
        Appointment appointment = appointmentQueryService.getById(idAppointment);

        //Validamos que el turno corresponde al día de la fecha.
        if (!appointment.getDate().toLocalDate().equals(LocalDate.now())) {
            throw new ConflictException("exception.appointmentNotEqualsNow.user", null, "exception.appointmentNotEqualsNow.log", new Object[]{idAppointment, appointment.getDate(), "createConsultationUseCase", "execute"}, LogLevel.ERROR);
        }

        //Validamos que no exista otra consulta para ese turno
        if(consultationRepository.existsByAppointmentId(idAppointment)) {
            throw new ConflictException("exception.createConsultationUseCase.duplicateAppointment.user",null,"exception.createConsultationUseCase.duplicateAppointment.log",new Object[]{idAppointment,"createConsultationUseCase", "execute"}, LogLevel.ERROR);
        }



        //Creamos la consulta
        Consultation consultation  = Consultation.build(appointment);


        //Actualiza la consulta + crea historial + envía webSocket.
        //Si bien el estado es el mismo que al crear, se reutiliza para creár historial y webSocket
        ConsultationResponseDTO consultationResponseDTO =  changeStatusUseCase.execute(consultation, consultation.getStatus());


        return new Response<>(
                true,
                messageSource.getMessage("createConsultationUseCase.create.ok", null, LocaleContextHolder.getLocale()),
                consultationResponseDTO
        );
    }
}
