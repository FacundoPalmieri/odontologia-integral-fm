package com.odontologiaintegralfm.feature.person.core.service.intefaces;

import com.odontologiaintegralfm.feature.person.core.dto.ContactPhoneRequestDTO;
import com.odontologiaintegralfm.infrastructure.scheduler.dto.internal.SchedulerResultDTO;
import com.odontologiaintegralfm.feature.person.core.model.ContactPhone;

import java.util.Set;

/**
 * @author [Facundo Palmieri]
 */
public interface IContactPhoneService {

    /**
     * Método para buscar o  crear un contacto telefónico y persistirlo en la base de datos.
     *
     * @param contactPhone Tipo y número de teléfono
     * @return {@link ContactPhone}
     */
    Set<ContactPhone> findOrCreate(Set<ContactPhoneRequestDTO> contactPhone);

    /**
     * Realiza una eliminación física de los teléfonos huérfanos.
     * Este método es invocado desde tareas programadas.
     * @return
     */
    SchedulerResultDTO deleteOrphan();



}
