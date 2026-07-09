import { PrestationScopeEnum } from "../../utils/enums/consultation-instance.enum";

export interface PrestationDto {
  id: number;
  name: string;
  isUnique: boolean;
  hasSteps: boolean;
  requiresLocation: boolean;
  allowedScopes: PrestationScopeEnum[];
  currentPrice: number;
}

export interface PrestationStepDto {
  id: number; 
  nameStep: string;
  position: number;
  required: boolean;
}