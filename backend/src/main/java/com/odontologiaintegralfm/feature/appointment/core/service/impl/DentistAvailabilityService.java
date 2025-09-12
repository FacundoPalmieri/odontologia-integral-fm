package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistAvailabilityRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.DentistAvailabilityResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.WorkingDayDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.DentistAvailability;
import com.odontologiaintegralfm.feature.appointment.core.repository.IDentistAvailabilityRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IDentistAvailabilityService;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import com.odontologiaintegralfm.feature.dentist.core.service.interfaces.IDentistService;
import com.odontologiaintegralfm.infrastructure.message.service.implement.MessageService;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.shared.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
@Service
public class DentistAvailabilityService implements IDentistAvailabilityService {

    @Autowired
    private IDentistService dentistService;

    @Autowired
    private IDentistAvailabilityRepository dentistAvailabilityRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private AppointmentService appointmentService;


    /**
     * Método para crear/Actualizar la disponibilidad de turnos de un dentista.
     * - Inicio de jornada.
     * - Fin de jornada.
     * - Duración de turno.
     *
     * @param dentistAvailabilityRequestDTO
     * @return
     */
    @Override
    public Response<DentistAvailabilityResponseDTO> update(DentistAvailabilityRequestDTO dentistAvailabilityRequestDTO) {
        try {
            //Valida que exista dentista
            Dentist dentist = dentistService.getById(dentistAvailabilityRequestDTO.idDentist())
                    .orElseThrow(() -> new ConflictException("exception.dentistNotFound.user", null, "exception.dentistNotFound.log", new Object[]{dentistAvailabilityRequestDTO.idDentist(), "Dentist Availability Service", "update"}, LogLevel.ERROR));


            //Buscar si existe relación:
            List<DentistAvailability> dentistAvailabilityExisting = dentistAvailabilityRepository.findAllByDentistId(dentistAvailabilityRequestDTO.idDentist());

            //Si la lista NO está vacía, existe relación previa entre dentista y disponibilidad entonces se limpia en la base.
            if (!dentistAvailabilityExisting.isEmpty()) {
                dentistAvailabilityRepository.deleteAll(dentistAvailabilityExisting);
            }

            //Se persiste la nueva relación.
            List<DentistAvailability> newAvailabilities = dentistAvailabilityRequestDTO.days().stream()
                    .map(dto -> new DentistAvailability(
                            dentist,
                            dto.dayName(),
                            dto.startTime(),
                            dto.endTime(),
                            dto.appointmentDuration(),
                            LocalDateTime.now(),
                            authenticatedUserService.getAuthenticatedUser(),
                            true
                    ))
                    .toList();

            List<DentistAvailability> dentistAvailabilitiesSaved = dentistAvailabilityRepository.saveAll(newAvailabilities);

            //Llamar al servicio de turnos para verificar si hay turnos existentes que se vean afectados.
            List<AppointmentConflictResponseDTO> appointments = appointmentService.getConflict(dentistAvailabilityRequestDTO.idDentist(), dentistAvailabilityRequestDTO.days());


            // Se arma la respuesta final.
            DentistAvailabilityResponseDTO dentistAvailabilityResponseDTO = new DentistAvailabilityResponseDTO(
                    dentistAvailabilitiesSaved.get(0).getDentist().getId(),
                    dentistAvailabilitiesSaved
                            .stream()
                            .map(da -> new WorkingDayDTO(
                                    da.getKeyName(),
                                    da.getStartTime(),
                                    da.getEndTime(),
                                    da.getAppointmentDuration()
                            ))
                            .toList(),
                    appointments
            );

            String messageUser = messageService.getMessage("dentistAvailabilityService.update.ok", null, LocaleContextHolder.getLocale());

            return new Response<>(true, messageUser, dentistAvailabilityResponseDTO);
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistAvailabilityService", dentistAvailabilityRequestDTO.idDentist(), null, "update");
        }
    }

    /**
     * Método para obtener la disponibilidad de un dentista.
     * - Inicio de jornada.
     * - Fin de jornada.
     * - Duración de turno.
     *
     * @param id : Id del dentista
     */
    @Override
    public Response<DentistAvailabilityResponseDTO> get(Long id) {
        try {


            //Valida que exista dentista
            Dentist dentist = dentistService.getById(id)
                    .orElseThrow(() -> new ConflictException("exception.dentistNotFound.user", null, "exception.dentistNotFound.log", new Object[]{id, "Dentist Availability Service", "update"}, LogLevel.ERROR));

            //Buscar si existe relación:
            List<DentistAvailability> dentistAvailability= dentistAvailabilityRepository.findAllByDentistId(id);

            if(dentistAvailability.isEmpty()) {
                throw new NotFoundException("dentistAvailabilityService.notFound.user",null,"dentistAvailabilityService.notFound.log",new Object[]{id, "Dentist Availability Service", "get"}, LogLevel.WARN);
            }

            //Mapeo a un DTO
            DentistAvailabilityResponseDTO dentistAvailabilityResponseDTO = new DentistAvailabilityResponseDTO(
                    dentistAvailability.get(0).getDentist().getId(),
                    dentistAvailability
                            .stream()
                            .map(da -> new WorkingDayDTO(
                                    da.getKeyName(),
                                    da.getStartTime(),
                                    da.getEndTime(),
                                    da.getAppointmentDuration()
                            ))
                            .toList(),
                    null
            );

            return new Response<>(true, null, dentistAvailabilityResponseDTO);

        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DentistAvailabilityService", id, null, "get");
        }
    }



}
