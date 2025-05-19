import { ToothFaceEnum } from "../../utils/factories/tooth-face.factory";
import { TreatmentInterface } from "./treatment.interface";

export interface ToothInterface {
  number: number;
  treatments?: TreatmentInterface[];
}

export interface ToothFaceInterface {
  face: ToothFaceEnum;
  label: string;
}
