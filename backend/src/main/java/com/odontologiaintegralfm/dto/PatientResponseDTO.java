package com.odontologiaintegralfm.dto;



import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class  PatientResponseDTO{
       private  PersonResponseDTO person;
       private  String healthPlans;
       private  String affiliateNumber;
       private  Set<PatientMedicalRiskResponseDTO> medicalHistoryRisk;
}
