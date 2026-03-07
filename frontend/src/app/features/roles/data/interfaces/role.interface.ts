import { PermissionInterface } from "./permission.interface";

export interface RoleInterface {
  id: number;
  name: string;
  label: string;
  permissionsList?: PermissionInterface[];
}
