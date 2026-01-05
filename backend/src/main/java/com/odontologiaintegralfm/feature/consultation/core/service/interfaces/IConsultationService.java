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
     * Método interno de la aplicación
     * Recupera una consulta por su ID, si no existe arroja NotFound exception.
     * @param id : id de la consulta.
     */
    Consultation getByIdInternal(Long id);


    /**
     * Método que brinda respuesta al controller.
     * Recupera una consulta por su ID, si no existe arroja NotFound exception.
     * @param id : id de la consulta.
     */
    Response<ConsultationResponseDTO> getById(Long id);



    /**
     * Actualiza el estado de una consulta.
     *
     * @param idConsultation : id Consulta
     */
    Response<ConsultationResponseDTO> callPatient(Long idConsultation);



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
