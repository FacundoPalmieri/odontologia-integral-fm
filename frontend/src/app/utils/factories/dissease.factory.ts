import {
  DisseaseConditionInterface,
  DisseaseTypeInterface,
} from "../../domain/interfaces/patient.interface";
import { DisseaseConditionEnum, DisseaseEnum } from "../enums/dissease.enum";

export class DisseaseFactory {
  static createDisseaseTypes(): DisseaseTypeInterface[] {
    return [
      {
        dissease: DisseaseEnum.ALERGICO,
        name: "Alérgico",
      },
      {
        dissease: DisseaseEnum.AMAMANTA,
        name: "Amamanta",
      },
      {
        dissease: DisseaseEnum.ANEMIA,
        name: "Anemia",
      },
      {
        dissease: DisseaseEnum.ANTICONCEPTIVOS,
        name: "Anticonceptivos",
      },
      {
        dissease: DisseaseEnum.ASMA,
        name: "Asma",
      },
      {
        dissease: DisseaseEnum.CARDIACO,
        name: "Cardíaco",
      },
      {
        dissease: DisseaseEnum.DIABETICO,
        name: "Diabético",
      },
      {
        dissease: DisseaseEnum.EMBARAZO,
        name: "Embarazo",
      },
      {
        dissease: DisseaseEnum.EPILEPSIA,
        name: "Epilepsia",
      },
      {
        dissease: DisseaseEnum.HEMORRAGIA,
        name: "Hemorragia",
      },
      {
        dissease: DisseaseEnum.HEPATITIS,
        name: "Hepatitis",
      },
      {
        dissease: DisseaseEnum.HIV,
        name: "H.I.V",
      },
      {
        dissease: DisseaseEnum.PRESION,
        name: "Presión",
      },
      {
        dissease: DisseaseEnum.TOMA_MEDICAMENTO,
        name: "Toma medicamento",
      },
      {
        dissease: DisseaseEnum.TRATAMIENTO_MEDICO_ACTUAL,
        name: "Tratamiento médico actual",
      },
      {
        dissease: DisseaseEnum.ULCERAS,
        name: "Úlceras",
      },
      {
        dissease: DisseaseEnum.OTROS,
        name: "Otros",
      },
    ];
  }

  static createDisseaseConditions(): DisseaseConditionInterface[] {
    return [
      {
        condition: DisseaseConditionEnum.BY_PASS,
        name: "By-Pass",
      },
      {
        condition: DisseaseConditionEnum.FIEBRE_REUMATICA,
        name: "Fiebre Reumática",
      },
      {
        condition: DisseaseConditionEnum.MARCAPASOS,
        name: "Marcapasos",
      },
      {
        condition: DisseaseConditionEnum.PROTESIS_VALVULARES,
        name: "Prótesis Valvulares",
      },
      {
        condition: DisseaseConditionEnum.OTROS,
        name: "Otros",
      },
    ];
  }
}
