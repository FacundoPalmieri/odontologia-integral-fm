package com.odontologiaintegralfm.feature.appointment.core.service.interfaces;


import com.odontologiaintegralfm.feature.appointment.core.dto.AppointmentConflictResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;

import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
public interface IAppointmentService {

    /**
     * Método que lista todos los conflictos del dentista.
     */
    Response<List<AppointmentConflictResponseDTO>> getConflict(Long idDentist);






}
