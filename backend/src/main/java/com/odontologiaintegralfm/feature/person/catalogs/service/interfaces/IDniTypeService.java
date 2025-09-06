package com.odontologiaintegralfm.feature.person.catalogs.service.interfaces;

import com.odontologiaintegralfm.feature.person.catalogs.dto.DniTypeResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.feature.person.catalogs.model.DniType;

import java.util.Set;

public interface IDniTypeService {

    /**
     * Método para obtener listado de tipos de DNI.
     * @return DniTypeResponseDTO La lista de tipos de DNI
     */
    Response<Set<DniTypeResponseDTO>> getAll();

    /**
     * Método para obtener un "tipo de identificación" de acuerdo al ID recibido.
     * @param id del tipo de identificación.
     * @return {@link DniType} con el tipo del tipo DNI encontrado.
     */
    DniType getById(Long id);


}
