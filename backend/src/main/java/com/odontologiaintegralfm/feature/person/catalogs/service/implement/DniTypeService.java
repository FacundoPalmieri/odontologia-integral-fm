package com.odontologiaintegralfm.feature.person.catalogs.service.implement;

import com.odontologiaintegralfm.feature.person.catalogs.dto.DniTypeResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.feature.person.catalogs.model.DniType;
import com.odontologiaintegralfm.feature.person.catalogs.repository.IDniTypeRepository;
import com.odontologiaintegralfm.feature.person.catalogs.service.interfaces.IDniTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Response<Set<DniTypeResponseDTO>> getAll() {
        try{
            Set<DniType> dniTypes = dniTypeRepository.findAllByEnabledTrue();
            Set<DniTypeResponseDTO> dniTypeResponseDTOS = dniTypes.stream()
                    .map(dniType -> new DniTypeResponseDTO(dniType.getId(),dniType.getName()))
                    .collect(Collectors.toSet());
            return new Response<>(true, null, dniTypeResponseDTOS);

        }catch (DataAccessException | CannotCreateTransactionException e) {
            throw new DataBaseException(e, "DniTypeService", null,null, "getAll");
        }
    }


    /**
     * Método para obtener un "tipo de identificación"
     *
     * @param id del tipo de identificación.
     * @return el objeto DniType
     * @throws NotFoundException Si no encuentra el objeto
     * @throws DataBaseException Error de conexión a la base de datos.
     */
    @Override
    public DniType getById(Long id) {
        try{
            return dniTypeRepository.findByIdAndEnabledTrue(id).orElseThrow(()-> new NotFoundException("exception.dniTypeNotFound.user", null, "exception.dniTypeNotFound.log", new Object[]{id,"DniTypeService","getById"}, LogLevel.ERROR));
        }catch (DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "DniTypeService", id,null, "getById");
        }
    }
}
