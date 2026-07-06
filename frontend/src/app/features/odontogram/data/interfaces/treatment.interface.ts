import {
  TreatmentEnum,
  TreatmentConditionEnum,
} from "../../utils/enums/treatment.enum";

export interface TreatmentInterfaceOld {
  name: TreatmentEnum;
  label: string;
  treatmentType: TreatmentConditionEnum;
  treatmentConditionName?: string;
  treatmentConditionColor?: string;
  bridgeStart?: number;
  bridgeEnd?: number;
  faces?: string[];
}

export interface ShowTreatmentInterface {
  name: TreatmentEnum;
  label: string;
  availableTypes: TreatmentConditionEnum[];
  faces?: string[];
  icons?: string[];
}

export interface TreatmentInterface {
  id: number;
  name: string;
  conditions: TreatmentConditionInterface[];
}

export interface TreatmentConditionInterface {
  id: number;
  name: string;
  color: string;
}
