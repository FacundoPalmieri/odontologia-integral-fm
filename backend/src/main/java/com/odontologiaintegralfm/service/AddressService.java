package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.ConflictException;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.Address;
import com.odontologiaintegralfm.repository.IAddressRepository;
import com.odontologiaintegralfm.service.interfaces.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AddressService implements IAddressService {

    @Autowired
    private IAddressRepository addressRepository;

     /**
     * Método para crear un domicilio
     * Primero se realizar una búsqueda para ver si existe y está deshabilitado. En caso que sea así, se procede a habilitarlo.
     * En caso que no exista, se crea.
     * @param address Objeto con el domicilio de la persona
     * @throws DataBaseException En caso de error de conexión en base de datos.
     */

    @Override
    @Transactional
    public Address enableOrCreate(Address address) {
        try{
            //Realiza búsqueda exacta
            Optional<Address> addressOptional = addressRepository.findAddressComplete(address.getStreet(),address.getNumber(),address.getFloor(),address.getApartment(),address.getLocality());

            //Verifica si el optional tiene valor y está habilitado el domicilio.
            if(addressOptional.isPresent() && addressOptional.get().getEnabled()){
                throw new ConflictException("exception.addressExists.user", null, "exception.addressExists.log", new Object[]{addressOptional.get().getId(), "AddressService", "enableOrCreate"}, LogLevel.ERROR);

               //Verifica si el optional tiene valor y está Deshabilitado el domicilio. En ese caso, lo reactiva.
            } else if(addressOptional.isPresent()){
                Address addressExisting = addressOptional.get();
                addressExisting.setEnabled(true);
                addressRepository.save(addressExisting);
                return addressExisting;
            }

            //Si el domicilio no existe, lo crea.
            address.setEnabled(true);
            addressRepository.save(address);
            return address;

        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "AddressService",null, address.getStreet() + " " +  address.getNumber(), "enableOrCreate");
        }

    }

}
