import { PersonCreateDtoInterface, PersonDtoInterface } from "./person.dto";

export interface PatientDtoInterface {
  personDto: PersonDtoInterface;
  healthPlans: string;
  affiliateNumber: string;
  medicalHistoryRiskDto: MedicalHistoryRiskDtoInterface[];
}

export interface MedicalHistoryRiskDtoInterface {
  id: number;
  name: string;
  observation: string;
}

export interface PatientCreateDtoInterface {
  personDto: PersonCreateDtoInterface;
  healthPlanId: number;
  affiliateNumber: string;
  medicalRiskDto: MedicalRiskCreateDtoInterface[];
}

export interface MedicalRiskCreateDtoInterface {
  medicalRiskId: number;
  observation: string;
}
