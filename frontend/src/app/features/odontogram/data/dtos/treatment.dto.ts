export interface TreatmentDto {
  id: number;
  name: string;
  conditions: TreatmentConditionDto[];
}

export interface TreatmentConditionDto {
  id: number;
  name: string;
  color: string;
}
