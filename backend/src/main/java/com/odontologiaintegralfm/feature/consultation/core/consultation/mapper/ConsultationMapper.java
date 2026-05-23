package com.odontologiaintegralfm.feature.consultation.core.consultation.mapper;


import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConsultationMapper {

    @Mapping(target = "patientName", expression = "java(consultation.getPatient().getPerson().getFullName())")
    @Mapping(target = "dentistName",  expression = "java(consultation.getDentist().getPerson().getFullName())")
    @Mapping(target = "consultationStatus", expression = "java(consultation.getStatus().getLabel())")
    ConsultationResponseDTO toDTO(Consultation consultation);

    @Mapping(target = "patientName", expression = "java(consultation.getPatient().getPerson().getFullName())")
    @Mapping(target = "dentistName",  expression = "java(consultation.getDentist().getPerson().getFullName())")
    @Mapping(target = "consultationStatus", expression = "java(consultation.getStatus().getLabel())")
    List<ConsultationResponseDTO>  toDTO(List<Consultation> consultations);
}