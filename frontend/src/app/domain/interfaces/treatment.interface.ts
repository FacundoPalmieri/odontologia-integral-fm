import {
  TreatmentEnum,
  TreatmentTypeEnum,
} from "../../utils/enums/treatment.enum";

export interface TreatmentInterfaceOld {
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
