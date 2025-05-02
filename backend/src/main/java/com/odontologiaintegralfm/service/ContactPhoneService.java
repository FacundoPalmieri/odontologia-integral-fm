package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.ContactPhone;
import com.odontologiaintegralfm.repository.IContactPhoneRepository;
import com.odontologiaintegralfm.service.interfaces.IContactPhoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactPhoneService implements IContactPhoneService {
    @Autowired
    private IContactPhoneRepository contactPhoneRepository;


    /**
     * @param phone
     */
    @Override
    @Transactional
    public ContactPhone save(ContactPhone phone) {
        try{
           return contactPhoneRepository.save(phone);
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "TelephoneTypeService", phone.getId(), phone.getNumber(), "getById");
        }
    }
}
