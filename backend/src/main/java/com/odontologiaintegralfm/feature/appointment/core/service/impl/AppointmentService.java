package com.odontologiaintegralfm.feature.appointment.core.service.impl;

import com.odontologiaintegralfm.feature.appointment.catalogs.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.model.Appointment;
import com.odontologiaintegralfm.feature.appointment.core.model.AppointmentConflict;
import com.odontologiaintegralfm.feature.appointment.core.repository.IAppointmentRepository;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentConflictService;
import com.odontologiaintegralfm.feature.appointment.core.service.interfaces.IAppointmentService;
import com.odontologiaintegralfm.shared.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AppointmentService implements IAppointmentService {

    @Autowired
    private IAppointmentRepository appointmentRepository;

    @Autowired
    private IAppointmentConflictService appointmentConflictService;

    @Autowired
    private MessageSource messageSource;

    /**
     * Método protegido que sirve para obtener todos los turnos del dentista en estado "Reservado"
     * Se utiliza para validación interna de la aplicación
     */
    protected List<Appointment> getAppointmentsReservedByDentistInternal(Long idDentist) {
        return appointmentRepository.findFutureAppointmentsReservedByDentist(idDentist, LocalDateTime.now(), AppointmentStatus.RESERVED);
    }


    /**
     * Método que lista todos los conflictos del dentista.
     */
    @Override
    public Response<List<AppointmentConflictResponseDTO>> getConflict(Long idDentist) {

        List<AppointmentConflict> appointmentConflicts = appointmentConflictService.getAllNotResolvedByDentist(idDentist);

        if ( appointmentConflicts == null || appointmentConflicts.isEmpty()) {
            String messageUser = messageSource.getMessage("appointmentService.getConflict.empty", null, LocaleContextHolder.getLocale());
            return new Response<>(false, messageUser, Collections.emptyList());
        }

        List<AppointmentConflictResponseDTO> conflicts = appointmentConflicts.stream()
                .map(ac -> new AppointmentConflictResponseDTO(
                        ac.getAppointment().getId(),
                        ac.getAppointment().getDate(),
                        ac.getAppointment().getPatient().getPerson().getLastName() + "," + ac.getAppointment().getPatient().getPerson().getFirstName(),
                        ac.getAppointmentConflictReason().toString(),
                        ac.getAppointmentConflictReason().getLabel()
                ))
                .toList();
        return new Response<>(true, "", conflicts);
    }

}

