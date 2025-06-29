import {
  DentistCreateDtoInterface,
  DentistDataDtoInterface,
} from "./dentist.dto";
import { PersonCreateDtoInterface, PersonDtoInterface } from "./person.dto";
import { RoleDtoInterface } from "./role.dto";

export interface UserDtoInterface {
  id: number;
  username: string;
  rolesList: RoleDtoInterface[];
  enabled: boolean;
  person: PersonDtoInterface;
  dentist: DentistDataDtoInterface;
  avatarUrl?: Blob; //Sólo necesaria para mostrar en la grilla
}

export interface UserCreateDtoInterface {
  id: number;
  username: string;
  password1: string;
  password2: string;
  rolesList: number[];
  enabled: boolean;
  person: PersonCreateDtoInterface;
  dentist?: DentistCreateDtoInterface;
}
