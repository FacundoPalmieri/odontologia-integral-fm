package com.odontologiaintegralfm.feature.consultation.core.service.interfaces;


import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationCorrectionRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationResponseDTO;
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
     * Actualiza el estado de una consulta de manera interna por flujo normal
     *
     * @param idConsultation : id Consulta
     */
    Response<ConsultationResponseDTO> updateStatus(Long idConsultation);



    /**
     * Revierte el estado de una consulta al estado anterior.
     *
     * @param idConsultation : id Consulta
     */
    Response<ConsultationResponseDTO> updateCorrectionStatus(Long idConsultation, ConsultationCorrectionRequestDTO correction);


    /**
     * Elimina una consulta.
     *
     * @param idConsultation : id Consulta
     */
    Response<Void> disabled(Long idConsultation, String observation);

}
