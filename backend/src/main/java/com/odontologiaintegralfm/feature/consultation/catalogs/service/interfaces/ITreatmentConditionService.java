package com.odontologiaintegralfm.feature.consultation.catalogs.service.interfaces;

import com.odontologiaintegralfm.shared.response.Response;
import com.odontologiaintegralfm.feature.consultation.catalogs.model.TreatmentCondition;
import org.springframework.data.domain.Page;

/**
 * Interfaz correspondiente a la condición de los tratamientos
 * 1.Pre-Existente.
 * 2.Requerido.
 */
public interface ITreatmentConditionService {

    Response<Page<TreatmentCondition>> getAll(int pageValue, int sizeValue, String sortByValue, String directionValue);

}
