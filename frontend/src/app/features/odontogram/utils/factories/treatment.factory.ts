import { ShowTreatmentInterface } from "../../data/interfaces/treatment.interface";
import { TreatmentEnum, TreatmentConditionEnum } from "../enums/treatment.enum";

export class TreatmentFactory {
  static createTreatments(): ShowTreatmentInterface[] {
    return [
      {
        name: TreatmentEnum.CARIES,
        label: "Caries",
        availableTypes: [TreatmentConditionEnum.REQUIRED],
        icons: ["circle-filled"],
      },
      {
        name: TreatmentEnum.TRATAMIENTO_CONDUCTO,
        label: "Trat. de Conducto",
        availableTypes: [
          TreatmentConditionEnum.REQUIRED,
          TreatmentConditionEnum.EXISTING,
        ],
        icons: ["letter-t-small", "letter-c-small"],
      },
      {
        name: TreatmentEnum.OBTURACION_COMPOSITE,
        label: "Obt. Composite",
        availableTypes: [TreatmentConditionEnum.EXISTING],
        icons: ["circle-filled"],
      },
      {
        name: TreatmentEnum.DIENTE_AUSENTE,
        label: "Diente Ausente",
        availableTypes: [TreatmentConditionEnum.EXISTING],
        icons: ["x"],
      },
      {
        name: TreatmentEnum.CORONA,
        label: "Corona",
        availableTypes: [
          TreatmentConditionEnum.EXISTING,
          TreatmentConditionEnum.REQUIRED,
        ],
        icons: ["circle"],
      },
      {
        name: TreatmentEnum.PUENTE,
        label: "Puente",
        availableTypes: [TreatmentConditionEnum.EXISTING],
        icons: ["building-bridge-2"],
      },
      {
        name: TreatmentEnum.EXTRACCION,
        label: "Extracción",
        availableTypes: [TreatmentConditionEnum.REQUIRED],
        icons: ["equal"],
      },
      {
        name: TreatmentEnum.IMPLANTE,
        label: "Implantes",
        availableTypes: [TreatmentConditionEnum.EXISTING],
        icons: ["letter-i-small", "letter-m-small"],
      },
    ];
  }
}
