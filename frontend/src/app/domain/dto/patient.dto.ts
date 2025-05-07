export interface PatientCreateDto {
  firstName: string;
  lastName: string;
  dniTypeId: number;
  dni: string;
  birthDate: Date;
  genderId: number;
  nationalityId: number;
  localityId: number;
  street: string;
  number: number;
  floor: string;
  apartment: string;
  healthPlansId: number;
  affiliateNumber: string;
  email: string;
  phoneType: number;
  phone: string;
  // medicalRiskId: number[];
  // medicalHistoryObservation: string;
}
