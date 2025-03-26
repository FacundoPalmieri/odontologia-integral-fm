import { PermissionInterface } from "../interfaces/permission.interface";

export interface RoleUpdateDto {
  id: number;
  role: string;
  permissionsList: PermissionInterface[];
}
