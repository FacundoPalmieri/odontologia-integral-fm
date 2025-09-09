package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;

import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.feature.appointment.core.dto.WorkingDayDTO;


import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
public interface IAppointmentService {

    /**
     * Método interno de la aplicación para obtener conflictos con turno futuros ante cambios en la jornada laboral de un dentista.
     * Se usa para validaciones.
     * @param idDentist: Id Dentista
     * @return : Turno
     */
   List<AppointmentConflictResponseDTO> getConflict(Long idDentist, List<WorkingDayDTO> days);


}
