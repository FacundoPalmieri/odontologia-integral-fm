import { PermissionDto } from "../dtos/permission.dto";
import { PermissionInterface } from "../interfaces/permission.interface";

export class PermissionSerializer {
  static toView(permission: PermissionDto): PermissionInterface {
    const permissionView: PermissionInterface = {
      id: permission.id,
      name: permission.name,
      label: permission.label,
    };
    return permissionView;
  }
}
