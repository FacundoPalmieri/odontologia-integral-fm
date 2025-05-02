package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.ContactEmail;
import com.odontologiaintegralfm.repository.IContactEmailRepository;
import com.odontologiaintegralfm.service.interfaces.IContactEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactEmailService implements IContactEmailService {
    @Autowired
    private IContactEmailRepository contactEmailRepository;

    @Transactional
    public ContactEmail save(ContactEmail contactEmail) {
        try {
           return  contactEmailRepository.save(contactEmail);
        }catch(DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "ContactEmailService", contactEmail.getId(), contactEmail.getEmail(), "save");
        }
    }
}
