import { TreatmentInterface } from "./treatment.interface";

export interface ToothInterface {
  number: number;
  topTreatments?: TreatmentInterface[];
  bottomTreatment?: TreatmentInterface;
  leftTreatment?: TreatmentInterface;
  rightTreatment?: TreatmentInterface;
  centerTreatment?: TreatmentInterface;
  fullToothTreatment?: TreatmentInterface;
}
