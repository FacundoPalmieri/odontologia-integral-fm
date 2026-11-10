export enum TreatmentEnum {
  TRATAMIENTO_CONDUCTO = "tratamiento_conducto",
  OBTURACION_COMPOSITE = "obturacion_composite",
  DIENTE_AUSENTE = "diente_ausente",
  CORONA = "corona",
  PUENTE = "puente",
  CARIES = "caries",
  EXTRACCION = "extraccion",
  IMPLANTE = "implante",
  DUAL_TREATMENT = "DUAL_TREATMENT",
  PROTESIS_REMOVIBLES = 'protesis_removibles',
  SURCO_PROFUNDO = 'surco_profundo'
}

/** Condition keys returned by the backend in `Treatment.conditions`. */
export enum TreatmentConditionEnum {
  PREEXISTING = "PREEXISTING",
  REQUIRED = "REQUIRED",
  IN_PROGRESS = "IN_PROGRESS",
  FINISHED = "FINISHED",
  REPLACED = "REPLACED",
  CANCELED = "CANCELED",
}
