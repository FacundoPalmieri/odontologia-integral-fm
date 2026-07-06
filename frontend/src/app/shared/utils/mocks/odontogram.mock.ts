import { OdontogramInterface } from "../../../features/odontogram/data/interfaces/odontogram.interface";
import { ToothFaceEnum } from "../../../features/odontogram/utils/enums/tooth-face.enum";
import {
  TreatmentEnum,
  TreatmentConditionEnum,
} from "../../../features/odontogram/utils/enums/treatment.enum";

export const mockOdontogram1: OdontogramInterface = {
  upperTeethLeft: [
    {
      number: 18,
      treatments: [
        {
          name: TreatmentEnum.CARIES,
          label: "Caries",
          treatmentType: TreatmentConditionEnum.REQUIRED,
          faces: [ToothFaceEnum.OCLUSAL, ToothFaceEnum.DISTAL],
        },
        {
          name: TreatmentEnum.CARIES,
          label: "Caries",
          treatmentType: TreatmentConditionEnum.REQUIRED,
          faces: [ToothFaceEnum.LINGUAL],
        },
      ],
    },
    {
      number: 17,
      treatments: [
        {
          name: TreatmentEnum.TRATAMIENTO_CONDUCTO,
          label: "Tratamiento de Conducto",
          treatmentType: TreatmentConditionEnum.EXISTING,
        },
        {
          name: TreatmentEnum.CARIES,
          label: "Caries",
          treatmentType: TreatmentConditionEnum.REQUIRED,
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
          treatmentType: TreatmentConditionEnum.EXISTING,
        },
        {
          name: TreatmentEnum.CARIES,
          label: "Caries",
          treatmentType: TreatmentConditionEnum.REQUIRED,
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
      treatments: [
        {
          name: TreatmentEnum.PUENTE,
          label: "Puente",
          treatmentType: TreatmentConditionEnum.EXISTING,
          bridgeStart: 11,
          bridgeEnd: 21,
        },
      ],
    },
  ],
  upperTeethRight: [
    {
      number: 21,
      treatments: [
        {
          name: TreatmentEnum.IMPLANTE,
          label: "Implante",
          treatmentType: TreatmentConditionEnum.EXISTING,
        },
      ],
    },
    {
      number: 22,
      treatments: [
        {
          name: TreatmentEnum.OBTURACION_COMPOSITE,
          label: "Diente Ausente",
          treatmentType: TreatmentConditionEnum.EXISTING,
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
          treatmentType: TreatmentConditionEnum.EXISTING,
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
          treatmentType: TreatmentConditionEnum.EXISTING,
          bridgeStart: 36,
          bridgeEnd: 33,
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

export const mockOdontogram2: OdontogramInterface = {
  upperTeethLeft: [
    {
      number: 18,
      treatments: [
        {
          name: TreatmentEnum.CARIES,
          label: "Caries",
          treatmentType: TreatmentConditionEnum.REQUIRED,
          faces: [ToothFaceEnum.OCLUSAL, ToothFaceEnum.DISTAL],
        },
      ],
    },
    {
      number: 17,
      treatments: [
        {
          name: TreatmentEnum.TRATAMIENTO_CONDUCTO,
          label: "Tratamiento de Conducto",
          treatmentType: TreatmentConditionEnum.EXISTING,
        },
        {
          name: TreatmentEnum.CARIES,
          label: "Caries",
          treatmentType: TreatmentConditionEnum.REQUIRED,
          faces: [ToothFaceEnum.LINGUAL],
        },
      ],
    },
    {
      number: 16,
      treatments: [
        {
          name: TreatmentEnum.CARIES,
          label: "Caries",
          treatmentType: TreatmentConditionEnum.REQUIRED,
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
          name: TreatmentEnum.IMPLANTE,
          label: "Implante",
          treatmentType: TreatmentConditionEnum.EXISTING,
        },
      ],
    },
    {
      number: 22,
    },
    {
      number: 23,
      treatments: [
        {
          name: TreatmentEnum.DIENTE_AUSENTE,
          label: "Diente Ausente",
          treatmentType: TreatmentConditionEnum.EXISTING,
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
          treatmentType: TreatmentConditionEnum.EXISTING,
          bridgeStart: 36,
          bridgeEnd: 33,
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

export const mockOdontogram3: OdontogramInterface = {
  upperTeethLeft: [
    {
      number: 18,
      treatments: [
        {
          name: TreatmentEnum.CARIES,
          label: "Caries",
          treatmentType: TreatmentConditionEnum.REQUIRED,
          faces: [ToothFaceEnum.OCLUSAL, ToothFaceEnum.DISTAL],
        },
      ],
    },
    {
      number: 17,
      treatments: [
        {
          name: TreatmentEnum.CARIES,
          label: "Caries",
          treatmentType: TreatmentConditionEnum.REQUIRED,
          faces: [ToothFaceEnum.LINGUAL],
        },
      ],
    },
    {
      number: 16,
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
    },
    {
      number: 22,
    },
    {
      number: 23,
      treatments: [
        {
          name: TreatmentEnum.DIENTE_AUSENTE,
          label: "Diente Ausente",
          treatmentType: TreatmentConditionEnum.EXISTING,
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
          treatmentType: TreatmentConditionEnum.EXISTING,
          bridgeStart: 36,
          bridgeEnd: 33,
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
