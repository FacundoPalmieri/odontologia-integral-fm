package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.PatientCreateRequestDTO;
import com.odontologiaintegralfm.dto.PatientUpdateRequestDTO;
import com.odontologiaintegralfm.dto.PatientResponseDTO;
import com.odontologiaintegralfm.dto.Response;

import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
public interface IPatientService {

    /**
     * Método para crear un paciente.
     * @param patientRequestDTO El objeto paciente a crear
     * @return Una respuesta que contiene el objeto {@link PatientResponseDTO } del paciente creado.
     */
    Response<PatientResponseDTO> create(PatientCreateRequestDTO patientRequestDTO);



    /**
     * Método para actualizar datos de un paciente.
     * @param patientUpdateRequestDTO El objeto paciente a Actualizar.
     * @return Una respuesta que contiene el objeto {@link PatientResponseDTO } del paciente actualizado.
     */
    Response<PatientResponseDTO> update(PatientUpdateRequestDTO patientUpdateRequestDTO);

    /**
     * Método para obtener un listado de pacientes habilitados en el sistema.
     * @return Una respuesta que contiene una lista de objetos {@link PatientResponseDTO }
     */
    Response<List<PatientResponseDTO>> getAll();


    /**
     * Método para obtener un paciente habilitado por ID
     * @param id del paciente
     * @return Una respuesta que contiene el objeto {@link PatientResponseDTO } del paciente
     */
    Response<PatientResponseDTO> getById(Long id);

}
