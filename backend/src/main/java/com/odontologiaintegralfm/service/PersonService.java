package com.odontologiaintegralfm.service;


import com.odontologiaintegralfm.configuration.securityConfig.AuthenticatedUserService;
import com.odontologiaintegralfm.dto.PersonCreateRequestDTO;
import com.odontologiaintegralfm.dto.PersonUpdateRequestDTO;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.ConflictException;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.Address;
import com.odontologiaintegralfm.model.Person;
import com.odontologiaintegralfm.repository.IPersonRepository;
import com.odontologiaintegralfm.service.interfaces.IDniTypeService;
import com.odontologiaintegralfm.service.interfaces.IGenderService;
import com.odontologiaintegralfm.service.interfaces.INationalityService;
import com.odontologiaintegralfm.service.interfaces.IPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PersonService implements IPersonService {
    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private IPersonRepository personRepository;

    @Autowired
    private IDniTypeService dniTypeService;

    @Autowired
    private IGenderService genderService;

    @Autowired
    private INationalityService nationalityService;

    /**
     * Método para crear una Persona
     * @param person    objeto vacío que corresponde a la clase hija (Patient, Dentist, etc..)
     * @param personDto datos recibído del request
     * @param address   objeto domicilio ya persistido en la base de datos
     * @return objeto {@link Person } con la persona persistida
     */
    @Override
    public Person build(Person person, PersonCreateRequestDTO personDto, Address address) {
        person.setFirstName(personDto.firstName());
        person.setLastName(personDto.lastName());
        person.setDniType(dniTypeService.getById(personDto.dniTypeId()));
        person.setDni(personDto.dni());
        person.setBirthDate(personDto.birthDate());
        person.setGender(genderService.getById(personDto.genderId()));
        person.setNationality(nationalityService.getById(personDto.nationalityId()));
        person.setAddress(address);
        person.setCreatedAt(LocalDateTime.now());
        person.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        person.setEnabled(true);
        personRepository.save(person);
        return person;
    }

    /**
     * @param personDTO
     * @return
     */
    @Override
    public Person update(Person person,PersonUpdateRequestDTO personDTO) {
        person.setFirstName(personDTO.firstName());
        person.setLastName(personDTO.lastName());
        if(!person.getDniType().getId().equals(personDTO.dniTypeId())){
            person.setDniType(dniTypeService.getById(personDTO.dniTypeId()));
        }
        person.setDni(personDTO.dni());
        person.setBirthDate(personDTO.birthDate());
        if(!person.getGender().getId().equals(personDTO.genderId())){
            person.setGender(genderService.getById(personDTO.genderId()));
        }
        if(!person.getNationality().getId().equals(personDTO.nationalityId())){
            person.setNationality(nationalityService.getById(personDTO.nationalityId()));
        }

        return person;
    }

    /**
     * Mètodo para validar la existencia de una persona por combinaciòn de Tipo documento + Nùmero
     * @param dniTypeId Id del tipo del documento
     * @param dni Nùmero de identification.
     */
    @Override
    public void validatePerson(Long dniTypeId, String  dni) {
        try{
            Optional<Person> patientDni = personRepository.findByDniTypeIdAndDni(dniTypeId, dni);
            if(patientDni.isPresent()){
                throw  new ConflictException("exception.personExists.user",null,"exception.personExists.log",new Object[]{dniTypeId, dni,"PersonService", "validatePerson"}, LogLevel.ERROR);
            }

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService", null,dni, "validatePatient");
        }

    }
}
