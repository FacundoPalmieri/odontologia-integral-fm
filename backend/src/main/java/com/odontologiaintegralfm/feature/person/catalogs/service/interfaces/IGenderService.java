package com.odontologiaintegralfm.feature.person.catalogs.service.interfaces;

import com.odontologiaintegralfm.feature.person.catalogs.dto.GenderResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.feature.person.catalogs.model.Gender;

import java.util.List;

/**
 * @author [Facundo Palmieri]
 */
public interface IGenderService {

    /**
     * Método para obtener todos los Géneros "Habilitados" de la aplicación
     * @return Una lista de GenderResponseDTO
     */
    Response<List<GenderResponseDTO>> getAll();

    /**
     * Método para obtener un Género "Habilitado" de acuerdo al ID recibido como parámetro.
     * @param id del tipo de Género.
     * @return  {@link Gender} con el tipo de Género encontrado.
     */
    Gender getById(Long id);
}
