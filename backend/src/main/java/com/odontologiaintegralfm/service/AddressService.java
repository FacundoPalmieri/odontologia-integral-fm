package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.Address;
import com.odontologiaintegralfm.repository.IAddressRepository;
import com.odontologiaintegralfm.service.interfaces.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressService implements IAddressService {

    @Autowired
    private IAddressRepository addressRepository;

    /**
     * Método para crear un domicilio
     * @param address Objeto con el domicilio de la persona
     * @throws DataBaseException En caso de error de conexión en base de datos.
     */
    @Override
    @Transactional
    public void save(Address address) {
        try{
            addressRepository.save(address);
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "AddressService", null, address.getStreet(), "save");
        }

    }
}
