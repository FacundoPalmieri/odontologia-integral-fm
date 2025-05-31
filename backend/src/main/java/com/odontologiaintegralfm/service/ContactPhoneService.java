package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.ContactPhoneRequestDTO;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.ContactPhone;
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
import java.util.Set;

@Service
public class ContactPhoneService implements IContactPhoneService {
    @Autowired
    private IContactPhoneRepository contactPhoneRepository;

    @Autowired
    private IPhoneTypeService phoneTypeService;


    /**
     * Método para buscar o  crear un contacto telefónico y persistirlo en la base de datos.
     *
     * @param phoneDTOs Tipo y número de teléfono
     * @return {@link ContactPhone}
     */
    @Override
    @Transactional
    public Set<ContactPhone> findOrCreate(Set<ContactPhoneRequestDTO> phoneDTOs) {
        Set<ContactPhone> contactPhones = new HashSet<>();

        for (ContactPhoneRequestDTO dto : phoneDTOs) {
            Optional<ContactPhone> existingPhone = contactPhoneRepository.findByNumber(dto.phone());
            if (existingPhone.isPresent()) {
                contactPhones.add(existingPhone.get());
            } else {
                ContactPhone newPhone = new ContactPhone();
                newPhone.setNumber(dto.phone());
                newPhone.setPhoneType(phoneTypeService.getById(dto.phoneType()));
                contactPhones.add(contactPhoneRepository.save(newPhone));
            }
        }
        return contactPhones;
    }



}
