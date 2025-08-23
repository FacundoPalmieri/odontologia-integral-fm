package com.odontologiaintegralfm.feature.person.catalogs.service.implement;

import com.odontologiaintegralfm.feature.person.catalogs.dto.PhoneTypeResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.feature.person.catalogs.model.PhoneType;
import com.odontologiaintegralfm.feature.person.catalogs.repository.IPhoneTypeRepository;
import com.odontologiaintegralfm.feature.person.catalogs.service.interfaces.IPhoneTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PhoneTypeService implements IPhoneTypeService {
    @Autowired
    private IPhoneTypeRepository phoneTypeRepository;


    /**
     * Método para obtener el "tipo teléfono" de acuerdo al ID recibido como parámetro.
     *
     * @param id del tipo del tipo teléfono.
     * @return {@link PhoneType} con el tipo de Teléfono encontrado.
     */
    @Override
    public PhoneType getById(Long id) {
        try{
            return phoneTypeRepository.findByIdAndEnabledTrue(id).orElseThrow(()-> new NotFoundException("exception.phoneTypeNotFound.user", null, "exception.phoneTypeNotFound.log",new Object[]{id,"PhoneTypeService","getById"}, LogLevel.ERROR));
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "PhoneTypeService", id, null, "getById");
        }
    }

    /**
     * Método para obtener todos los "tipo teléfono" habilitados en la aplicación.
     *
     * @return Response<Set < PhoneType>> con el listado de tipos de teléfonos habilitados.
     */
    @Override
    public Response<Set<PhoneTypeResponseDTO>> getAll() {
        try{
            Set<PhoneType> phoneTypes = phoneTypeRepository.findAllByEnabledTrue();

            Set<PhoneTypeResponseDTO> phoneTypeResponseDTOSet = phoneTypes
                    .stream()
                    .map(phoneType -> new PhoneTypeResponseDTO(phoneType.getId(), phoneType.getName()))
                    .collect(Collectors.toSet());


            return new Response<>(true,null,phoneTypeResponseDTOSet);

        }catch(DataAccessException | CannotCreateTransactionException e){
           throw new DataBaseException(e,"PhoneTypeService",null,null,"getAll");
        }
    }
}
