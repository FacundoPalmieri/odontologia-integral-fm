import { PersonCreateDtoInterface, PersonDtoInterface } from "./person.dto";

export interface PatientDtoInterface {
  person: PersonDtoInterface;
  healthPlans: string;
  affiliateNumber: string;
  medicalHistoryRisk: MedicalHistoryRiskDtoInterface[];
  avatarUrl?: Blob; //Para cargar en la grillas
}

export interface MedicalHistoryRiskDtoInterface {
  id: number;
  name: string;
  observation: string;
}

export interface PatientCreateDtoInterface {
  person: PersonCreateDtoInterface;
  healthPlanId: number;
  affiliateNumber: string;
  medicalRisk: MedicalRiskCreateDtoInterface[];
}

export interface MedicalRiskCreateDtoInterface {
  medicalRiskId: number;
  observation: string;
}
