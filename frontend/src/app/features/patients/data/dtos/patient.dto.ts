import { PersonCreateDto, PersonDto } from "../../../../shared/dtos/person.dto";
import {
  MedicalHistoryRiskDto,
  MedicalRiskCreateDto,
} from "./medical-history.dto";

export interface PatientDto {
  person: PersonDto;
  healthPlans: string;
  affiliateNumber: string;
  medicalHistoryRisk: MedicalHistoryRiskDto[];
  avatarUrl?: string;
}

export interface PatientCreateDto {
  person: PersonCreateDto;
  healthPlanId: number;
  affiliateNumber: string;
  medicalRisk: MedicalRiskCreateDto[];
}
