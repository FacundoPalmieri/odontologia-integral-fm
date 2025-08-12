package com.odontologiaintegralfm.service;


import com.odontologiaintegralfm.configuration.securityConfig.AuthenticatedUserService;
import com.odontologiaintegralfm.dto.*;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.ConflictException;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.ContactEmail;
import com.odontologiaintegralfm.model.Person;
import com.odontologiaintegralfm.repository.IPersonRepository;
import com.odontologiaintegralfm.service.interfaces.*;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio que implementa la lógica de negocio para la gestión de {@link Person}.
 * Permite crear, actualizar, obtener personas, validar existencia y gestionar la imagen de perfil.
 * Utiliza múltiples servicios y repositorios para manejar relaciones y operaciones asociadas.
 *
 * La mayoría de los métodos que modifican datos están anotados con {@link Transactional} para garantizar la atomicidad.
 *
 * @author Facundo Palmieri
 */

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

    @Autowired
    private IAvatarService avatarService;

    @Autowired
    private MessageService messageService;

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
        person.setContactPhones(contactPhoneService.findOrCreate(personDto.contactPhones()));
        person.setCreatedAt(LocalDateTime.now());
        person.setAddress(addressService.findOrCreate(
                addressService.buildAddress(personDto.address())));

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
        person.setContactPhones(contactPhoneService.findOrCreate(personDTO.contactPhones()));
        person.setAddress(addressService.findOrCreate(
                addressService.buildAddress(personDTO.address())));


        //Cambios para auditoria.
        person.setUpdatedAt(LocalDateTime.now());
        person.setUpdatedBy(authenticatedUserService.getAuthenticatedUser());

        return personRepository.save(person);
    }

    /**
     * Método para obtener una persona por su ID.
     *
     * @param id de la persona.
     * @return Objeto Person
     */
    @Override
    public Person getById(Long id) {
        try {
            return personRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("exception.personNotFound.user", null, "exception.personNotFound.log", new Object[]{id, "PersonService", "getById"}, LogLevel.ERROR));

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PersonService", id, "<- Id de la Persona", "getById");
        }
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
            throw new DataBaseException(e, "PersonService", null,dni, "validatePerson");
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
     * Método para actualizar la imágen de perfil de la persona.
     *
     * @param file
     * @param personId
     * @return
     */
    @Override
    public Response<String> saveAvatar(MultipartFile file, Long personId)throws IOException {

        //Verifica si existe imagen.
        if(file.isEmpty()){
            return null;
        }

        //Obtiene la persona
        Person person = getById(personId);

        //Llama al avatarService para crear/actualizar imagen
        person.setAvatarUrl(avatarService.saveImage(file, person));
        personRepository.save(person);

        String messageUser = messageService.getMessage("personService.saveAvatar.user.ok", null, LocaleContextHolder.getLocale());
        return new Response<>(true, messageUser,person.getAvatarUrl());
    }



    /**
     * Método para obtener la imágen de perfil de la persona.
     *
     * @param personId Id de la persona.
     * @return
     * @throws IOException
     */
    @Override
    public UrlResource getAvatar(Long personId) throws IOException {
        try{
            //Obtiene la persona
            Person person = getById(personId);
            return  avatarService.getImage(person);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PersonService", personId,"<-  Id de la persona", "getAvatar");
        }


    }

    /**
     * Elimina el avatar por ID de persona.
     *
     * @param personId : ID Persona
     * @return objeto response.
     * @throws IOException
     */
    @Override
    public Response<String> deleteAvatar(Long personId) throws IOException {
        try{
            //Obtiene la persona
            Person person = getById(personId);
            avatarService.deleteImage(person);

            String messageUser = messageService.getMessage("personService.deleteAvatar.user.ok",null, LocaleContextHolder.getLocale());
            return new Response<>(true, messageUser,null);
        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "PersonService", personId,"<-  Id de la persona", "deleteAvatar");
        }
    }

}
