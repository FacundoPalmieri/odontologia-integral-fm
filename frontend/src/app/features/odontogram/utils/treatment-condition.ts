import { TreatmentConditionEnum } from "./enums/treatment.enum";

/** Display label and Material color token for each condition key. */
export const TREATMENT_CONDITIONS: Record<TreatmentConditionEnum, { label: string; color: string }> = {
  [TreatmentConditionEnum.PREEXISTING]: { label: "Preexistente", color: "var(--mat-sys-outline)" },
  [TreatmentConditionEnum.REQUIRED]: { label: "Requerida", color: "var(--mat-sys-primary)" },
  [TreatmentConditionEnum.IN_PROGRESS]: { label: "En curso", color: "var(--mat-sys-secondary)" },
  [TreatmentConditionEnum.FINISHED]: { label: "Finalizada", color: "var(--mat-sys-tertiary)" },
  [TreatmentConditionEnum.REPLACED]: { label: "Reemplazada", color: "var(--mat-sys-on-surface-variant)" },
  [TreatmentConditionEnum.CANCELED]: { label: "Cancelada", color: "var(--mat-sys-error)" },
};

export const conditionLabel = (c: TreatmentConditionEnum) => TREATMENT_CONDITIONS[c].label;
export const conditionColor = (c: TreatmentConditionEnum) => TREATMENT_CONDITIONS[c].color;
