package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.model.ContactPhone;
import com.odontologiaintegralfm.model.Person;

/**
 * @author [Facundo Palmieri]
 */
public interface IContactPhoneService {

    /**
     * Método para crear un contacto telefónico y persistirlo en la base de datos.
     * @param phone número de telefóno
     * @return {@link ContactPhone}
     */
    ContactPhone create(ContactPhone phone);


    /**
     * Método que construye un objeto {@link ContactPhone}
     *
     * @param phone     con los datos del contacto.
     * @param phoneType con los datos del tipo contacto.
     * @param person    con los datos del paciente (incluye ID)
     * @return ContactEmail con el objeto creado.
     */
    ContactPhone buildContactPhone(String phone, Long phoneType, Person person);

    /**
     * Método para actualizar el contacto de una persona.
     *
     * @param phone
     * @param phoneType
     * @param person
     * @return
     */
    ContactPhone updatePatientContactPhone(String phone, Long phoneType, Person person);


    /**
     * Método para obtener el contacto telefónico de una persona.
     * @param person objeto con datos de la persona
     * @return {@link ContactPhone}
     */
    ContactPhone getByPerson (Person person);
}
