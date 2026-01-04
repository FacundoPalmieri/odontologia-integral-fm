package com.odontologiaintegralfm.feature.consultation.core.service.impl;


import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.catalogs.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.catalogs.service.interfaces.ITreatmentService;
import com.odontologiaintegralfm.feature.consultation.core.dto.ToothDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.TreatmentRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationOdontogramDetail;
import com.odontologiaintegralfm.feature.consultation.core.model.ConsultationOdontogramHeader;
import com.odontologiaintegralfm.feature.consultation.core.repository.IConsultationOdontogramDetailRepository;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IConsultationOdontogramDetailsService;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsultationOdontogramDetailsService implements IConsultationOdontogramDetailsService {

    private final AuthenticatedUserService authenticatedUserService;
    private final IConsultationOdontogramDetailRepository consultationOdontogramDetailRepository;
    private final ITreatmentService treatmentService;


    public ConsultationOdontogramDetailsService(
            AuthenticatedUserService authenticatedUserService,
            IConsultationOdontogramDetailRepository consultationOdontogramDetailRepository,
            ITreatmentService treatmentService) {
        this.authenticatedUserService = authenticatedUserService;
        this.consultationOdontogramDetailRepository = consultationOdontogramDetailRepository;
        this.treatmentService = treatmentService;
    }

    /**
     * Deshabilita los odontogramas anteriores (debería ser solo uno el habilitado)
     */
    public void disable(ConsultationOdontogramHeader consultationOdontogramHeader) {

        // Damos de baja lógica al odontograma anterior.
        List<ConsultationOdontogramDetail> odontogramsDetailsPrevious = consultationOdontogramDetailRepository.findByOdontogramHeaderIdAndEnabledTrue(consultationOdontogramHeader.getId());

        for (ConsultationOdontogramDetail o : odontogramsDetailsPrevious) {
            o.setEnabled(false);
            o.setDisabledBy(authenticatedUserService.getAuthenticatedUser());
            o.setDisabledAt(LocalDateTime.now());
        }

        //Persistimos la actualización
        consultationOdontogramDetailRepository.saveAll(odontogramsDetailsPrevious);
    }





    /**
     * Crea el nuevo odontograma.
     */
    public void create(ConsultationOdontogramHeader odontogramHeader, List<ToothDTO> odontogram) {


        //Recuperamos todos los tratamientos para evitar múltiples llamadas a la BD dentro del foreach.
        Map<Long, Treatment> treatmentMap = treatmentService.getAll()
                .stream()
                .collect(Collectors.toMap(
                        treatment -> treatment.getId(),
                        treatment -> treatment
                ));


        // Mapeamos a la entidad.
        List<ConsultationOdontogramDetail> consultationOdontogramsNew = new ArrayList<>();

        for (ToothDTO o : odontogram) {

            //Obtenemos el tratamiento.
            for (TreatmentRequestDTO t : o.treatments()) {
                Treatment treatment = treatmentMap.get(t.idTreatment());
                if (treatment == null) {
                    throw new ConflictException("exception.treatment.notFound.user", null, "exception.treatment.notFound.log", new Object[]{t.idTreatment(), "ConsultationOdontogramHeaderService", "getAll"}, LogLevel.ERROR);
                }

                ConsultationOdontogramDetail consultationOdontogramDetail = ConsultationOdontogramDetail.build(
                        odontogramHeader,
                        o.tooth(),
                        t.toothFace(),
                        treatment
                );

                //Campos de auditoría.
                consultationOdontogramDetail.setCreatedAt(LocalDateTime.now());
                consultationOdontogramDetail.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
                consultationOdontogramDetail.setEnabled(true);

                consultationOdontogramsNew.add(consultationOdontogramDetail);


            }
        }
        consultationOdontogramDetailRepository.saveAll(consultationOdontogramsNew);
    }



}




