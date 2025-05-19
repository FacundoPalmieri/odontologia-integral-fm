import { ToothFaceInterface } from "../../domain/interfaces/tooth.interface";

export class ToothFaceFactory {
  static createToothFaces(): ToothFaceInterface[] {
    return [
      { face: ToothFaceEnum.LINGUAL, label: "Lingual" },
      { face: ToothFaceEnum.DISTAL, label: "Distal" },
      { face: ToothFaceEnum.VESTIBULAR, label: "Vestibular" },
      { face: ToothFaceEnum.MESIAL, label: "Mesial" },
      { face: ToothFaceEnum.INCISAL, label: "Incisal" },
    ];
  }
}

export enum ToothFaceEnum {
  LINGUAL = "lingual",
  DISTAL = "distal",
  VESTIBULAR = "vestibular",
  MESIAL = "mesial",
  INCISAL = "incisal",
}
