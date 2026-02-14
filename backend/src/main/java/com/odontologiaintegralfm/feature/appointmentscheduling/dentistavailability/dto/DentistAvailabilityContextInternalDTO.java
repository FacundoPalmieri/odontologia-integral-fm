package com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.dto;

import com.odontologiaintegralfm.feature.appointmentscheduling.dentistavailability.model.DentistAvailability;
import com.odontologiaintegralfm.feature.dentist.core.model.Dentist;
import java.util.List;

/**
 * DTO interno del servicio DentistAvailability.
 * Se utiliza para preparar el contexto de validación de dentist y su jornada actual,
 * antes de Crear una nueva, o hacer un preview de los posibles conflictos ante la intención de actualizar la misma.
 */
public record DentistAvailabilityContextInternalDTO(
        Dentist dentist,
        List<DentistAvailability> dentistAvailabilities
) {

    public static DentistAvailabilityContextInternalDTO build (Dentist dentist, List<DentistAvailability> dentistAvailabilities) {
        return new DentistAvailabilityContextInternalDTO(
                dentist,
                dentistAvailabilities
        );
    }
}
