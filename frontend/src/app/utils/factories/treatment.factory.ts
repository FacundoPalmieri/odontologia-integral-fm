import { TreatmentInterface } from "../../domain/interfaces/treatment.interface";

export class TreatmentFactory {
  static createTreatments(): TreatmentInterface[] {
    return [
      {
        name: "Trat. de Conducto",
        icons: ["letter-t-small", "letter-c-small"],
        color: "text-[#ef4444]",
      },
      {
        name: "Trat. de Conducto",
        icons: ["letter-t-small", "letter-c-small"],
        color: "text-[#2b7fff]",
      },
      {
        name: "Obt. Composite",
        icons: ["rectangle-filled"],
        color: "text-[#ef4444]",
      },
      // {
      //   name: "Diente Ausente",
      //   icons: ["letter-x-small"],
      //   color: "text-[#ef4444]",
      // },
      {
        name: "Diente Ausente",
        icons: ["dental-off"],
        color: "text-[#ef4444]",
      },
      {
        name: "Corona",
        icons: ["circle"],
        color: "text-[#ef4444]",
      },
      {
        name: "Corona",
        icons: ["circle"],
        color: "text-[#2b7fff]",
      },
      {
        name: "Puente",
        icons: ["building-bridge-2"],
        color: "text-[#ef4444]",
      },
      {
        name: "Implante",
        icons: ["letter-i-small", "letter-m-small"],
        color: "text-[#ef4444]",
      },
      {
        name: "Caries",
        icons: ["rectangle-filled"],
        color: "text-[#2b7fff]",
      },
      {
        name: "Extracción",
        icons: ["equal"],
        color: "text-[#2b7fff]",
      },
    ];
  }
}
