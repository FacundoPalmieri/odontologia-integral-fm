export interface RoleDtoInterface {
  id: number;
  role: string;
  permissionsList: PermissionDtoInterface[];
}

export interface PermissionDtoInterface {
  id: number;
  permission: string;
  name: string;
}
