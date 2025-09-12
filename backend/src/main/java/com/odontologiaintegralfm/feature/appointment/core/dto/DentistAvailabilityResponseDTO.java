package com.odontologiaintegralfm.feature.appointment.core.dto;


import java.util.List;

/**
 *  DTO que representa los conflictos con turnos al momento de que el dentista actualiza la configuración de la jornada laboral.
 *  Es hijo de {@link DentistAvailabilityResponseDTO}
 */
public record DentistAvailabilityResponseDTO (
        Long idDentist,
        List<WorkingDayDTO> days,
        List<AppointmentConflictResponseDTO> appointmentConflict
){
}
