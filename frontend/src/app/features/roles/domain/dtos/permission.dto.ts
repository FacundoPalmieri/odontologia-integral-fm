export interface PermissionDto {
  id: number;
  name: string;
  label: string;
}

export interface PermissionCreateDto {
  permissionId: number;
  actionId: number[];
}
