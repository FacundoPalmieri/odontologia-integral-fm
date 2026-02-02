import { ToothFaceInterface } from "../../domain/interfaces/tooth.interface";
import { ToothFaceEnum } from "../enums/tooth-face.enum";

export class ToothFaceFactory {
  static createToothFaces(): ToothFaceInterface[] {
    return [
      { face: ToothFaceEnum.LINGUAL, label: "Lingual" },
      { face: ToothFaceEnum.DISTAL, label: "Distal" },
      { face: ToothFaceEnum.PALATINO, label: "Palatino" },
      { face: ToothFaceEnum.MESIAL, label: "Mesial" },
      { face: ToothFaceEnum.OCLUSAL, label: "Oclusal" },
    ];
  }
}
