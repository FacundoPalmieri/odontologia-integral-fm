import {
  DisseaseConditionEnum,
  DisseaseEnum,
} from "../../utils/enums/dissease.enum";
import { HealthPlanInterface } from "./person-data.interface";
import { PersonInterface } from "./person.interface";

export interface PatientInterface extends PersonInterface {
  medicalRisks: MedicalHistoryRiskInterface;
  healthPlan: HealthPlanInterface;
  affiliateNumber: string;
}

export interface MedicalHistoryRiskInterface {
  id: number;
  name: string;
  observation: string;
}

export interface DisseaseInterface {
  dissease: DisseaseTypeInterface;
  condition?: DisseaseConditionInterface[];
  type?: string;
  medicament?: string;
  description?: string;
}

export interface DisseaseTypeInterface {
  dissease: DisseaseEnum;
  name: string;
}

export interface DisseaseConditionInterface {
  condition: DisseaseConditionEnum;
  name: string;
}
