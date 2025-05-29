package com.odontologiaintegralfm.service.interfaces;


import com.odontologiaintegralfm.dto.PersonCreateRequestDTO;
import com.odontologiaintegralfm.dto.PersonResponseDTO;
import com.odontologiaintegralfm.dto.PersonUpdateRequestDTO;
import com.odontologiaintegralfm.model.Person;

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
     * Método para saber cuantos pacientes están relacionados a un ID de domicilio
     * @param addressId domicilio
     * @return cantidad de personas asociadas.
     */
    long personsByAddressId(Long addressId);

}
