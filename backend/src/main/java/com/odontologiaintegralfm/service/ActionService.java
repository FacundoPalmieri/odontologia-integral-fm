package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.ActionResponseDTO;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.Action;
import com.odontologiaintegralfm.service.interfaces.IActionService;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.Set;
import java.util.stream.Collectors;


@Service
public class ActionService implements IActionService {

    /**
     * Convierte una entidad {@link Action} a un objeto de transferencia de datos (DTO).
     * <p>
     * Este método crea una instancia de {@link ActionResponseDTO} y transfiere los datos
     * desde la entidad {@link Action}.
     * </p>
     *
     * @param action La entidad {@link Action} a convertir.
     * @return Un objeto {@link ActionResponseDTO} con los datos de la entidad.
     */
    @Override
    public Set<ActionResponseDTO> convertToDTO(Set<Action> action) {
        try{

            return action.stream()
                    .map(data -> new ActionResponseDTO(data.getId(), data.getAction()))
                    .collect(Collectors.toSet());
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"ActionService", null, null, "convertToDTO");
        }
    }

}
