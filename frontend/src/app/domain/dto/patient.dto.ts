import { PersonCreateDtoInterface, PersonDtoInterface } from "./person.dto";

export interface PatientDtoInterface {
  person: PersonDtoInterface;
  healthPlans: string;
  affiliateNumber: string;
  medicalHistoryRisk: MedicalHistoryRiskDtoInterface[];
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
