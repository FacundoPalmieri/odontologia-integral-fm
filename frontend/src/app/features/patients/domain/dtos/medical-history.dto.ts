export interface MedicalHistoryRiskDto {
  id: number;
  name: string;
  observation: string;
}

export interface MedicalRiskCreateDto {
  medicalRiskId: number;
  observation: string;
}
