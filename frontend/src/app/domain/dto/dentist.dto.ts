import { DayEnum } from "../../utils/enums/day.enum";
import { PersonDtoInterface } from "./person.dto";

export interface DentistDtoInterface {
  person: PersonDtoInterface;
  licenseNumber: string;
  dentistSpecialty: string;
}

export interface DentistDataDtoInterface {
  dentistSpecialty: string;
  licenseNumber: string;
}

export interface DentistCreateDtoInterface {
  licenseNumber: string;
  dentistSpecialtyId: number;
}

export interface DentistSpecialtyDtoInterface {
  id: number;
  name: string;
}

export interface DentistDayAvailabilityDtoInterface {
  dayName: DayEnum;
  startTime: string;
  endTime: string;
  appointmentDuration: number;
}

export type DentistAvailabilityDtoInterface =
  DentistDayAvailabilityDtoInterface[];
