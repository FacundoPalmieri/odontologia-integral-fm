package com.odontologiaintegralfm.service.interfaces;


import com.odontologiaintegralfm.dto.AddressRequestDTO;
import com.odontologiaintegralfm.model.Address;
import com.odontologiaintegralfm.model.Patient;

import java.util.Optional;

public interface IAddressService {

    /**
     * Método para crear un domicilio
     * Primero se realizar una búsqueda para ver si está deshabilitado. En caso que sea así, se procede a habilitarlo.
     * En caso que no exista, se crea.
     * @param address Objeto con el domicilio de la persona
     */
    Address enableOrCreate(Address address);

    /**
     * Método para obtener una dirección por la información exacta.
     * @param address objeto con los datos a buscar
     * @return Optional<Address>
     */
    Optional<Address> getByAddress(Address address);


    /**
     * Método para un borrado físico por domicilio huérfano.
     * @param id del domicilio
     */
    void delete(Long id);


    /**
     * Método que construye un objeto {@link Address}
     * @return objeto Address
     */
    Address buildAddress(AddressRequestDTO addressDTO);



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
     * @param patient
     * @return Address
     */
    Address updatePatientAddress(Patient patient, AddressRequestDTO address);

    /**
     * Método para obtener el domicilio de una persona
     *
     * @param id objeto con los datos de la persona.
     * @return {@link Address}
     */
    Address getByPersonId(Long id);
}
