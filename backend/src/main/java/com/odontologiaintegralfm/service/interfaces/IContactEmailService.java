package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.model.ContactEmail;
import com.odontologiaintegralfm.model.ContactPhone;
import com.odontologiaintegralfm.model.Person;

import java.util.Set;

/**
 * @author [Facundo Palmieri]
 */
public interface IContactEmailService {

    /**
     * Método para crear un contacto email y persistirlo en la base de datos.
     * @param contactEmail de la persona.
     * @return {@link ContactEmail}
     */
    ContactEmail create(ContactEmail contactEmail);


    /**
     * Método que construye un objeto {@link ContactEmail}
     * @param email Email del contacto
     * @param person con los datos del paciente (incluye ID)
     * @return ContactEmail con el objeto creado.
     */
    ContactEmail buildContactEmail(String email, Person person);


    /**
     * Método para actualizar el contacto de un paciente
     * @param email
     * @param person
     * @return
     */
    ContactEmail updateContactEmail(String email, Person person);


    /**
     * Método para obtener el email de una persona.
     * @param person objeto con datos de la persona
     * @return {@link ContactEmail}
     */
    ContactEmail getByPerson (Person person);
}
