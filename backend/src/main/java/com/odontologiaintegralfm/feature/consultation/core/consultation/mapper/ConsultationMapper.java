package com.odontologiaintegralfm.feature.consultation.core.consultation.mapper;


import com.odontologiaintegralfm.feature.consultation.core.consultation.model.Consultation;
import com.odontologiaintegralfm.feature.consultation.core.consultation.dto.ConsultationResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConsultationMapper {


    @Mapping(target = "appointmentId", expression = "java(consultation.getAppointment().getId())")
    @Mapping(target = "dateTime", expression = "java(consultation.getAppointment().getDate())")
    @Mapping(target = "patientId", expression = "java(consultation.getPatient().getId())")
    @Mapping(target = "patientName", expression = "java(consultation.getPatient().getPerson().getFullName())")
    @Mapping(target = "dentistName", expression = "java(consultation.getDentist().getPerson().getFullName())")
    @Mapping(target = "consultationStatus", expression = "java(consultation.getStatus().getLabel())")
    @Mapping(target = "webSocketStatus", expression = "java(consultation.getStatus().webSocketEvent().toString())")
    ConsultationResponseDTO toDTO(Consultation consultation);

    List<ConsultationResponseDTO>  toDTO(List<Consultation> consultations);
}