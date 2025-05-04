package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.GenderResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.model.Gender;

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
