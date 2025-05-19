package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.MedicalHistory;
import com.odontologiaintegralfm.model.Patient;
import com.odontologiaintegralfm.repository.IMedicalHistoryRepository;
import com.odontologiaintegralfm.service.interfaces.IMedicalHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.time.LocalDate;

/**
 * @author [Facundo Palmieri]
 */
@Service
public class MedicalHistoryService implements IMedicalHistoryService {
    @Autowired
    private IMedicalHistoryRepository medicalHistoryRepository;


    /**
     * Método para crear una historia clínica y persistirla en la base de datos.
     * @param medicalHistory de la persona.
     * @return {@link MedicalHistory}
     */
    @Override
    public MedicalHistory create(MedicalHistory medicalHistory) {
        try{
           return medicalHistoryRepository.save(medicalHistory);
        }catch (DataIntegrityViolationException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"MedicalHistoryService",null, medicalHistory.getPatient().getFirstName() + " " + medicalHistory.getPatient().getLastName(), "create");
        }
    }



    /**
     * Método para obtener la historia clínica de una paciente por su id.
     * @param idPatient del paciente
     * @return MedicalHistory
     */
    @Override
    public MedicalHistory getByPatient(Long idPatient) {
        try{
            return medicalHistoryRepository.findById(idPatient).orElseThrow(()-> new NotFoundException("exception.medicalHistoryNotFound.user", null, "exception.medicalHistoryNotFound.log", new Object[]{idPatient, "MedicalHistoryService","getByPatientId"}, LogLevel.ERROR));
        }catch (DataIntegrityViolationException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"MedicalHistoryService",idPatient, null, "getByPatientId");
        }
    }



    /**
     * Método que construye un objeto {@link MedicalHistory}.
     * @param patient con los datos personales del paciente (incluye ID)
     * @return MedicalHistory con la historia clínica creada.
     */
    @Override
    public MedicalHistory buildMedicalHistory(Patient patient) {
        MedicalHistory medicalHistory = new MedicalHistory();
        medicalHistory.setPatient(patient);
        medicalHistory.setDateTime(LocalDate.now());
        medicalHistory.setEnabled(true);
        return medicalHistory;
    }
}
