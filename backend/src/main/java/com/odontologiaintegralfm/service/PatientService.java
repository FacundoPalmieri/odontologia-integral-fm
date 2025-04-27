package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.PatientCreateRequestDTO;
import com.odontologiaintegralfm.dto.PatientCreateResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.Patient;
import com.odontologiaintegralfm.repository.IPatientRepository;
import com.odontologiaintegralfm.service.interfaces.IPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author [Facundo Palmieri]
 */
/*
@Service
public class PatientService implements IPatientService {
    @Autowired
    private IPatientRepository patientRepository;

    /**
     * Método para crear un paciente
     *
     * @param patientCreateRequestDTO El objeto paciente a crear
     * @return Una respuesta que contiene el objeto {@link PatientCreateResponseDTO } del paciente recién creado
     */

/*

    @Override
    @Transactional
    public Response<PatientCreateResponseDTO> createPatient(PatientCreateRequestDTO patientCreateRequestDTO) {
        try{

            //Validar que el paciente no exista.
            validatePatient(patientCreateRequestDTO.dni());

            //Validar datos compuesto
            validateData(patientCreateRequestDTO);

            Patient patient = new Patient();
            patient.setFirstName(patientCreateRequestDTO.firstName());
            patient.setLastName(patientCreateRequestDTO.lastName());
            patient.setDniType

        }
    }

    private void validatePatient (String dni){
        try{
            patientRepository.findByDni(dni).orElseThrow(() -> new NotFoundException(null,"exception.patientNotFound.user",null,"exception.patientNotFound.log",null,dni,"PatientService", "validatePatient", LogLevel.WARN));

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService", null,dni, "validatePatient");
        }
    }

    private void validateData (PatientCreateRequestDTO patientCreateRequestDTO){
        try{

            //Validar que exista el "Tipo DNI"


            //Validar que exista el "Género"


            //Validar que exista la "Nacionalidad"


            //Validar que exista la "Localidad"


            //Validar que exista el "Plan de Salud"



        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService", null, patientCreateRequestDTO.dni(), "validateData");
        }
    }
}
*/