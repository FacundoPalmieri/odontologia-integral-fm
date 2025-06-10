export interface RoleDtoInterface {
  id: number;
  role: string;
  permissionsList: PermissionDtoInterface[];
}

export interface RoleCreateDtoInterface {
  id: number;
  role: string;
  permissionsList: PermissionCreateDtoInterface[];
}

export interface PermissionDtoInterface {
  id: number;
  permission: string;
  name: string;
}

export interface PermissionCreateDtoInterface {
  id: number;
  permission: string;
  name: string;
}
