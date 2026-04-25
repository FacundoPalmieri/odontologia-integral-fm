package com.odontologiaintegralfm.feature.consultation.catalogs.treatment.service;

import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.dto.TreatmentResponseDTO;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.catalogs.treatment.repository.ITreatmentRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import java.util.List;


/**
 * @author [Facundo Palmieri]
 */
@Service
public class TreatmentService  {

    private final  ITreatmentRepository treatmentRepository;

    public TreatmentService(ITreatmentRepository treatmentRepository) {
        this.treatmentRepository = treatmentRepository;
    }


    /**
     * Método para obtener un Set de tratamientos "habilitados"
     *
     * @return {@link TreatmentResponseDTO}
     */
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

    /**
     * Obtiene todos los tratamientos "habilitdos" en una lista para uso interno.
     */
    public List<Treatment> getAll() {
        try{
            return treatmentRepository.findAll();
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "TreatmentService", null, null, "getAll");
        }
    }

    /**
     * Obtiene tratamiento "habilitado" por su Id. Si no encuentra arroja exception
     *
     * @param id : id del tratamiento.
     */
    public Treatment getById(Long id) {
        try{
            return treatmentRepository.findById(id)
                    .orElseThrow(() -> new ConflictException("exception.treatment.notFound.user",null,"exception.treatment.notFound.log",new Object[]{id,"TreatmentService","getById" }, LogLevel.ERROR));
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "TreatmentService", id, null, "getById");
        }
    }
}
