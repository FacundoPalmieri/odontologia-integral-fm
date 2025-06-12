import { PersonDtoInterface } from "./person.dto";

export interface DentistDtoInterface {
  person: PersonDtoInterface;
  licenseNumber: string;
  dentistSpecialty: string;
}

export interface DentistCreateDtoInterface {
  licenseNumber: string;
  dentistSpecialtyId: number;
}
