package com.odontologiaintegralfm.feature.person.person.service;


import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.feature.person.person.dto.NationalityResponseDTO;
import com.odontologiaintegralfm.feature.person.person.model.Nationality;

import java.util.List;

public interface INationalityService {

    /**
     * Obtiene una lista con todas las nacionalidades "Habilitiadas".
     * @return lista de NationalityResponseDTO
     */
    Response<List<NationalityResponseDTO>> getAll();

    /**
     * Método para obtener la "Nacionalidad habilitada" de acuerdo al ID recibido como parámetro.
     * @param id del tipo de Nacionalidad.
     * @return  {@link Nationality} con el tipo de Nacionalidad encontrado.
     */
    Nationality getById(Long id);
}
