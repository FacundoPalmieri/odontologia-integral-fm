import { ShowTreatmentInterface } from "../../domain/interfaces/treatment.interface";
import { TreatmentEnum, TreatmentTypeEnum } from "../enums/treatment.enum";

export class TreatmentFactory {
  static createTreatments(): ShowTreatmentInterface[] {
    return [
      {
        name: TreatmentEnum.CARIES,
        label: "Caries",
        availableTypes: [TreatmentTypeEnum.REQUIRED],
        icons: ["circle-filled"],
      },
      {
        name: TreatmentEnum.TRAT_DE_CONDUCTO,
        label: "Trat. de Conducto",
        availableTypes: [
          TreatmentTypeEnum.REQUIRED,
          TreatmentTypeEnum.EXISTING,
        ],
        icons: ["letter-t-small", "letter-c-small"],
      },
      {
        name: TreatmentEnum.OBT_COMPOSITE,
        label: "Obt. Composite",
        availableTypes: [TreatmentTypeEnum.EXISTING],
        icons: ["circle-filled"],
      },
      {
        name: TreatmentEnum.DIENTE_AUSENTE,
        label: "Diente Ausente",
        availableTypes: [TreatmentTypeEnum.EXISTING],
        icons: ["x"],
      },
      {
        name: TreatmentEnum.CORONA,
        label: "Corona",
        availableTypes: [
          TreatmentTypeEnum.EXISTING,
          TreatmentTypeEnum.REQUIRED,
        ],
        icons: ["circle"],
      },
      {
        name: TreatmentEnum.PUENTE,
        label: "Puente",
        availableTypes: [TreatmentTypeEnum.EXISTING],
        icons: ["building-bridge-2"],
      },
      {
        name: TreatmentEnum.EXTRACCION,
        label: "Extracción",
        availableTypes: [TreatmentTypeEnum.REQUIRED],
        icons: ["equal"],
      },
      {
        name: TreatmentEnum.IMPLANTES,
        label: "Implantes",
        availableTypes: [TreatmentTypeEnum.EXISTING],
        icons: ["letter-i-small", "letter-m-small"],
      },
    ];
  }
}
