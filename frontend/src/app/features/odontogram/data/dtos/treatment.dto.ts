export interface TreatmentsResponse {
  content: TreatmentDto[];
}

export interface TreatmentDto {
  id: number;
  name: string;
  label: string;
  conditions: TreatmentConditionDto[];
}

export interface TreatmentConditionDto {
  id: number;
  name: string;
  color: string;
}
