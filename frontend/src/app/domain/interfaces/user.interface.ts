import { DentistSpecialtyInterface } from "./dentist.interface";
import { PersonInterface } from "./person.interface";
import { RoleInterface } from "./role.interface";

export interface UserInterface {
  id?: number;
  username: string;
  password1?: string;
  password2?: string;
  rolesList: RoleInterface[];
  person?: PersonInterface | null;
  dentist?: DentistDataInterface;
  enabled?: boolean;
}

export interface DentistDataInterface {
  licenseNumber: string;
  dentistSpecialty: DentistSpecialtyInterface;
}
