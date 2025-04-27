package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.PatientCreateRequestDTO;
import com.odontologiaintegralfm.dto.PatientCreateResponseDTO;
import com.odontologiaintegralfm.dto.Response;

/**
 * @author [Facundo Palmieri]
 */
public interface IPatientService {

    /**
     * Método para crear un paciente
     * @param patientCreateRequestDTO El objeto paciente a crear
     * @return Una respuesta que contiene el objeto {@link PatientCreateResponseDTO } del paciente recién creado
     */
   // Response<PatientCreateResponseDTO> createPatient(PatientCreateRequestDTO patientCreateRequestDTO);

}
