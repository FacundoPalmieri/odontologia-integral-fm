package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.PhoneTypeResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.model.PhoneType;

import java.util.Set;

public interface IPhoneTypeService {

    /**
     * Método para obtener el "tipo teléfono" de acuerdo al ID recibido como parámetro.
     * @param id del tipo del tipo teléfono.
     * @return  {@link PhoneType} con el tipo de Teléfono encontrado.
     */
    PhoneType getById(Long id);

    /**
     * Método para obtener todos los "tipo teléfono" habilitados en la aplicación.
     * @return  Response<Set<PhoneType>> con el listado de tipos de teléfonos habilitados.
     */
    Response<Set<PhoneTypeResponseDTO>> getAll();
}
