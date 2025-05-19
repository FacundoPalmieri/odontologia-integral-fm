package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.ContactEmail;
import com.odontologiaintegralfm.model.Person;
import com.odontologiaintegralfm.repository.IContactEmailRepository;
import com.odontologiaintegralfm.service.interfaces.IContactEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Optional;

@Service
public class ContactEmailService implements IContactEmailService {
    @Autowired
    private IContactEmailRepository contactEmailRepository;


    /**
     * Método para crear un contacto email y persistirlo en la base de datos.
     * @param contactEmail de la persona.
     * @return {@link ContactEmail}
     */
    @Transactional
    public ContactEmail create(ContactEmail contactEmail) {
        try {
            //Buscar si el contacto existe
            Optional<ContactEmail> contactEmailOptional = contactEmailRepository.findByEmail(contactEmail.getEmail());
            if (contactEmailOptional.isPresent()) {
                ContactEmail existingContactEmail = contactEmailOptional.get();
                existingContactEmail.getPersons().addAll(contactEmail.getPersons());
                return contactEmailRepository.save(existingContactEmail);

            }else{
                return contactEmailRepository.save(contactEmail);

            }
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ContactEmailService", contactEmail.getId(), contactEmail.getEmail(), "save");
        }
    }


    /**
     * Método que construye un objeto {@link ContactEmail}
     *
     * @param email   Email del contacto
     * @param person con los datos del paciente (incluye ID)
     * @return ContactEmail con el objeto creado.
     */
    @Override
    @Transactional
    public ContactEmail buildContactEmail(String email, Person person) {
        ContactEmail contactEmail = new ContactEmail();
        contactEmail.setPersons(new HashSet<>());
        contactEmail.setEmail(email);
        contactEmail.getPersons().add(person);
        contactEmail.setEnabled(true);
        return contactEmail;
    }

    /**
     * Método para actualizar el contacto de un paciente
     * @param email
     * @param person
     * @return
     */
    @Override
    @Transactional
    public ContactEmail updateContactEmail(String email, Person person) {
        //Buscar si el Email existe en la base de datos.
       Optional <ContactEmail> contactEmail = contactEmailRepository.findByEmail(email);
       if(contactEmail.isPresent()) {
            boolean flag = false;
            for (Person p : contactEmail.get().getPersons()) {
                //Verificar si el email pertenece al paciente.
                if (p.getId().equals(person.getId())) {
                    flag = true;
                    if (!contactEmail.get().getEnabled()) {
                        contactEmail.get().setEnabled(true); // Si pertenece y está deshabilitado, se reactiva.
                        return create(contactEmail.get());
                    }
                }
            }

            //Si flag no cambió de valor, debo crear la relación.
            if (!flag) {
                contactEmail.get().getPersons().add(person);
                return create(contactEmail.get());
            }
       }
       clearContactEmail(person);

        // Sino existe en la base de datos creo el email
        return create(buildContactEmail(email, person));
    }



    /**
     * Método para obtener el email de una persona.
     * @param person objeto con datos de la persona
     * @return {@link ContactEmail}
     */
    @Override
    public ContactEmail getByPerson(Person person) {
        try{
            return contactEmailRepository.findByPersonsId(person.getId()).orElseThrow(()-> new NotFoundException("exception.contactEmailNotFound.log",null, "exception.contactEmailNotFound.user",new Object[]{person.getId(), person.getFirstName(), person.getLastName(),"ContactEmailService", "getByPerson"}, LogLevel.ERROR));
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ContactEmailService", person.getId(), person.getFirstName() + "," + person.getLastName(), "getByPerson");
        }
    }



    private void clearContactEmail(Person person) {
        Optional<ContactEmail> existingEmail = contactEmailRepository.findByPersonsId(person.getId());
        if(existingEmail.isPresent()) {
            existingEmail.get().getPersons().remove(person);
            contactEmailRepository.save(existingEmail.get());
            if(existingEmail.get().getPersons().isEmpty()){
                contactEmailRepository.delete(existingEmail.get());
            }

        }
    }


}
