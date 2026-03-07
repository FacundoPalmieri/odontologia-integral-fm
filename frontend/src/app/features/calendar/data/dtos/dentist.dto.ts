import { PersonDto } from "../../../../shared/dtos/person.dto";

export interface DentistDto {
  person: PersonDto;
  licenseNumber: string;
  dentistSpecialty: string;
}

export interface DentistDataDto {
  dentistSpecialty: DentistSpecialtyDto;
  licenseNumber: string;
}

export interface DentistCreateDto {
  licenseNumber: string;
  dentistSpecialtyId: number;
}

export interface DentistSpecialtyDto {
  id: number;
  name: string;
}
