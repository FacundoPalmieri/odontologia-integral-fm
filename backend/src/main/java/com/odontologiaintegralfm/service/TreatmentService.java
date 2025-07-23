package com.odontologiaintegralfm.service;

import com.odontologiaintegralfm.dto.Response;
import com.odontologiaintegralfm.dto.TreatmentResponseDTO;
import com.odontologiaintegralfm.exception.DataBaseException;
import com.odontologiaintegralfm.model.Treatment;
import com.odontologiaintegralfm.repository.ITreatmentRepository;
import com.odontologiaintegralfm.service.interfaces.ITreatmentService;
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
public class TreatmentService implements ITreatmentService {

    @Autowired
    private ITreatmentRepository treatmentRepository;

    /**
     * Método para obtener un Set de tratamientos "habilitados"
     *
     * @return {@link TreatmentResponseDTO}
     */
    @Override
    public  Response<Page<TreatmentResponseDTO>> getAll(int pageValue,int sizeValue, String sortBy, String direction) {
       try{
           //Define criterio de ordenamiento
           Sort sort = direction.equalsIgnoreCase("desc")
                   ? Sort.by(sortBy).descending()
                   : Sort.by(sortBy).ascending();


           //Se define paginación con n°página, cantidad elementos y ordenamiento.
           Pageable pageable = PageRequest.of(pageValue,sizeValue,sort);


           Page<Treatment> treatments = treatmentRepository.findAllByEnabledTrue(pageable);
           Page<TreatmentResponseDTO> treatmentResponseDTO = treatments.map(treatment -> {
               System.out.println("Treatment ID: " + treatment.getId() + " - Conditions size: " + treatment.getCondition().size());
               return new TreatmentResponseDTO(treatment.getId(), treatment.getName(), treatment.getCondition());
           });


           return new Response<>(true,null, treatmentResponseDTO);
       }catch(DataAccessException | CannotCreateTransactionException e){
           throw new DataBaseException(e, "TreatmentService", null, null, "getAll");
       }
    }
}
