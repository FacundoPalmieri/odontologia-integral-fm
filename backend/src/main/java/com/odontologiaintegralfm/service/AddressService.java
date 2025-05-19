package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.AddressRequestDTO;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.Address;
import com.odontologiaintegralfm.model.Patient;
import com.odontologiaintegralfm.repository.IAddressRepository;
import com.odontologiaintegralfm.service.interfaces.IAddressService;
import com.odontologiaintegralfm.service.interfaces.IGeoService;
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

    @Autowired
    private IGeoService geoService;


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

            //Verifica si el optional tiene valor y está habilitado el domicilio se retorna el mismo.
            if(addressOptional.isPresent() && addressOptional.get().getEnabled()){
                return addressOptional.get();

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

    /**
     * Método para obtener una dirección por la información exacta.
     * @param address objeto con los datos a buscar
     * @return Optional<Address>
     */
    public Optional<Address> getByAddress(Address address){
        try{
            return addressRepository.findAddressComplete(address.getStreet(),address.getNumber(),address.getFloor(),address.getApartment(),address.getLocality());
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "AddressService",null, address.getStreet() + " " +  address.getNumber(), "getByAddress");

        }
    }

    /**
     * Método para un borrado físico por domicilio huérfano.
     * @param Id del domicilio
     */
    @Transactional
    public void delete(Long Id){
        try{
            addressRepository.deleteById(Id);
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "AddressService",Id,null, "Delete");
        }
    }

    /**
     * Método que construye un objeto {@link Address} y llama al servicio correspondiente para su creación.
     * @return objeto Address
     */
    @Transactional
    @Override
    public Address buildAddress(AddressRequestDTO addressDTO) {
            Address address = new Address();
            address.setStreet(addressDTO.street());
            address.setNumber(addressDTO.number());
            address.setFloor(addressDTO.floor());
            address.setApartment(addressDTO.apartment());
            address.setLocality(geoService.getLocalityById(addressDTO.localityId()));
            return address;
    }

    /**
     * Método para actualizar el domicilio de un paciente.
     * <ul>
     *     <li>
     *         Se crea un objeto dirección.
     *         Se busca si la misma existe.
     *          <ul>
     *              <li>
     *                  Se verifica si el paciente apunta a la dirección.
     *                  Si no apunta a esa dirección, se guarda el ID de la dirección que tenía asignada y se setea la nueva.
     *                  Se verifica si el domicilio anterior que tenía asignado el paciente quedó asignado a otro paciente, caso contrario de elimina físicamente.
     *              </li>
     *          </ul>
     *          Si la dirección no existe se crea una nueva  y se le asigna al paciente
     *     </li>
     * </ul>
     *
     * @param patient
     * @return Address
     */
    @Override
    @Transactional
    public Address updatePatientAddress(Patient patient, AddressRequestDTO addressDTO) {
        // Creamos una nueva dirección con los datos del request
        Address addressNew = buildAddress(addressDTO);

        // Buscamos si ya existe una dirección idéntica
        Optional<Address> addressExisting = getByAddress(addressNew);

        //Si la dirección existe no necesito crear una nueva, solo verificar si el paciente la tiene asignada
        if(addressExisting.isPresent()){
            // Se sabe que la dirección existe, entonces se verifica si el paciente apunta a la dirección existente
            if(!patient.getAddress().getId().equals(addressExisting.get().getId())){
                //Se sabe que el paciente no apunta a la dirección recibida en el request. Entonces se guarda el ID de la dirección que tenía asignada y se setea la nueva.
                Address addressOld = patient.getAddress();
                patient.setAddress(addressExisting.get());

                //Se verifica si el domicilio anterior que tenía asignado el paciente quedó asignado a otro paciente.
                Optional <Patient> patientsAddressOld = addressRepository.findByAddressIdOld(addressOld.getId());
                if(patientsAddressOld.isEmpty()){
                    // Si el domicilio quedó huérfano, se elimina.
                    delete(addressOld.getId());
                }
                return addressExisting.get();
            }
            return addressExisting.get();
        }else{
            // Si la dirección no existe se crea una nueva  y se le asigna al paciente
            Address addressCreate = enableOrCreate(addressNew);
            patient.setAddress(addressCreate);
            return addressCreate;
        }

    }


    /**
     * Método para obtener el domicilio de una persona
     *
     * @param id objeto con los datos de la persona.
     * @return {@link Address}
     */
    @Override
    public Address getByPersonId(Long id) {
        try{
            return addressRepository.findByPersonId(id);
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "AddressService",id,null, "getByPersonId");
        }
    }


}
