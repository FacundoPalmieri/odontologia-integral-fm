package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.dto.TreatmentResponseDTO;
import org.springframework.data.domain.Page;

/**
 * Interfaz correspondiente a los tratamientos odontológicos.
 */
public interface ITreatmentService {

    /**
     * Método para obtener un Set de tratamientos "habilitados"
     * @return {@link TreatmentResponseDTO}
     */
 Response<Page<TreatmentResponseDTO>> getAll(int pageValue, int sizeValue, String sortByValue, String directionValue);
}
