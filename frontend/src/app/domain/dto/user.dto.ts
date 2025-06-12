import { DentistCreateDtoInterface, DentistDtoInterface } from "./dentist.dto";
import { PersonCreateDtoInterface, PersonDtoInterface } from "./person.dto";
import { RoleDtoInterface } from "./role.dto";

export interface UserDtoInterface {
  id: number;
  username: string;
  rolesList: RoleDtoInterface[];
  enabled: boolean;
  person: PersonDtoInterface;
  desntist: DentistDtoInterface[];
}

export interface UserCreateDtoInterface {
  username: string;
  password1: string;
  password2: string;
  rolesList: number[];
  person: PersonCreateDtoInterface;
  dentist?: DentistCreateDtoInterface;
}
