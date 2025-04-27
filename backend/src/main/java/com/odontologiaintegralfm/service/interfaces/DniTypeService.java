package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.DniTypeResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.DniType;
import com.odontologiaintegralfm.repository.IDniTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
@Service
public class DniTypeService implements IDniTypeService {
    @Autowired
    private IDniTypeRepository dniTypeRepository;

    /**
     * Método para obtener listado de tipos de DNI.
     *
     * @return DniTypeResponseDTO La lista de tipos de DNI
     */
    @Override
    public Response<List<DniTypeResponseDTO>> getAll() {
        try{
            List<DniType> dniTypes = dniTypeRepository.findAll();
            List<DniTypeResponseDTO> dniTypeResponseDTOS = dniTypes.stream()
                    .map(dniType -> new DniTypeResponseDTO(dniType.getId(),dniType.getName()))
                    .toList();
            return new Response<>(true, null, dniTypeResponseDTOS);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DniTypeService", null,null, "getAll");
        }
    }
}
