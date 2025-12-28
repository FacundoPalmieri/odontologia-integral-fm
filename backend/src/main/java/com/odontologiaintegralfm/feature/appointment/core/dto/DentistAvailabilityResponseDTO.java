package com.odontologiaintegralfm.feature.appointment.core.dto;


import com.odontologiaintegralfm.feature.appointment.core.model.DentistAvailability;

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

    public static DentistAvailabilityResponseDTO build(List<DentistAvailability> availabilities, List<AppointmentConflictResponseDTO> conflicts){
        return new DentistAvailabilityResponseDTO(
                availabilities.get(0).getDentist().getId(),
                availabilities
                        .stream()
                        .map(WorkingDayDTO::from)
                        .toList(),
                conflicts
        );
    }

}
