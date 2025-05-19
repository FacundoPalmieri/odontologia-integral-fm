package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.ContactPhone;
import com.odontologiaintegralfm.model.Patient;
import com.odontologiaintegralfm.model.Person;
import com.odontologiaintegralfm.repository.IContactPhoneRepository;
import com.odontologiaintegralfm.service.interfaces.IContactPhoneService;
import com.odontologiaintegralfm.service.interfaces.IPhoneTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;

@Service
public class ContactPhoneService implements IContactPhoneService {
    @Autowired
    private IContactPhoneRepository contactPhoneRepository;

    @Autowired
    private IPhoneTypeService phoneTypeService;


    /**
     * Método para crear un contacto telefónico y persistirlo en la base de datos.
     * @param phone número de telefóno
     * @return {@link ContactPhone}
     */
    @Override
    @Transactional
    public ContactPhone create(ContactPhone phone) {
        try{
            Optional<ContactPhone> contactPhone = contactPhoneRepository.findByNumber(phone.getNumber());
            if(contactPhone.isPresent()){
                ContactPhone existingPhone = contactPhone.get();
                existingPhone.getPersons().addAll(phone.getPersons());
                 return contactPhoneRepository.save(existingPhone);
            }else{

                return contactPhoneRepository.save(phone);
            }

        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "PhoneTypeService", phone.getId(), phone.getNumber(), "getById");
        }
    }

    /**
     * Método que construye un objeto {@link ContactPhone}
     *
     * @param phone     con los datos del contacto.
     * @param phoneType con los datos del tipo contacto.
     * @param person    con los datos del paciente (incluye ID)
     * @return ContactEmail con el objeto creado.
     */
    @Override
    @Transactional
    public ContactPhone buildContactPhone(String phone, Long phoneType, Person person) {
        ContactPhone contactPhone = new ContactPhone();
        contactPhone.setPersons(new HashSet<>());
        contactPhone.setNumber(phone);
        contactPhone.setPhoneType(phoneTypeService.getById(phoneType));
        contactPhone.setNumber(phone);
        contactPhone.getPersons().add(person);
        contactPhone.setEnabled(true);
        return contactPhone;
    }

    /**
     * Método para actualizar el contacto de una persona.
     *
     * @param phone
     * @param phoneType
     * @param person
     * @return
     */
    @Override
    @Transactional
    public ContactPhone updatePatientContactPhone(String phone, Long phoneType, Person person) {
        //Buscar si el Teléfono existe en la base de datos.
        Optional<ContactPhone> contactPhone = contactPhoneRepository.findByNumber(phone);
        if(contactPhone.isPresent()){
            boolean flag = false;
            for(Person p : contactPhone.get().getPersons()){
                //Verificar si el teléfono pertenece al paciente.
                if(p.getId().equals(person.getId())) {
                    flag = true;
                    if (!contactPhone.get().getEnabled()) {
                        contactPhone.get().setEnabled(true); // Si pertenece y está deshabilitado, se reactiva.
                       return create(contactPhone.get());
                    }
                }
            }
            //Si flag no cambió de valor, debo crear la relación.
            if(!flag){
                contactPhone.get().getPersons().add(person);
               return create(contactPhone.get());
            }
        }

        clearContactPhone(person);

        // Sino existe en la base de datos creo el email
        return create(buildContactPhone(phone,phoneType, person));

    }

    /**
     * Método para obtener el contacto telefónico de una persona.
     *
     * @param person objeto con datos de la persona
     * @return {@link ContactPhone}
     */
    @Override
    public ContactPhone getByPerson(Person person) {
        try{
            return contactPhoneRepository.findByPersonsId(person.getId()).orElseThrow(()->new NotFoundException("exception.contactPhoneNotFound.user",null, "exception.contactPhoneNotFound.log",new Object[]{person.getId(), person.getFirstName(), person.getLastName(),"ContactPhoneService", "getByPerson"}, LogLevel.ERROR));
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "ContactPhoneService", person.getId(), person.getFirstName()+ "," +person.getLastName(), "getByPerson");

        }
    }

    /**
     * Método privado que se encarga de eliminar relaciones y contactos físicos huérfanos.
     *  Si no existe en la base de datos:
     *             - Elimino la relación con el teléfono anterior.
     *             - Verifico si el teléfono quedó huérfano.
     *             - Creo el teléfono.
     * @param person objeto con datos de la persona
     */
    private void clearContactPhone(Person person) {
        Optional<ContactPhone> existingPhone = contactPhoneRepository.findByPersonsId(person.getId());
        if(existingPhone.isPresent()){
            existingPhone.get().getPersons().remove(person);
            contactPhoneRepository.save(existingPhone.get());
            if(existingPhone.get().getPersons().isEmpty()){
                contactPhoneRepository.delete(existingPhone.get());
            }
        }
    }
}
