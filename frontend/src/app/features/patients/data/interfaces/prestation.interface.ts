export interface PrestationDto {
  id: number;
  name: string;
  isUnique: boolean;
  hasSteps: boolean;
  requiresLocation: boolean;
  allowedScopes: string[];
  currentPrice: number;
}

export interface PrestationStepDto {
  id: number; 
  nameStep: string;
  position: number;
  required: boolean;
}