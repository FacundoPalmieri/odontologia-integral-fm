//package com.odontologiaintegralfm.service;
//
//import com.odontologiaintegralfm.dto.ConsultationCreateRequestDTO;
//import com.odontologiaintegralfm.dto.ConsultationCreateResponseDTO;
//import com.odontologiaintegralfm.dto.Response;
//import com.odontologiaintegralfm.exception.DataBaseException;
//import com.odontologiaintegralfm.service.interfaces.IConsultationService;
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
