package com.odontologiaintegralfm.feature.person.core.service.intefaces;


import com.odontologiaintegralfm.feature.person.core.dto.AddressRequestDTO;
import com.odontologiaintegralfm.feature.person.core.dto.AddressResponseDTO;
import com.odontologiaintegralfm.infrastructure.scheduler.dto.internal.SchedulerResultDTO;
import com.odontologiaintegralfm.feature.person.core.model.Address;

public interface IAddressService {

    /**
     * Método para crear un domicilio
     * @param address Objeto con el domicilio de la persona
     */
    Address findOrCreate(Address address);


    /**
     * Método que construye un objeto {@link Address}
     * @return objeto Address
     */
    Address buildAddress(AddressRequestDTO addressDTO);

    /**
     * Elimina un domicilio huérfano.
     * @param id del domicilio.
     */
    void delete(Long id);


    /**
     * Método para convertir un {@link Address} a un objeto {@link AddressResponseDTO}
     * @param address Objeto con la información completa
     * @return AddressResponseDTO
     */
    AddressResponseDTO convertToDTO(Address address);


    /**
     * Realiza una eliminación física de los domicilios huérfanos.
     * Este método es invocado desde tareas programadas.
     * @return
     */
    SchedulerResultDTO deleteOrphan();
}
