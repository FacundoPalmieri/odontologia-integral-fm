export interface RoleInterface {
  id: number;
  role: string;
  permissionsList: PermissionInterface[];
}

export interface PermissionInterface {
  id: number;
  permission: string;
  name: string;
}
