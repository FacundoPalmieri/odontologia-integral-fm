package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.TelephoneType;
import com.odontologiaintegralfm.repository.ITelephoneTypeRepository;
import com.odontologiaintegralfm.service.interfaces.ITelephoneTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

@Service
public class TelephoneTypeService implements ITelephoneTypeService {
    @Autowired
    private ITelephoneTypeRepository telephoneTypeRepository;


    /**
     * Método para obtener el "tipo teléfono" de acuerdo al ID recibido como parámetro.
     *
     * @param id del tipo del tipo teléfono.
     * @return {@link TelephoneType} con el tipo de Teléfono encontrado.
     */
    @Override
    public TelephoneType getById(Long id) {
        try{
            return telephoneTypeRepository.findByIdAndEnabledTrue(id).orElseThrow(()-> new NotFoundException(null,"exception.telephoneTypeNotFound.user", null, "exception.telephoneTypeNotFound.log", id,null,"TelephoneTypeService","getById", LogLevel.ERROR));
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "TelephoneTypeService", id, null, "getById");
        }
    }
}
