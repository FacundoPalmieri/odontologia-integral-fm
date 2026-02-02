import { TreatmentInterfaceOld } from "./treatment.interface";
import { ToothFaceEnum } from "../../utils/enums/tooth-face.enum";

export interface ToothInterface {
  number: number;
  treatments?: TreatmentInterfaceOld[];
}

export interface ToothFaceInterface {
  face: ToothFaceEnum;
  label: string;
}
