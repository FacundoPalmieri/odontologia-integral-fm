package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.dto.TreatmentResponseDTO;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.TreatmentCondition;
import com.odontologiaintegralfm.repository.ITreatmentConditionRepository;
import com.odontologiaintegralfm.service.interfaces.ITreatmentConditionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;

/**
 * @author [Facundo Palmieri]
 */
@Service
public class TreatmentConditionService implements ITreatmentConditionService {

    @Autowired
    private ITreatmentConditionRepository treatmentConditionRepository;

    @Override
    public Response<Page<TreatmentCondition>> getAll(int page, int size, String sortBy, String direction) {
        try{

            //Define criterio de ordenamiento
            Sort sort = direction.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            //Se define paginación con n°página, cantidad elementos y ordenamiento.
            Pageable pageable = PageRequest.of(page, size, sort);

          Page<TreatmentCondition> treatmentConditions =  treatmentConditionRepository.findAll(pageable);

          return new Response<>(true,null , treatmentConditions);


        }catch(DataAccessException | CannotCreateTransactionException e){
         throw new DataBaseException(e, "TreatmentConditionService", null, null, "getAll");
        }
    }
}
