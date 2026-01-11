package com.odontologiaintegralfm.feature.consultation.catalogs.service.interfaces;

import com.odontologiaintegralfm.feature.consultation.catalogs.model.Treatment;
import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.feature.consultation.catalogs.dto.TreatmentResponseDTO;
import org.springframework.data.domain.Page;
import java.util.List;


/**
 * Interfaz correspondiente a los tratamientos odontológicos.
 */
public interface ITreatmentService {

    /**
     * Método para obtener un Set de tratamientos "habilitados" y paginados para el cliente.
     * @return {@link TreatmentResponseDTO}
     */
    Response<Page<TreatmentResponseDTO>> getAll(int pageValue, int sizeValue, String sortByValue, String directionValue);

    /**
     * Obtiene todos los tratamientos "habilitdos" en una lista para uso interno.
     */
    List<Treatment> getAll();

    /**
     * Obtiene tratamiento "habilitado" por su Id. Si no encuentra arroja exception
     * @param id : id del tratamiento.
     */
    Treatment getById(Long id);

}
