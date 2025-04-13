export interface PatientInterface {
  name: string;
  birthday: Date;
  dni: number;
  phone: number;
  mail: string;
  address: string;
  locality: string;
  medicare?: string;
  affiliateNumber?: number;
  medicalHistory?: string[];
}
