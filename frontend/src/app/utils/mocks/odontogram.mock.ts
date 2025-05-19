import { OdontogramInterface } from "../../domain/interfaces/odontogram.interface";
import {
  TreatmentEnum,
  TreatmentTypeEnum,
} from "../../utils/enums/treatment.enum";
import { ToothFaceEnum } from "../../utils/factories/tooth-face.factory";

export const mockOdontogram: OdontogramInterface = {
  upperTeethLeft: [
    {
      number: 18,
      treatments: [
        {
          name: TreatmentEnum.CARIES,
          label: "Caries",
          treatmentType: TreatmentTypeEnum.REQUIRED,
          faces: [ToothFaceEnum.INCISAL, ToothFaceEnum.DISTAL],
        },
        {
          name: TreatmentEnum.CARIES,
          label: "Caries",
          treatmentType: TreatmentTypeEnum.REQUIRED,
          faces: [ToothFaceEnum.LINGUAL],
        },
      ],
    },
    {
      number: 17,
      treatments: [
        {
          name: TreatmentEnum.TRAT_DE_CONDUCTO,
          label: "Tratamiento de Conducto",
          treatmentType: TreatmentTypeEnum.EXISTING,
        },
        {
          name: TreatmentEnum.CARIES,
          label: "Caries",
          treatmentType: TreatmentTypeEnum.REQUIRED,
          faces: [ToothFaceEnum.LINGUAL],
        },
      ],
    },
    {
      number: 16,
      treatments: [
        {
          name: TreatmentEnum.CORONA,
          label: "Corona",
          treatmentType: TreatmentTypeEnum.EXISTING,
        },
        {
          name: TreatmentEnum.CARIES,
          label: "Caries",
          treatmentType: TreatmentTypeEnum.REQUIRED,
          faces: [ToothFaceEnum.LINGUAL],
        },
      ],
    },
    {
      number: 15,
    },
    {
      number: 14,
    },
    {
      number: 13,
    },
    {
      number: 12,
    },
    {
      number: 11,
    },
  ],
  upperTeethRight: [
    {
      number: 21,
      treatments: [
        {
          name: TreatmentEnum.IMPLANTES,
          label: "Implanete",
          treatmentType: TreatmentTypeEnum.EXISTING,
        },
      ],
    },
    {
      number: 22,
      treatments: [
        {
          name: TreatmentEnum.OBT_COMPOSITE,
          label: "Diente Ausente",
          treatmentType: TreatmentTypeEnum.EXISTING,
          faces: [ToothFaceEnum.LINGUAL],
        },
      ],
    },
    {
      number: 23,
      treatments: [
        {
          name: TreatmentEnum.DIENTE_AUSENTE,
          label: "Diente Ausente",
          treatmentType: TreatmentTypeEnum.EXISTING,
        },
      ],
    },
    {
      number: 24,
    },
    {
      number: 25,
    },
    {
      number: 26,
    },
    {
      number: 27,
    },
    {
      number: 28,
    },
  ],
  lowerTeethLeft: [
    {
      number: 48,
    },
    { number: 47 },
    { number: 46 },
    { number: 45 },
    { number: 44 },
    { number: 43 },
    { number: 42 },
    { number: 41 },
  ],
  lowerTeethRight: [
    {
      number: 31,
    },
    {
      number: 32,
    },
    {
      number: 33,
    },
    {
      number: 34,
    },
    {
      number: 35,
    },
    {
      number: 36,
      treatments: [
        {
          name: TreatmentEnum.PUENTE,
          label: "Puente",
          treatmentType: TreatmentTypeEnum.EXISTING,
          bridgeStart: 36,
          bridgeEnd: 34,
        },
      ],
    },
    {
      number: 37,
    },
    {
      number: 38,
    },
  ],
  temporaryUpperLeft: [
    { number: 55 },
    { number: 54 },
    { number: 53 },
    { number: 52 },
    { number: 51 },
  ],
  temporaryUpperRight: [
    { number: 61 },
    { number: 62 },
    { number: 63 },
    { number: 64 },
    { number: 65 },
  ],
  temporaryLowerLeft: [
    { number: 85 },
    { number: 84 },
    { number: 83 },
    { number: 82 },
    { number: 81 },
  ],
  temporaryLowerRight: [
    { number: 71 },
    { number: 72 },
    { number: 73 },
    { number: 74 },
    { number: 75 },
  ],
};
