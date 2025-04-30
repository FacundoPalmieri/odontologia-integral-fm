package com.odontologiaintegralfm.service.interfaces;


import com.odontologiaintegralfm.model.Address;

public interface IAddressService {

    /**
     * Método para crear un domicilio
     * @param address Objeto con el domicilio de la persona
     */
    void save(Address address);
}
