package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.MedicalHistoryObservation;
import com.odontologiaintegralfm.repository.IMedicalHistoryObservationRepository;
import com.odontologiaintegralfm.service.interfaces.IMedicalHistoryObservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

@Service
public class MedicalHistoryObservationService implements IMedicalHistoryObservationService {
    @Autowired
    private IMedicalHistoryObservationRepository medicalHistoryObservationRepository;


    /**
     * Método para crear una observación en la historia clínica.
     *
     * @param medicalHistoryObservation con toda la información de la observación.
     * @return MedicalHistoryObservation con el objeto creado.
     */
    @Override
    public MedicalHistoryObservation create(MedicalHistoryObservation medicalHistoryObservation) {
         try{
             return medicalHistoryObservationRepository.save(medicalHistoryObservation);
         }catch (DataIntegrityViolationException | CannotCreateTransactionException e){
             throw new DataBaseException(e,"MedicalHistoryObservationService",null,medicalHistoryObservation.getMedicalHistory().getPatient().getLastName() + " " + medicalHistoryObservation.getMedicalHistory().getPatient().getFirstName() , "create");
         }
    }
}
