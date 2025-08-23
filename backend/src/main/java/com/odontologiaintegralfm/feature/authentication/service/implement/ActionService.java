package com.odontologiaintegralfm.feature.authentication.service.implement;

import com.odontologiaintegralfm.feature.authentication.dto.ActionResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.feature.authentication.model.Action;
import com.odontologiaintegralfm.feature.authentication.repository.IActionRepository;
import com.odontologiaintegralfm.feature.authentication.service.interfaces.IActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class ActionService implements IActionService {

    @Autowired
    private IActionRepository actionRepository;

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
                    .map(data -> new ActionResponseDTO(data.getId(), data.getName(), data.getLabel()))
                    .collect(Collectors.toSet());
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"ActionService", null, null, "convertToDTO");
        }
    }

    /**
     * Método para obtener las acciones por ID
     *
     * @param id de la acción
     * @return
     */
    @Override
    public Action getById(Long id) {
        try{
            return actionRepository.findById(id)
                    .orElseThrow(()-> new NotFoundException("exception.actionNotfound.user", null,"exception.actionNotfound.log", new Object[]{id,"ActionService","getById"}, LogLevel.ERROR));

        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"ActionService", id, null, "getById");
        }
    }

    /**
     * Método para obtener un listado de acciones.
     *
     * @return
     */
    @Override
    public Response<List<ActionResponseDTO>> getAll() {
        try{
           List <Action> actionsList = actionRepository.findAll();

           List<ActionResponseDTO> actionsListDTO = actionsList.stream()
                   .map(obj -> new ActionResponseDTO(obj.getId(), obj.getName(), obj.getLabel()))
                   .toList();

           return new Response<>(true, null, actionsListDTO);

        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e,"ActionService", null, null, "getAll");
        }
    }

}
