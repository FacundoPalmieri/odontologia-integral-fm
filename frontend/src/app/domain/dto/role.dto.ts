export interface RoleDtoInterface {
  id: number;
  name: string;
  label: string;
  permissionsList?: PermissionDtoInterface[];
}

export interface RoleCreateDtoInterface {
  id?: number;
  name: string;
  label: string;
  permissionsList: PermissionCreateDtoInterface[];
}

export interface PermissionDtoInterface {
  id: number;
  name: string;
  label: string;
}

export interface PermissionCreateDtoInterface {
  permissionId: number;
  actionId: number[];
}
