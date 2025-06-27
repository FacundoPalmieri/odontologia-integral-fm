import { PersonDtoInterface } from "../dto/person.dto";
import { RoleInterface } from "./role.interface";

export interface AuthUserInterface {
  idUser: number;
  jwt: string;
  refreshToken: string;
  roles: RoleInterface[];
  username: string;
  person: PersonDtoInterface;
}
