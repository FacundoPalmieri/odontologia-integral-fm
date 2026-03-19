import { ToothFaceInterface } from "../../data/interfaces/tooth.interface";
import { ToothFaceEnum } from "../enums/tooth-face.enum";

export class ToothFaceFactory {
  static createToothFaces(toothNumber: number): ToothFaceInterface[] {
    const isUpper =
      (toothNumber >= 11 && toothNumber <= 28) ||
      (toothNumber >= 51 && toothNumber <= 65);

    const faces: ToothFaceInterface[] = [];

    if (isUpper) {
      faces.push({ face: ToothFaceEnum.VESTIBULAR, label: "Vestibular" });
      faces.push({ face: ToothFaceEnum.PALATINO, label: "Palatino" });
    } else {
      faces.push({ face: ToothFaceEnum.LINGUAL, label: "Lingual" });
      faces.push({ face: ToothFaceEnum.VESTIBULAR, label: "Vestibular" });
    }

    faces.push({ face: ToothFaceEnum.MESIAL, label: "Mesial" });
    faces.push({ face: ToothFaceEnum.DISTAL, label: "Distal" });
    faces.push({ face: ToothFaceEnum.OCLUSAL, label: "Oclusal" });

    return faces;
  }
}
