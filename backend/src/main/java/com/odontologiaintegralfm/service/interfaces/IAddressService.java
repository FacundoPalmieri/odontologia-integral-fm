package com.odontologiaintegralfm.service.interfaces;


import com.odontologiaintegralfm.model.Address;

public interface IAddressService {

    /**
     * Método para crear un domicilio
     * Primero se realizar una búsqueda para ver si está deshabilitado. En caso que sea así, se procede a habilitarlo.
     * En caso que no exista, se crea.
     * @param address Objeto con el domicilio de la persona
     */
    Address enableOrCreate(Address address);

}
