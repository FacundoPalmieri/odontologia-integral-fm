package com.odontologiaintegralfm.feature.consultation.catalogs.service.interfaces;

import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.feature.consultation.catalogs.dto.TreatmentResponseDTO;
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
