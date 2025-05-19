package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.DentistCreateRequestDTO;
import com.odontologiaintegralfm.dto.DentistResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.Dentist;

import java.util.List;


public interface IDentistService {

    /**
     * Crea un nuevo odontólogo en el sistema junto con su domicilio, contactos, historia clínica
     * y devuelve una respuesta con todos los datos relevantes.
     *
     * <p>Este método sigue los siguientes pasos:
     * <ol>
     *     <li>Valida que no exista un odontólogo con el mismo DNI.</li>
     *     <li>Crea o habilita un domicilio según corresponda.</li>
     *     <li>Crea un nuevo objeto {@link Dentist} y lo persiste.</li>
     *     <li>Registra los contactos del odontólogo (email y teléfono).</li>
     *     <li>Construye un DTO con toda la información creada.</li>
     * </ol>
     *
     * @param dentistCreateRequestDTO DTO con los datos necesarios para crear el odontólogo.
     * @return {@link Response} que contiene un {@link DentistResponseDTO} con los datos del odontólogo creado.
     * @throws DataBaseException si ocurre un error durante el acceso a la base de datos.
     */
    Response<DentistResponseDTO> create(DentistCreateRequestDTO dentistCreateRequestDTO);


    /**
     * Método para obtener un listado de pacientes habilitados en el sistema.
     * @return Una respuesta que contiene una lista de objetos {@link DentistResponseDTO }
     */
    Response<List<DentistResponseDTO>> getAll();

}
