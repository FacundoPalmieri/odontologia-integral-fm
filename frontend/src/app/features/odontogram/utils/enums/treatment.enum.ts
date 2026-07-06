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

export enum TreatmentConditionEnum {
  EXISTING = 1,
  REQUIRED = 2,
  IN_PROGRESS = 3,
  DONE = 4,
}
