package com.odontologiaintegralfm.feature.consultation.core.service.interfaces;


import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationUpdateRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.model.Consultation;
import com.odontologiaintegralfm.shared.dto.Response;



public interface IConsultationService {

    /**
     * Crea una nueva consulta
     */
    Response<ConsultationResponseDTO> create(Long idAppointment);

    /**
     * Recupera una consulta por su ID, si no existe arroja NotFound exception.
     * @param id : id de la consulta.
     */
    Consultation getById(Long id);



    /**
     * Actualiza una consulta, sin eventos de corrección, de manera interna por flujo normal
     *
     * @param idConsultation : id Consulta
     * @param update : Estado nuevo de la consulta
     */
    Response<ConsultationResponseDTO> updateStatus(Long idConsultation,ConsultationUpdateRequestDTO update);


}
