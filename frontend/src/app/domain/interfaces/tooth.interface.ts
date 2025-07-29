import { ToothFaceEnum } from "../../utils/factories/tooth-face.factory";
import { TreatmentInterfaceOld } from "./treatment.interface";

export interface ToothInterface {
  number: number;
  treatments?: TreatmentInterfaceOld[];
}

export interface ToothFaceInterface {
  face: ToothFaceEnum;
  label: string;
}
