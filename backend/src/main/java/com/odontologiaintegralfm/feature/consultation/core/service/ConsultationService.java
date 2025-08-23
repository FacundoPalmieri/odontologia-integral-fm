//package com.odontologiaintegralfm.service;
//
//import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationCreateRequestDTO;
//import com.odontologiaintegralfm.feature.consultation.core.dto.ConsultationCreateResponseDTO;
//import com.odontologiaintegralfm.shared.response.Response;
//import com.odontologiaintegralfm.shared.exception.DataBaseException;
//import com.odontologiaintegralfm.feature.patient.catalogs.service.implement.interfaces.IConsultationService;
//import org.springframework.dao.DataAccessException;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.CannotCreateTransactionException;
//
///**
// * Servicio que se encarga de gestionar las consultas.
// */
//@Service
//public class ConsultationService implements IConsultationService {
//
//
//    /**
//     * Crea una nueva consulta
//     *
//     * @param consultationCreateRequestDTO
//     */
//    @Override
//    public Response<ConsultationCreateResponseDTO> create(ConsultationCreateRequestDTO consultationCreateRequestDTO) {
//        try{
//
//
//        }catch(DataAccessException | CannotCreateTransactionException e){
//            throw new DataBaseException(e, "ConsultationService", null,null , "create");
//
//        }
//    }
//}
