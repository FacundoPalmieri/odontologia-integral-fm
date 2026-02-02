import { PersonInterface } from "../../../../shared/interfaces/person.interface";
import { RoleInterface } from "../../../roles/domain/interfaces/role.interface";

export interface UserInterface {
  id?: number;
  username: string;
  password1?: string;
  password2?: string;
  rolesList: RoleInterface[];
  person?: PersonInterface | null;
  dentist?: UserDentistInterface;
  enabled?: boolean;
}

export interface UserDentistInterface {
  licenseNumber: string;
  dentistSpecialty: DentistSpecialtyInterface;
}

export interface DentistSpecialtyInterface {
  id: number;
  name: string;
}
