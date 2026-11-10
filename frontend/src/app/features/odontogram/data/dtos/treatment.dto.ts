import { TreatmentConditionEnum } from "../../utils/enums/treatment.enum";

export interface TreatmentsResponse {
  content: TreatmentDto[];
}

export interface TreatmentDto {
  id: number;
  name: string;
  label: string;
  conditions: TreatmentConditionEnum[];
}
