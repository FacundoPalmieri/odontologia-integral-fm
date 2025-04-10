export interface PatientInterface {
  name: string;
  age: number;
  birthday: Date;
  dni: number;
  phone: number;
  mail: string;
  address: string;
  locality: string;
  medicare?: string;
  affiliate_number?: number;
}
