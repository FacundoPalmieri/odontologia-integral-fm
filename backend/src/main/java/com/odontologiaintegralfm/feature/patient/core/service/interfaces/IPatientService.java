package com.odontologiaintegralfm.feature.patient.core.service.interfaces;

import com.odontologiaintegralfm.feature.patient.core.dto.PatientCreateRequestDTO;
import com.odontologiaintegralfm.feature.patient.core.dto.PatientUpdateRequestDTO;
import com.odontologiaintegralfm.feature.patient.core.dto.PatientResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.feature.patient.core.model.Patient;
import org.springframework.data.domain.Page;


/**
 * @author [Facundo Palmieri]
 */
public interface IPatientService {

    /**
     * Crea un nuevo paciente en el sistema junto a los riesgos médicos.
     * Devuelve una respuesta con todos los datos relevantes.
     *
     * <p>Este método sigue los siguientes pasos:
     * <ol>
     *     <li>Valida que no exista un paciente con el mismo DNI.</li>
     *     <li>Crea por medio del PersonService un nuevo objeto {@link Patient} y lo persiste.</li>
     *     <li>Crea por medio del patientMedicalRiskService los riesgos médicos asociados al paciente .</li>
     *     <li>Construye un DTO con toda la información creada.</li>
     * </ol>
     *
     * @param patientRequestDTO DTO con los datos necesarios para crear el paciente.
     * @return {@link Response} que contiene un {@link PatientResponseDTO} con los datos del paciente creado.
     * @throws DataBaseException si ocurre un error durante el acceso a la base de datos.
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
    Response<Page<PatientResponseDTO>> getAll(int page, int size, String sortBy, String direction);


    /**
     * Método para obtener un paciente habilitado por ID
     * @param id del paciente
     * @return Una respuesta que contiene el objeto {@link PatientResponseDTO } del paciente
     */
    Response<PatientResponseDTO> getById(Long id);


    /**
     * Método para obtener un paciente habilitado por ID
     * @param id del paciente
     * @return La entidad recuperada
     */
    Patient getByIdInternal(Long id);



}
