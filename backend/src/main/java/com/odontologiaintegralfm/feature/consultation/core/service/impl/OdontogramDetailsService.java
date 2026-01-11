package com.odontologiaintegralfm.feature.consultation.core.service.impl;


import com.odontologiaintegralfm.configuration.securityconfig.core.AuthenticatedUserService;
import com.odontologiaintegralfm.feature.consultation.catalogs.model.Treatment;
import com.odontologiaintegralfm.feature.consultation.catalogs.model.TreatmentCondition;
import com.odontologiaintegralfm.feature.consultation.catalogs.service.implement.TreatmentConditionService;
import com.odontologiaintegralfm.feature.consultation.catalogs.service.interfaces.ITreatmentService;
import com.odontologiaintegralfm.feature.consultation.core.dto.ToothRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.dto.TreatmentRequestDTO;
import com.odontologiaintegralfm.feature.consultation.core.model.OdontogramDetail;
import com.odontologiaintegralfm.feature.consultation.core.model.OdontogramHeader;
import com.odontologiaintegralfm.feature.consultation.core.repository.IOdontogramDetailRepository;
import com.odontologiaintegralfm.feature.consultation.core.service.interfaces.IOdontogramDetailsService;
import com.odontologiaintegralfm.shared.enums.LogLevel;
import com.odontologiaintegralfm.shared.exception.ConflictException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OdontogramDetailsService implements IOdontogramDetailsService {

    private final AuthenticatedUserService authenticatedUserService;
    private final IOdontogramDetailRepository odontogramDetailRepository;
    private final ITreatmentService treatmentService;
    private final TreatmentConditionService treatmentConditionService;


    public OdontogramDetailsService(
            AuthenticatedUserService authenticatedUserService,
            IOdontogramDetailRepository odontogramDetailRepository,
            ITreatmentService treatmentService, TreatmentConditionService treatmentConditionService) {
        this.authenticatedUserService = authenticatedUserService;
        this.odontogramDetailRepository = odontogramDetailRepository;
        this.treatmentService = treatmentService;
        this.treatmentConditionService = treatmentConditionService;
    }

    /**
     * Deshabilita los odontogramas anteriores (debería ser solo uno el habilitado)
     */
    public void disable(OdontogramHeader odontogramHeader) {

        // Damos de baja lógica al odontograma anterior.
        List<OdontogramDetail> odontogramsDetailsPrevious = odontogramDetailRepository.findByOdontogramHeaderIdAndEnabledTrue(odontogramHeader.getId());

        for (OdontogramDetail o : odontogramsDetailsPrevious) {
            o.setEnabled(false);
            o.setDisabledBy(authenticatedUserService.getAuthenticatedUser());
            o.setDisabledAt(LocalDateTime.now());
        }

        //Persistimos la actualización
        odontogramDetailRepository.saveAll(odontogramsDetailsPrevious);
    }





    /**
     * Crea el nuevo odontograma.
     */
    public void create(OdontogramHeader odontogramHeader, List<ToothRequestDTO> odontogram) {


        //Recuperamos todos los tratamientos para evitar múltiples llamadas a la BD dentro del foreach.
        Map<Long, Treatment> treatmentMap = treatmentService.getAll()
                .stream()
                .collect(Collectors.toMap(
                        treatment -> treatment.getId(),
                        treatment -> treatment
                ));

        //Recuperamos las condiciones de tratamiento para evitar múltiples llamadas a la base
        List <TreatmentCondition> treatmentCondition = treatmentConditionService.getAll();

        Map<Long, TreatmentCondition> treatmentConditionMap = treatmentCondition.stream().
                collect(Collectors.toMap(
                        tcondition -> tcondition.getId(),
                        tcondition -> tcondition
                ));






        // Mapeamos a la entidad.
        List<OdontogramDetail> consultationOdontogramsNew = new ArrayList<>();

        for (ToothRequestDTO o : odontogram) {

            //Obtenemos el tratamiento.
            for (TreatmentRequestDTO t : o.treatments()) {
                Treatment treatment = treatmentMap.get(t.idTreatment());
                if (treatment == null) {
                    throw new ConflictException("exception.treatment.notFound.user", null, "exception.treatment.notFound.log", new Object[]{t.idTreatment(), "OdontogramHeaderService", "getAll"}, LogLevel.ERROR);
                }

                OdontogramDetail odontogramDetail = OdontogramDetail.build(
                        odontogramHeader,
                        o.tooth(),
                        t.toothFace(),
                        treatment,
                        treatmentConditionMap.get(t.idTreatmentCondition())
                );

                //Campos de auditoría.
                odontogramDetail.setCreatedAt(LocalDateTime.now());
                odontogramDetail.setCreatedBy(authenticatedUserService.getAuthenticatedUser());
                odontogramDetail.setEnabled(true);

                consultationOdontogramsNew.add(odontogramDetail);


            }
        }
        odontogramDetailRepository.saveAll(consultationOdontogramsNew);
    }



    /**
     * Obtiene los detalles de un odontograma vigente, por ID de encabezado.
     *
     * @param idOdontogramHeader : Id del encabezado del odontograma.
     */
    @Override
    public List<OdontogramDetail> getByIdHeader(Long idOdontogramHeader) {
        return odontogramDetailRepository.findByOdontogramHeaderIdAndEnabledTrue(idOdontogramHeader);
    }


}




