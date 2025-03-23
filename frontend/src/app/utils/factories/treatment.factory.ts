import { TreatmentInterface } from "../../domain/interfaces/treatment.interface";

export class TreatmentFactory {
  static createTreatments(): TreatmentInterface[] {
    return [
      {
        name: "Extracción",
        icons: ["equal"],
      },
      {
        name: "Trat. de Conducto",
        icons: ["letter-t-small", "letter-c-small"],
      },
      {
        name: "Obt. Composite",
        icons: ["slash", "letter-a-small", "letter-c-small"],
      },
      {
        name: "Diente Ausente",
        icons: ["letter-x-small"],
      },
      {
        name: "Obt. Amalgama",
        icons: ["slash", "letter-a-small"],
      },
      {
        name: "Paradentosis",
        icons: ["letter-p-small", "letter-d-small"],
      },
      {
        name: "Corona",
        icons: ["letter-o-small"],
      },
      {
        name: "Pivot. Perno",
        icons: ["letter-p-small"],
      },
      {
        name: "Incrustración",
        icons: ["letter-i-small"],
      },
      {
        name: "Puente",
        icons: ["building-bridge-2"],
      },
      {
        name: "Prot. Removible",
        icons: ["code-variable"],
      },
      {
        name: "Implamente",
        icons: ["letter-i-small", "letter-m-small"],
      },
      {
        name: "Ortodoncia",
        icons: ["equal"],
      },
      {
        name: "Caries Curable",
        icons: ["circle-filled"],
      },
      {
        name: "Caries Incurable",
        icons: ["rectangle-filled"],
      },
      {
        name: "Obt. Silicato",
        icons: ["slash", "letter-s-small"],
      },
      {
        name: "Prest. Existente",
        icons: ["rectangle-filled"],
        color: "text-[#ef4444]",
      },
      {
        name: "Prest. Requerida",
        icons: ["rectangle-filled"],
        color: "text-[#2b7fff]",
      },
    ];
  }
}
