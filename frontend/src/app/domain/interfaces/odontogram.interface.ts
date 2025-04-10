import { ToothInterface } from "./tooth.interface";

export interface OdontogramInterface {
  upperTeethLeft: ToothInterface[];
  upperTeethRight: ToothInterface[];
  lowerTeethLeft: ToothInterface[];
  lowerTeethRight: ToothInterface[];
  temporaryUpperLeft: ToothInterface[];
  temporaryUpperRight: ToothInterface[];
  temporaryLowerLeft: ToothInterface[];
  temporaryLowerRight: ToothInterface[];
}
