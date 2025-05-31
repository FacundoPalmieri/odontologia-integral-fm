package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.ContactPhoneRequestDTO;
import com.odontologiaintegralfm.model.ContactPhone;
import com.odontologiaintegralfm.model.Person;

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



}
