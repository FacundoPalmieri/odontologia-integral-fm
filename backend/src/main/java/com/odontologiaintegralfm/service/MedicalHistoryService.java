package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.MedicalHistory;
import com.odontologiaintegralfm.repository.IMedicalHistoryRepository;
import com.odontologiaintegralfm.service.interfaces.IMedicalHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

/**
 * @author [Facundo Palmieri]
 */
@Service
public class MedicalHistoryService implements IMedicalHistoryService {
    @Autowired
    private IMedicalHistoryRepository medicalHistoryRepository;


    /**
     * @param medicalHistory
     * @return
     */
    @Override
    public MedicalHistory create(MedicalHistory medicalHistory) {
        try{
           return

                   medicalHistoryRepository.save(medicalHistory);
        }catch (DataIntegrityViolationException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"MedicalHistoryService",null, medicalHistory.getPatient().getFirstName() + " " + medicalHistory.getPatient().getLastName(), "create");
        }
    }
}
