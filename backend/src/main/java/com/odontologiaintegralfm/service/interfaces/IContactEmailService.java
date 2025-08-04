package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.internal.SchedulerResultDTO;
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

    /**
     * Realiza una eliminación física de los emails huérfanos.
     * Este método es invocado desde tareas programadas.
     * @return
     */
    SchedulerResultDTO deleteOrphan();

}
