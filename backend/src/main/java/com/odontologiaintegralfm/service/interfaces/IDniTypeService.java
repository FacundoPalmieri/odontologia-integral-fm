package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.DniTypeResponseDTO;
import com.odontologiaintegralfm.dto.Response;
import java.util.List;

public interface IDniTypeService {

    /**
     * Método para obtener listado de tipos de DNI.
     * @return DniTypeResponseDTO La lista de tipos de DNI
     */
    Response<List<DniTypeResponseDTO>> getAll();


}
