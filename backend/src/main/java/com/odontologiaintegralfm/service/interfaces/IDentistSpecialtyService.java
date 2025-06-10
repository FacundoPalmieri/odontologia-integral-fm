package com.odontologiaintegralfm.service.interfaces;


import com.odontologiaintegralfm.model.DentistSpecialty;


public interface IDentistSpecialtyService {

    /**
     * Método para obtener una Especialidad "habilitada" de acuerdo al ID recibido como parámetro.
     * @param id del tipo de Especialidad.
     * @return  {@link DentistSpecialty} con el tipo de Especialidad.
     */
    DentistSpecialty getById(Long id);

}
