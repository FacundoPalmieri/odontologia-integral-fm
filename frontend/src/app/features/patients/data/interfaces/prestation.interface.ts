import { PrestationScopeEnum } from "../../utils/enums/consultation-instance.enum";

export interface PrestationDto {
  id: number;
  /** Stable key, e.g. 'SPLINTING'. */
  name: string;
  /** User-facing text. */
  label: string;
  isUnique: boolean;
  /** Max teeth allowed; null = no limit. */
  maxTooth: number | null;
  requiresOriginTreatment: boolean;
  /** Empty = the type has no own location (only origin treatment). */
  allowedScopes: PrestationScopeEnum[];
  currentPrice: number;
}

export interface PrestationStepDto {
  id: number; 
  nameStep: string;
  position: number;
  required: boolean;
}