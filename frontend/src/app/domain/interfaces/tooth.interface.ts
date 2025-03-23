import { TreatmentInterface } from "./treatment.interface";

export interface ToothInterface {
  number: number;
  topTreatment?: TreatmentInterface;
  bottomTreatment?: TreatmentInterface;
  leftTreatment?: TreatmentInterface;
  rightTreatment?: TreatmentInterface;
  centerTreatment?: TreatmentInterface;
}
