package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentCancelRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentCreateRequestDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentRescheduleRequestDTO;
import com.odontologiaintegralfm.shared.response.Response;

import java.time.LocalDate;


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
    Response<AppointmentResponseDTO> create(AppointmentCreateRequestDTO appointmentCreateRequestDTO);




    /**
     * Reprograma un turno existente.
     * <p>
     * Verifica que el turno exista, que esté en un estado que permita
     * reprogramación, que se cumpla el tiempo mínimo según quién solicita el cambio,
     * y que la nueva fecha no genere conflictos de agenda (turnos, bloqueos,
     * feriados o jornada del dentista). Luego actualiza la fecha y registra el
     * cambio en el historial.
     *
     * @param idAppointment ID del turno a reprogramar.
     * @param appointmentRescheduleRequestDTO Datos de la nueva fecha, observación y origen de la solicitud.
     * @return Un {@link Response} con el turno actualizado luego de la reprogramación.
     */
    Response<AppointmentResponseDTO> reschedule(Long idAppointment, AppointmentRescheduleRequestDTO appointmentRescheduleRequestDTO);



    /**
     * Cancela un turno verificando que esté en un estado válido para cancelación y
     * que se cumpla el tiempo mínimo según quién solicite la acción.
     * <p>
     * La fecha del turno no se modifica: únicamente cambia su estado a CANCELADO
     * y se registra el evento en el historial.
     *
     * @param idAppointment ID del turno a cancelar.
     * @param appointmentCancelRequestDTO Datos complementarios de la cancelación,
     *                                    como observación y origen de la solicitud.
     * @return Un {@link Response} con el turno ya marcado como cancelado.
     */
    Response<AppointmentResponseDTO> cancel(Long idAppointment, AppointmentCancelRequestDTO appointmentCancelRequestDTO);


    Response<Integer> cancelAllByDate (Long idDentist ,LocalDate date, AppointmentCancelRequestDTO appointmentCancelRequestDTO);

}
