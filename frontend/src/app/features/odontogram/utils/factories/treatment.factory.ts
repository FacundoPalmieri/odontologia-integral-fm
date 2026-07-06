import { ShowTreatmentInterface } from "../../data/interfaces/treatment.interface";
import { TreatmentEnum, TreatmentConditionEnum } from "../enums/treatment.enum";

export class TreatmentFactory {
  static createTreatments(): ShowTreatmentInterface[] {
    return [
      {
        name: TreatmentEnum.CARIES,
        label: "Caries",
        icons: ["circle-filled"],
      },
      {
        name: TreatmentEnum.TRATAMIENTO_CONDUCTO,
        label: "Trat. de Conducto",
        icons: ["letter-t-small", "letter-c-small"],
      },
      {
        name: TreatmentEnum.OBTURACION_COMPOSITE,
        label: "Obt. Composite",
        icons: ["circle-filled"],
      },
      {
        name: TreatmentEnum.DIENTE_AUSENTE,
        label: "Diente Ausente",
        icons: ["x"],
      },
      {
        name: TreatmentEnum.CORONA,
        label: "Corona",
        icons: ["circle"],
      },
      {
        name: TreatmentEnum.PUENTE,
        label: "Puente",
        icons: ["building-bridge-2"],
      },
      {
        name: TreatmentEnum.EXTRACCION,
        label: "Extracción",
        icons: ["equal"],
      },
      {
        name: TreatmentEnum.IMPLANTE,
        label: "Implantes",
        icons: ["letter-i-small", "letter-m-small"],
      },
      {
        name: TreatmentEnum.SURCO_PROFUNDO,
        label: "Surco profundo",
        icons: ["letter-s-small", "letter-p-small"],
      },
      {
        name: TreatmentEnum.PROTESIS_REMOVIBLES,
        label: "Prótesis removible",
        icons: ["letter-p-small", "letter-r-small"],
      },
    ];
  }
}
