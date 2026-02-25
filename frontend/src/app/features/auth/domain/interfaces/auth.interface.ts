import { PersonDto } from "../../../../shared/dtos/person.dto";
import { RoleInterface } from "../../../roles/domain/interfaces/role.interface";

export interface LoginInterface {
  username: string;
  password: string;
}

export interface LogoutInterface {
  refreshToken: string;
  idUser: number;
  username: string;
}

export interface UserDataInterface {
  idUser: number;
  jwt: string;
  refreshToken: string;
  roles: RoleInterface[];
  username: string;
  person: PersonDto;
  dentist: boolean;
}

export interface ResetPasswordInterface {
  token: string;
  newPassword1: string;
  newPassword2: string;
}
