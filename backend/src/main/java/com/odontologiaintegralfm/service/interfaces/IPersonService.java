package com.odontologiaintegralfm.service.interfaces;


import com.odontologiaintegralfm.dto.PersonCreateRequestDTO;
import com.odontologiaintegralfm.dto.PersonResponseDTO;
import com.odontologiaintegralfm.dto.PersonUpdateRequestDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.model.Person;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


/**
 * Interfaz que define las operaciones de negocio relacionadas con la entidad {@link Person}.
 * Permite crear, actualizar, consultar y validar personas, así como gestionar su imagen de perfil.
 *
 * @author Facundo Palmieri
 */
public interface IPersonService {


    /**
     * Método para crear una Persona
     * @param personDto datos recibído del request
     * @return objeto {@link Person } con la persona persistida
     */
     Person create(PersonCreateRequestDTO personDto);

    /**
     * Método para actualizar una Persona.
     * @param person datos del objeto Persona recuperado desde la BD.
     * @param personDTO datos nuevos recibidos en el DTO.
     * @return Person
     */
     Person update(Person person, PersonUpdateRequestDTO personDTO);

    /**
     * Método para obtener una persona por su ID.
     * @param id de la persona.
     * @return Objeto Person
     */
     Person getById(Long id);


    /**
     * Mètodo para validar la existencia de una persona por combinaciòn de Tipo documento + Nùmero
     * @param dniTypeId Id del tipo del documento
     * @param dni Nùmero de identification.
     */
    void validatePerson(Long dniTypeId, String  dni);

    /**
     * Método para transformar un {@link Person} a un {@link PersonResponseDTO}
     * @param person objeto completo
     * @return PersonResponseDTO
     */
    PersonResponseDTO convertToDTO (Person person);


    /**
     * Método para actualizar la imágen de perfil de la persona.
     * @param file
     * @param personId
     * @return
     */
    Response<String> saveAvatar (MultipartFile file, Long personId) throws IOException;

    /**
     * Método para obtener la imágen de perfil de la persona.
     * @param personId Id de la persona.
     * @return
     * @throws IOException
     */
    UrlResource getAvatar (Long personId) throws IOException;

    /**
     * Elimina el avatar por ID de persona.
     * @param personId: ID Persona
     * @return objeto response.
     * @throws IOException
     */
    Response<String> deleteAvatar (Long personId) throws IOException;


}
