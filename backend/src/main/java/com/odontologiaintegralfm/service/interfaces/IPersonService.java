package com.odontologiaintegralfm.service.interfaces;


import com.odontologiaintegralfm.dto.PersonCreateRequestDTO;
import com.odontologiaintegralfm.dto.PersonResponseDTO;
import com.odontologiaintegralfm.dto.PersonUpdateRequestDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.model.Person;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
     * Método para saber cuantos pacientes están relacionados a un ID de domicilio
     * @param addressId domicilio
     * @return cantidad de personas asociadas.
     */
    long personsByAddressId(Long addressId);

}
