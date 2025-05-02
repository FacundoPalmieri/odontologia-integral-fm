package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.GenderResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.enums.LogLevel;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.exception.NotFoundException;
import com.odontologiaintegralfm.model.Gender;
import com.odontologiaintegralfm.repository.IGenderRepository;
import com.odontologiaintegralfm.service.interfaces.IGenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.util.List;

@Service
public class GenderService implements IGenderService {
    @Autowired
    private IGenderRepository genderRepository;


    /**
     * Método para obtener todos los Géneros "Habilitados" de la aplicación
     *
     * @return Una lista de GenderResponseDTO
     */
    @Override
    public Response<List<GenderResponseDTO>> getAll() {
        try{
            List<Gender> listGender = genderRepository.findAllByEnabledTrue();
            List<GenderResponseDTO> listGenderDTOs = listGender.stream()
                    .map(gender -> new GenderResponseDTO(gender.getId(),gender.getAlias()))
                    .toList();

            return new Response<>(true,null,listGenderDTOs);

        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "GenderService", null, null, "getAll");
        }
    }

    /**
     * Método para obtener un Género "Habilitado" de acuerdo al ID recibido como parámetro.
     *
     * @param id del tipo de Género.
     * @return Gender con el tipo de Género encontrado.
     * @throws NotFoundException Si no encuentra el objeto
     * @throws DataBaseException Error de conexión a la base de datos.
     */
    @Override
    public Gender getById(Long id) {
        try{
            return genderRepository.findByIdAndEnabledTrue(id).orElseThrow(() -> new NotFoundException("exception.genderNotFound.user", null, "exception.genderNotFound.log", new Object[]{ id,"GenderService","getById"}, LogLevel.ERROR));
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "GenderService", id, null, "getById");
        }
    }
}
