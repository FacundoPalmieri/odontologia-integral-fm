package com.odontologiaintegralfm.feature.consultation.core.consultationinstance.repository;

import com.odontologiaintegralfm.feature.consultation.core.consultationinstance.model.ConsultationInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IConsultationInstanceRepository extends JpaRepository<ConsultationInstance, Long> {

    ConsultationInstance findByConsultationId(Long consultationId);

    List<ConsultationInstance> findByConsultationPatientId(Long patientId);

}
