import { PermissionInterface } from "./permission.interface";

export interface RoleInterface {
  id: number;
  role: string;
  permissionsList: PermissionInterface[];
}
