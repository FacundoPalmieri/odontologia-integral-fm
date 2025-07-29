export interface TreatmentDtoInterface {
  id: number;
  name: string;
  conditions: TreatmentConditionDtoInterface[];
}

export interface TreatmentConditionDtoInterface {
  id: number;
  name: string;
  color: string;
}
