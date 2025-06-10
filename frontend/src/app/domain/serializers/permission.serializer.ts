import { PermissionDtoInterface } from "../dto/role.dto";
import { PermissionInterface } from "../interfaces/role.interface";

export class PermissionSerializer {
  static toView(permission: PermissionDtoInterface): PermissionInterface {
    const permissionView: PermissionInterface = {
      id: permission.id,
      permission: permission.permission,
      name: permission.name,
    };
    return permissionView;
  }
}
