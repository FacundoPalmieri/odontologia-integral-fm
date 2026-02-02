import { PermissionCreateDto, PermissionDto } from "./permission.dto";

export interface RoleDto {
  id: number;
  name: string;
  label: string;
  permissionsList?: PermissionDto[];
}

export interface RoleCreateDto {
  id?: number;
  name: string;
  label: string;
  permissionsList: PermissionCreateDto[];
}
