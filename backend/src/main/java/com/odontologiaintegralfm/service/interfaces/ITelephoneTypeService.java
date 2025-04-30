package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.model.TelephoneType;

public interface ITelephoneTypeService {

    /**
     * Método para obtener el "tipo teléfono" de acuerdo al ID recibido como parámetro.
     * @param id del tipo del tipo teléfono.
     * @return  {@link TelephoneType} con el tipo de Teléfono encontrado.
     */
    TelephoneType getById(Long id);
}
