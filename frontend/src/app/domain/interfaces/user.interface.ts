import { RoleInterface } from "./rol.interface";

export interface UserInterface {
  id: number;
  username: string;
  rolesList: RoleInterface[];
  enabled: boolean;
}
