package com.odontologiaintegralfm.service.interfaces;


import com.odontologiaintegralfm.dto.PersonCreateRequestDTO;
import com.odontologiaintegralfm.dto.PersonUpdateRequestDTO;
import com.odontologiaintegralfm.model.Address;
import com.odontologiaintegralfm.model.Person;

public interface IPersonService {

    /**
     * Método para crear una Persona
     * @param person objeto vacío que corresponde a la clase hija (Patient, Dentist, etc..)
     * @param personDto datos recibído del request
     * @param address objeto domicilio ya persistido en la base de datos
     * @return
     */
    Person build(Person person, PersonCreateRequestDTO personDto, Address address);


    Person update(Person person, PersonUpdateRequestDTO personDTO);


    /**
     * Mètodo para validar la existencia de una persona por combinaciòn de Tipo documento + Nùmero
     * @param dniTypeId Id del tipo del documento
     * @param dni Nùmero de identification.
     */
    void validatePerson(Long dniTypeId, String  dni);

}
