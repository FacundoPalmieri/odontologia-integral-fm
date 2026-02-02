import { HealthPlanInterface } from "../../../../shared/interfaces/person-data.interface";
import { PersonInterface } from "../../../../shared/interfaces/person.interface";
import { MedicalHistoryRiskInterface } from "./medical-history.interface";

export interface PatientInterface {
  person: PersonInterface;
  medicalRisks: MedicalHistoryRiskInterface[];
  healthPlan: HealthPlanInterface;
  affiliateNumber: string;
}
