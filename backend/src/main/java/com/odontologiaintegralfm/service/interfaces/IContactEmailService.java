package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.model.ContactEmail;
import com.odontologiaintegralfm.model.Person;

import java.util.Set;

/**
 * @author [Facundo Palmieri]
 */
public interface IContactEmailService {

    /**
     * Método para buscar o crear un contacto email y persistirlo en la base de datos.
     * @param email a agregar
     * @return {@link ContactEmail}
     */
    Set<ContactEmail> findOrCreate(Set<String> email);



}
