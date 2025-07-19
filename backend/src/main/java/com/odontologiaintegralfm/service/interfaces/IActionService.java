package com.odontologiaintegralfm.service.interfaces;


import com.odontologiaintegralfm.dto.ActionResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.model.Action;
import java.util.List;
import java.util.Set;

public interface IActionService {

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
    Set<ActionResponseDTO> convertToDTO(Set<Action> action);

    /**
     * Método para obtener las acciones por ID
     * @param id de la acción
     * @return
     */
    Action getById(Long id);


    /**
     * Método para obtener un listado de acciones.
     * @return
     */
    Response<List<ActionResponseDTO>> getAll();
}
