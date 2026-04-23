package com.odontologiaintegralfm.feature.consultation.core.consultation.service;

import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationRepository;
import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.consultation.enums.ConsultationStatusType;
import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class CallPatientUseCase {

    private final IConsultationRepository consultationRepository;
    private final ChangeConsultationStatusUseCase changeConsultationStatusUseCase;
    private final MessageSource messageSource;


    public CallPatientUseCase(IConsultationRepository consultationRepository,
                              ChangeConsultationStatusUseCase changeConsultationStatusUseCase,
                              @Qualifier("messageSource") MessageSource messageSource){
        this.consultationRepository = consultationRepository;
        this.changeConsultationStatusUseCase = changeConsultationStatusUseCase;
        this.messageSource = messageSource;
    }



    /**
     * Actualiza el estado de una consulta odontológica aplicando las reglas de negocio correspondientes.
     * <p>
     * Reglas principales:
     * <ul>
     *   <li>No se permite modificar una consulta que ya se encuentre finalizada.</li>
     *   <li>Si la actualización no es una corrección, el nuevo estado debe respetar la progresión
     *       válida de estados definida para la consulta.</li>
     *   <li>Si la actualización corresponde a una corrección, se registra un evento de corrección
     *       asociado a la consulta, incluyendo el usuario autenticado.</li>
     * </ul>
     * <p>
     * La operación es transaccional y actualiza información de auditoría
     * (usuario y fecha de última modificación).
     *
     * @param idConsultation identificador único de la consulta a actualizar
     * @return {@link Response} con la información actualizada de la consulta
     *
     * @throws ConflictException si la consulta ya se encuentra finalizada o si el cambio
     *                           de estado no es válido según las reglas de negocio
     * @throws DataBaseException si ocurre un error al persistir los cambios en la base de datos
     */
    @Transactional
    @LogAction(
            value = "callPatientUseCase.logAction.execute.ok",
            args = {"#idConsultation", "#result.data.patientName", "#result.data.dentistName", "#result.data.consultationStatus"},
            type = LogType.SYSTEM,
            level = LogLevel.INFO

    )
    public Response<ConsultationResponseDTO> execute(Long idConsultation) {

        // Recuperamos la consulta.
        Optional<Consultation> consultationOptional = consultationRepository.findById(idConsultation);
        if(consultationOptional.isEmpty()){
            throw new NotFoundException("exception.consultation.notFound.user", null, "exception.consultation.notFound.log", new Object[]{idConsultation, "CallPatientUseCase", "execute"}, LogLevel.ERROR);
        }

        Consultation consultation = consultationOptional.get();

        //Validamos que la consulta no se encuentre finalizada.
        if (consultation.getStatus() == ConsultationStatusType.FINISHED) {
            throw new ConflictException("exception.consultation.finished.user", null, "exception.consultation.finished.log", new Object[]{idConsultation, consultation.getStatus().toString(), "CallPatientUseCase", "execute"}, LogLevel.ERROR);
        }

        //Validamos que esté en el estado correcto.
        if (consultation.getStatus() != ConsultationStatusType.WAITING_ROOM) {
            throw new ConflictException("exception.consultation.waitingRoom.user", null, "exception.consultation.waitingRoom.log", new Object[]{idConsultation, consultation.getStatus().toString(), "CallPatientUseCase", "execute"}, LogLevel.ERROR);
        }

        //Actualiza la consulta + crea historial + envía webSocket.
        ConsultationResponseDTO consultationResponseDTO = changeConsultationStatusUseCase.execute(consultation, ConsultationStatusType.IN_CONSULTATION);


        return new Response<>(
                true,
                messageSource.getMessage("consultationService.update.ok", null, LocaleContextHolder.getLocale()),
                consultationResponseDTO
        );

    }

}
