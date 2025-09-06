package com.odontologiaintegralfm.feature.person.core.service.implement;

import com.odontologiaintegralfm.infrastructure.logging.annotations.LogAction;
import com.odontologiaintegralfm.feature.person.core.dto.AddressRequestDTO;
import com.odontologiaintegralfm.feature.person.core.dto.AddressResponseDTO;
import com.odontologiaintegralfm.infrastructure.scheduler.dto.internal.SchedulerResultDTO;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.enums.LogType;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.feature.person.core.model.Address;
import com.odontologiaintegralfm.feature.person.core.repository.IAddressRepository;
import com.odontologiaintegralfm.feature.person.core.service.intefaces.IAddressService;
import com.odontologiaintegralfm.feature.person.catalogs.service.interfaces.IGeoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AddressService implements IAddressService {

    @Autowired
    private IAddressRepository addressRepository;

    @Autowired
    private IGeoService geoService;




    /**
     * Método para crear un domicilio
     * @param address Objeto con el domicilio de la persona
     */
    @Override
    @Transactional
    public Address findOrCreate(Address address) {
        try{
            //Realiza búsqueda exacta
            Optional<Address> addressOptional = addressRepository.findAddressComplete(address.getStreet(),address.getNumber(),address.getFloor(),address.getApartment(),address.getLocality());

            //Verifica si el optional tiene valor y lo retorna.
            if(addressOptional.isPresent()) {
                return addressOptional.get();
            }

            //Si el domicilio no existe, lo crea.
            addressRepository.save(address);
            return address;

        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "AddressService",null, address.getStreet() + " " +  address.getNumber(), "findOrCreate");
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
     * Elimina un domicilio huérfano.
     * @param id del domicilio.
     */
    @Override
    public void delete(Long id) {
        try{
            addressRepository.deleteById(id);
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "AddressService",id,"<- Id domicilio", "delete");
        }
    }


    /**
     * Método para convertir un {@link Address} a un objeto {@link AddressResponseDTO}
     * @param address Objeto con la información completa
     * @return AddressResponseDTO
     */
    public AddressResponseDTO convertToDTO(Address address){
      return  new AddressResponseDTO(
                address.getLocality().getId(),
                address.getLocality().getName(),
                address.getLocality().getProvince().getId(),
                address.getLocality().getProvince().getName(),
                address.getLocality().getProvince().getCountry().getId(),
                address.getLocality().getProvince().getCountry().getName(),
                address.getStreet(),
                address.getNumber(),
                address.getFloor(),
                address.getApartment()
        );
    }



    /**
     * Realiza una eliminación física de los domicilios huérfanos.
     * Este método es invocado desde tareas programadas.
     *
     * @return
     */
    @Override
    @Transactional
    @LogAction(
            value ="addressService.systemLogService.deleteOrphan",
            args =  {"#result.durationSeconds","#result.message", "#result.countInit","#result.countDeleted" },
            type = LogType.SCHEDULED,
            level = LogLevel.INFO
    )
    public SchedulerResultDTO deleteOrphan() {
        int countInit = 0;
        int countDeleted = 0;
        long start;
        long end;
        double durationSeconds;

        //Inicia tarea programada
        start = System.currentTimeMillis();


        try{
            List<Long> orphanAddresses  = addressRepository.findOrphan();

            //1. SI NO HAY REGISTROS PARA ELIMINAR
            if (orphanAddresses.isEmpty()) {
                //Finaliza tarea programada
                end = System.currentTimeMillis();
                //Convierte milisegundos a segundos.
                durationSeconds = (end - start) / 1000.0;

                SchedulerResultDTO schedulerResultDTO = new SchedulerResultDTO(
                        durationSeconds,
                        "No se encontraron registros para eliminar",
                        countInit,
                        countDeleted);
                return schedulerResultDTO;
            }

            //2. SI EXISTEN REGISTROS PARA ELIMINAR

            //Se obtiene total para loguear.
             countInit = orphanAddresses.size();

            //Se eliminan registros.
           countDeleted = addressRepository.deleteAll(orphanAddresses);

            //Finaliza tarea programada
            end = System.currentTimeMillis();

            //Convierte milisegundos a segundos.
            durationSeconds = (end - start) / 1000.0;

            SchedulerResultDTO schedulerResultDTO = new SchedulerResultDTO(
                    durationSeconds,
                    "Registros eliminados correctamente",
                    countInit,
                    countDeleted);
            return schedulerResultDTO;

        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "AddressService",null,null, "deleteOrphan");
        }
    }
}
