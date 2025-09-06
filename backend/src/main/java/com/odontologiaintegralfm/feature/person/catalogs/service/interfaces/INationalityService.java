package com.odontologiaintegralfm.feature.person.catalogs.service.interfaces;


import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.feature.person.catalogs.dto.NationalityResponseDTO;
import com.odontologiaintegralfm.feature.person.catalogs.model.Nationality;

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
