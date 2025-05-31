package com.odontologiaintegralfm.service;


import com.odontologiaintegralfm.configuration.securityConfig.AuthenticatedUserService;
import com.odontologiaintegralfm.dto.*;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.ConflictException;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.ContactEmail;
import com.odontologiaintegralfm.model.Person;
import com.odontologiaintegralfm.repository.IPersonRepository;
import com.odontologiaintegralfm.service.interfaces.*;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PersonService implements IPersonService {
    @Autowired
    @Lazy
    private AuthenticatedUserService authenticatedUserService;

    @Autowired
    private IPersonRepository personRepository;

    @Autowired
    private IDniTypeService dniTypeService;

    @Autowired
    private IGenderService genderService;

    @Autowired
    private INationalityService nationalityService;

    @Autowired
    private IAddressService addressService;

    @Autowired
    private IContactEmailService contactEmailService;

    @Autowired
    private IContactPhoneService contactPhoneService;


    /**
     * Método para crear una Persona
     * @param personDto datos recibído del request
     * @return objeto {@link Person } con la persona persistida
     */
    @Transactional
    public Person create(PersonCreateRequestDTO personDto) {
        //Valída que la persona no exista por combinación Tipo DNI + DNI número.
        validatePerson(personDto.dniTypeId(), personDto.dni());

        Person person = new Person();
        person.setFirstName(personDto.firstName());
        person.setLastName(personDto.lastName());
        person.setDniType(dniTypeService.getById(personDto.dniTypeId()));
        person.setDni(personDto.dni());
        person.setBirthDate(personDto.birthDate());
        person.setGender(genderService.getById(personDto.genderId()));
        person.setNationality(nationalityService.getById(personDto.nationalityId()));
        person.setContactEmails(contactEmailService.findOrCreate(personDto.contactEmails()));
        person.setContactPhones(contactPhoneService.findOrCreate(personDto.contactPhonesDTO()));
        person.setCreatedAt(LocalDateTime.now());
        person.setAddress(addressService.findOrCreate(
                addressService.buildAddress(personDto.addressRequestDTO())));

        //Cambios para auditoria.
        person.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
        person.setEnabled(true);

        return personRepository.save(person);
    }

    /**
     * Método para actualizar una Persona.
     * @param person datos del objeto Persona recuperado desde la BD.
     * @param personDTO datos nuevos recibidos en el DTO.
     * @return Person
     */
    @Override
    @Transactional
    public Person update(Person person,PersonUpdateRequestDTO personDTO) {

        person.setFirstName(personDTO.firstName());
        person.setLastName(personDTO.lastName());
        person.setDniType(dniTypeService.getById(personDTO.dniTypeId()));
        person.setDni(personDTO.dni());
        person.setBirthDate(personDTO.birthDate());
        person.setGender(genderService.getById(personDTO.genderId()));
        person.setNationality(nationalityService.getById(personDTO.nationalityId()));
        person.setContactEmails(contactEmailService.findOrCreate(personDTO.contactEmails()));
        person.setContactPhones(contactPhoneService.findOrCreate(personDTO.contactPhonesDTO()));
        person.setAddress(addressService.findOrCreate(
                addressService.buildAddress(personDTO.addressRequestDTO())));


        //Cambios para auditoria.
        person.setUpdatedAt(LocalDateTime.now());
        person.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());

        return personRepository.save(person);
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

    /**
     * Método para transformar un {@link Person} a un {@link PersonResponseDTO}
     *
     * @param person objeto completo
     * @return PersonResponseDTO
     */
    @Override
    public PersonResponseDTO convertToDTO(Person person) {
        return new PersonResponseDTO(
                person.getId(),
                person.getFirstName(),
                person.getLastName(),
                person.getDniType().getName(),
                person.getDni(),
                person.getBirthDate(),
                Period.between(person.getBirthDate(), LocalDate.now()).getYears(),
                person.getGender().getName(),
                person.getNationality().getName(),

                person.getContactEmails().stream()
                        .map(ContactEmail::getEmail)
                        .collect(Collectors.toSet()),

                person.getContactPhones().stream()
                        .map(phone -> new ContactPhoneResponseDTO(
                                phone.getPhoneType().getName(),
                                phone.getNumber()
                        ))
                        .collect(Collectors.toSet()),

                new AddressResponseDTO(
                        person.getAddress().getLocality().getId(),
                        person.getAddress().getLocality().getName(),
                        person.getAddress().getLocality().getProvince().getId(),
                        person.getAddress().getLocality().getProvince().getName(),
                        person.getAddress().getLocality().getProvince().getCountry().getId(),
                        person.getAddress().getLocality().getProvince().getCountry().getName(),
                        person.getAddress().getStreet(),
                        person.getAddress().getNumber(),
                        person.getAddress().getFloor(),
                        person.getAddress().getApartment()
                )
        );
    }

    /**
     * Método para saber cuantos pacientes están relacionados a un ID de domicilio
     * @param addressId domicilio
     * @return cantidad de personas asociadas.
     */
    @Override
    public long personsByAddressId(Long addressId) {
        try {
            return personRepository.countByAddress_Id(addressId);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PatientService", addressId,"<-  Id de domicilio", "personsByAddressId");
        }
    }
}
