import { PermissionInterface } from "../interfaces/permission.interface";

export interface RoleCreateDto {
  id: number;
  role: string;
  permissionsList: PermissionInterface[];
}
