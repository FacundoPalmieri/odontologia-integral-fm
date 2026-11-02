package com.odontologiaintegralfm.feature.consultation.treatment.service;

import com.odontologiaintegralfm.shared.dto.Response;
import com.odontologiaintegralfm.feature.consultation.treatment.dto.TreatmentResponseDTO;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.DataBaseException;
import com.odontologiaintegralfm.shared.exception.NotFoundException;
import com.odontologiaintegralfm.feature.consultation.treatment.mapper.TreatmentMapper;
import com.odontologiaintegralfm.feature.consultation.treatment.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.treatment.repository.ITreatmentRepository;
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

    private final TreatmentMapper treatmentMapper;

    public TreatmentService(ITreatmentRepository treatmentRepository, TreatmentMapper treatmentMapper) {
        this.treatmentRepository = treatmentRepository;
        this.treatmentMapper = treatmentMapper;
    }


    /**
     * Método para obtener un Set de tratamientos "habilitados"
     *
     * @return {@link TreatmentResponseDTO}
     */
    public  Response<Page<TreatmentResponseDTO>> findAll(int pageValue, int sizeValue, String sortBy, String direction) {
       try{
           //Define criterio de ordenamiento
           Sort sort = direction.equalsIgnoreCase("desc")
                   ? Sort.by(sortBy).descending()
                   : Sort.by(sortBy).ascending();


           //Se define paginación con n°página, cantidad elementos y ordenamiento.
           Pageable pageable = PageRequest.of(pageValue,sizeValue,sort);


           Page<Treatment> treatments = treatmentRepository.findAllByEnabledTrue(pageable);
           Page<TreatmentResponseDTO> treatmentResponseDTO = treatments.map(treatmentMapper::toDTO);


           return new Response<>(true,null, treatmentResponseDTO);
       }catch(DataAccessException | CannotCreateTransactionException e){
           throw new DataBaseException(e, "TreatmentService", null, null, "getAll");
       }
    }

    /**
     * Obtiene todos los tratamientos "habilitdos" en una lista para uso interno.
     */
    public List<Treatment> findAll() {
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
    public Treatment findById(Long id) {
        try{
            return treatmentRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("exception.treatment.notFound.user",null,"exception.treatment.notFound.log",new Object[]{id,"TreatmentService","getById" }, LogLevel.ERROR));
        }catch(DataAccessException | CannotCreateTransactionException e){
            throw new DataBaseException(e, "TreatmentService", id, null, "getById");
        }
    }
}
