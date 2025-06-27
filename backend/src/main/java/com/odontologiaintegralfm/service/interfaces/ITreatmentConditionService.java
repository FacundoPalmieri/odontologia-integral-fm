package com.odontologiaintegralfm.service.interfaces;

import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.model.TreatmentCondition;
import org.springframework.data.domain.Page;

/**
 * Interfaz correspondiente a la condición de los tratamientos
 * 1.Pre-Existente.
 * 2.Requerido.
 */
public interface ITreatmentConditionService {

    Response<Page<TreatmentCondition>> getAll(int pageValue, int sizeValue, String sortByValue, String directionValue);

}
