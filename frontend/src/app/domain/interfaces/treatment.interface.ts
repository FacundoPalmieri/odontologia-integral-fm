import {
  TreatmentEnum,
  TreatmentTypeEnum,
} from "../../utils/enums/treatment.enum";

export interface TreatmentInterface {
  name: TreatmentEnum;
  label: string;
  treatmentType: TreatmentTypeEnum;
  bridgeStart?: number;
  bridgeEnd?: number;
  faces?: string[];
}

export interface ShowTreatmentInterface {
  name: TreatmentEnum;
  label: string;
  availableTypes: TreatmentTypeEnum[];
  faces?: string[];
  icons?: string[];
}
