package com.odontologiaintegralfm.feature.dentist.catalogs.service.interaces;


import com.odontologiaintegralfm.feature.dentist.catalogs.dto.DentistSpecialtyResponseDTO;
import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.feature.dentist.catalogs.model.DentistSpecialty;
import java.util.List;


public interface IDentistSpecialtyService {

    /**
     * Método para obtener una Especialidad "habilitada" de acuerdo al ID recibido como parámetro.
     * @param id del tipo de Especialidad.
     * @return  {@link DentistSpecialty} con el tipo de Especialidad.
     */
    DentistSpecialty getById(Long id);

    /**
     * Método para obtener todas las especialidades de odontólogos "habilitadas"
     * @return Response<List<DentistSpecialty>>
     */
    Response<List<DentistSpecialtyResponseDTO>> getAll();

}
