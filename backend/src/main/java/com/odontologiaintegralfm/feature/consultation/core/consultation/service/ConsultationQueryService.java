package com.odontologiaintegralfm.feature.consultation.core.consultation.service;

import com.odontologiaintegralfm.feature.consultation.core.consultation.mapper.ConsultationMapper;
import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.repository.IConsultationRepository;
import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

@Service
public class ConsultationQueryService {

    private final IConsultationRepository consultationRepository;
    private final ConsultationMapper consultationMapper;


    public ConsultationQueryService(IConsultationRepository consultationRepository,
                                    ConsultationMapper consultationMapper) {
        this.consultationRepository = consultationRepository;
        this.consultationMapper = consultationMapper;
    }




    //------------------ Métodos Cliente-------------------------------//
    /**
     * Método que brinda respuesta al controller.
     * Recupera una consulta por su ID, si no existe arroja NotFound exception.
     *
     * @param id : id de la consulta.
     */
    public Response<ConsultationResponseDTO> getById(Long id) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(()->new NotFoundException("exception.consultation.notFound.user", null, "exception.consultation.notFound.log",new Object[]{id, "ConsultationQueryService","getById"}, LogLevel.ERROR));


        return new Response<>(
                true,
                null,
                consultationMapper.toDTO(consultation)
        );
    }





    //------------------ Métodos Internos-------------------------------//

    /**
     * Recupera una consulta por su ID, si no existe arroja NotFound exception.
     *
     * @param id : id de la consulta.
     */
    public Consultation findById(Long id) {
        try{
            return consultationRepository.findById(id)
                    .orElseThrow(()->new NotFoundException("exception.consultation.notFound.user", null, "exception.consultation.notFound.log",new Object[]{id, "ConsultationQueryService","findById"}, LogLevel.ERROR));
        }catch(CannotCreateTransactionException | DataAccessException e){
            throw new DataBaseException(e, "ConsultationService", id, null, "getById");
        }
    }



}
