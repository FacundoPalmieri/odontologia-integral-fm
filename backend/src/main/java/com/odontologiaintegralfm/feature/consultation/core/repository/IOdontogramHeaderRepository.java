package com.odontologiaintegralfm.feature.consultation.core.repository;


import com.odontologiaintegralfm.feature.consultation.core.model.OdontogramHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IOdontogramHeaderRepository extends JpaRepository<OdontogramHeader, Long> {

    //Obtiene la lista de Header asociados a una consulta (pueden ser enabled = true o false)
    List<OdontogramHeader> findAllByConsultationId(Long idConsultation);


    //Obtiene solo el encabezado vigente por ID de consulta.
    OdontogramHeader findByConsultationIdAndEnabledTrue(Long idConsultation);
}
