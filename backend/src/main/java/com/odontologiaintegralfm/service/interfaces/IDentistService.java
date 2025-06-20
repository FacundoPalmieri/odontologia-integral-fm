package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.DentistCreateRequestDTO;
import com.odontologiaintegralfm.dto.DentistResponseDTO;
import com.odontologiaintegralfm.dto.DentistUpdateRequestDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.Dentist;
import com.odontologiaintegralfm.model.Person;

import java.util.List;
import java.util.Optional;
import java.util.Set;


public interface IDentistService {

    /**
     * Crea un nuevo odontólogo en el sistema junto con su domicilio, contactos, historia clínica
     * y devuelve un objeto  Dentist
     *
     * <p>Este método:
     * <ol>
     *     <li>Valida que no exista un odontólogo con el mismo DNI.</li>
     *     <li>Crea un nuevo objeto {@link Dentist} y lo persiste.</li>
     * </ol>
     * @param person Objeto ya persistido en la base con los datos referidos a la Persona.
     * @param dentistCreateRequestDTO DTO con los datos necesarios para específicamente un odontólogo.
     * @return {@link Dentist} que contiene el dentista persistido en la base de datos.
     * @throws DataBaseException si ocurre un error durante el acceso a la base de datos.
     */
    Dentist create(Person person, DentistCreateRequestDTO dentistCreateRequestDTO);

    /**
     * Método para actualizar un Dentista.
     * @param dentist datos del objeto Dentista recuperado desde la BD.
     * @param dentistUpdateRequestDTO datos nuevos recibidos en el DTO.
     * @return Dentista
     */
    Dentist update(Dentist dentist, DentistUpdateRequestDTO dentistUpdateRequestDTO);

    /**
     * Método para obtener un listado de pacientes habilitados en el sistema.
     * @return Una respuesta que contiene una lista de objetos {@link DentistResponseDTO }
     */
    Response<Set<DentistResponseDTO>> getAll();

    /**
     * Método para obtener un dentista por su Id. Retorna un Optional ya que al consultar por ID de Person, puede ser que la persona no sea un dentista.
     * El método llamador realiza acción.
     * @param id del Dentista.
     * @return objeto  Optional<Dentist>
     */
    Optional<Dentist> getById(Long id);

    /**
     * Método para transformar un {@link Dentist} a un {@link DentistResponseDTO}
     * @param dentist objeto completo
     * @return DentistResponseDTO
     */
    DentistResponseDTO convertToDTO(Dentist dentist);

}
