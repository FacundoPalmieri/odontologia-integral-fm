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
import java.util.Set;

@Service
public class ContactEmailService implements IContactEmailService {
    @Autowired
    private IContactEmailRepository contactEmailRepository;


    /**
     * Método para buscar o crear un contacto email y persistirlo en la base de datos.
     * @param emails a agregar
     * @return {@link ContactEmail}
     */
    @Transactional
    public Set<ContactEmail> findOrCreate(Set<String> emails) {
        Set<ContactEmail> contactEmails = new HashSet<>();
        try {
            for (String email : emails) {
                Optional<ContactEmail> existing = contactEmailRepository.findByEmail(email);
                if (existing.isPresent()) {
                    contactEmails.add(existing.get());
                } else {
                    ContactEmail newContactEmail = new ContactEmail();
                    newContactEmail.setEmail(email);
                    ContactEmail saved = contactEmailRepository.save(newContactEmail);
                    contactEmails.add(saved);
                }
            }
            return contactEmails;
        } catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ContactEmailService", null, String.join(", ", emails), "create");
        }
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



}
