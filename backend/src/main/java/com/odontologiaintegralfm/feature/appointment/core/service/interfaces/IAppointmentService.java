package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentCreateResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentRescheduleRequestDTO;
import com.odontologiaintegralfm.shared.response.Response;


public interface IAppointmentService {

    /**
     * Crea un nuevo turno.
     * <p>
     * Aplica las validaciones necesarias según las reglas de negocio
     * (existencia del dentista y paciente, disponibilidad horaria,
     * feriados, bloqueos, y conflicto de turnos).
     * </p>
     * @param appointmentCreateRequestDTO datos necesarios para la creación del turno (dentista, paciente y fecha-hora).
     */
    Response<AppointmentCreateResponseDTO> create(AppointmentCreateRequestDTO appointmentCreateRequestDTO);


    Response<AppointmentCreateResponseDTO> reschedule(Long idAppointment, AppointmentRescheduleRequestDTO appointmentRescheduleRequestDTO);



}
