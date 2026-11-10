import {
  TreatmentEnum,
  TreatmentConditionEnum,
} from "../../utils/enums/treatment.enum";

export interface TreatmentInterfaceOld {
  name: TreatmentEnum;
  label: string;
  treatmentType: TreatmentConditionEnum;
  bridgeStart?: number;
  bridgeEnd?: number;
  faces?: string[];
  /** Id of the saved treatment instance; absent for treatments added in this consultation. */
  instanceId?: number;
}

export interface ShowTreatmentInterface {
  name: TreatmentEnum;
  label: string;
  faces?: string[];
  icons?: string[];
}
