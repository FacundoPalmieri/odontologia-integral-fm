package com.odontologiaintegralfm.feature.appointmentscheduling.appointment.service;

import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.dto.*;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.enums.AppointmentStatus;
import com.odontologiaintegralfm.feature.appointmentscheduling.appointment.model.Appointment;
import com.odontologiaintegralfm.shared.dto.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


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



    /**
     * Cancela todos los turnos para un dentista en una fecha determinada.
     *
     * <p>Este método se utiliza ante situaciones excepcionales (ej.: una urgencia o
     * imprevisto del profesional) donde el dentista no puede atender en toda la jornada
     * seleccionada. Se actualiza el estado de todos los turnos a CANCELED,
     * se registra su historial y se notifica a los pacientes por email.</p>
     *
     * @param idDentist ID del dentista cuyos turnos se deben cancelar.
     * @param date Fecha en la cual se deben cancelar los turnos. Debe ser posterior al día actual.
     * @param appointmentCancelRequestDTO Motivo, observación y fuente de la cancelación.

     */
    Response<Integer> cancelAllByDate (Long idDentist ,LocalDate date, AppointmentCancelAllRequestDTO appointmentCancelRequestDTO);



    /**
     * Obtiene la lista de turnos en estado RESERVADO de un día para un dentista específico.
     * @param idDentist : id Dentista.
     * @param date : Fecha
     */
    List<Appointment> getAppointmentByDentistAndDate(Long idDentist, LocalDate date, AppointmentStatus status);


    /**
     * Obtiene turnos de un dentista en estado Reservado en un período específico.
     * @param idDentist : id Dentista
     * @param start : Inicio del período
     * @param end : Fin del período
     * @param status : Estado del turno.
     * @return : Map Fecha -> Turno
     */
    Map<LocalDate, List<Appointment>> getByDateRange(Long idDentist, LocalDateTime start, LocalDateTime end, AppointmentStatus status);


    /**
     * Obtiene la información de un turno. En caso de no encontrarlo arroja exception.
     * @param idAppointment : id del turno
     */
    Appointment getById(Long idAppointment);

}
