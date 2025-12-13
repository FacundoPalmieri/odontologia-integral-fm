package com.odontologiaintegralfm.feature.consultation.core.service.interfaces;


import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationCreateResponseDTO;
import com.odontologiaintegralfm.shared.dto.Response;



public interface IConsultationService {

    /**
     * Crea una nueva consulta
     */
    Response<ConsultationCreateResponseDTO> create(Long idAppointment);
}
