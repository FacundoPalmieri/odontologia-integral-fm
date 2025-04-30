package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.Nationality;
import com.odontologiaintegralfm.dto.NationalityResponseDTO;
import com.odontologiaintegralfm.repository.INationalityRepository;
import com.odontologiaintegralfm.service.interfaces.INationalityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.List;

@Service
public class NationalityService implements INationalityService {
    @Autowired
    private INationalityRepository nationalityRepository;

    /**
     * Obtiene una lista con todas las nacionalidades "Habilitiadas".
     *
     * @return lista de NationalityResponseDTO
     */
    @Override
    public Response<List<NationalityResponseDTO>> getAll() {
        try{
            List<Nationality> nationalityList = nationalityRepository.findAllByEnabledTrue();
            List<NationalityResponseDTO> nationalityResponseDTOList = nationalityList.stream()
                    .map(nationality -> new NationalityResponseDTO(nationality.getId(),nationality.getName()))
                    .toList();
            return new Response<>(true, null, nationalityResponseDTOList);
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "NationalityService", null,null, "getAll");
        }
    }

    /**
     * Método para obtener la "Nacionalidad habilitada" de acuerdo al ID recibido como parámetro.
     *
     * @param id del tipo de Nacionalidad.
     * @return {@link Nationality} con el tipo de Nacionalidad encontrado.
     */
    @Override
    public Nationality getById(Long id) {
        try{
            return nationalityRepository.findByIdAndEnabledTrue(id).orElseThrow(()-> new NotFoundException(null,"exception.nationalityNotFound.user", null, "exception.nationalityNotFound.log", id,null,"NationalityService","getById", LogLevel.ERROR));
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "NationalityService", id,null, "getById");
        }
    }
}
