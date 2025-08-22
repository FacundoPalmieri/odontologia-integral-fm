export interface DentistInterface {
  licenseNumber: string;
  dentistSpecialty: DentistSpecialtyInterface;
}

export interface DentistSpecialtyInterface {
  id: number;
  name: string;
}
