import { RoleCreateDtoInterface, RoleDtoInterface } from "../dto/role.dto";
import { RoleInterface } from "../interfaces/role.interface";

export class RoleSerializer {
  static toUpdateDto(role: RoleInterface): RoleCreateDtoInterface {
    const roleDto: RoleCreateDtoInterface = {
      id: role.id,
      role: role.role,
      permissionsList: role.permissionsList.map((permission) => ({
        id: permission.id,
        permission: permission.permission,
        name: permission.name,
      })),
    };
    return roleDto;
  }

  static toCreateDto(role: RoleInterface): RoleCreateDtoInterface {
    const roleDto: RoleCreateDtoInterface = {
      id: role.id,
      role: role.role,
      permissionsList: role.permissionsList.map((permission) => ({
        id: permission.id,
        permission: permission.permission,
        name: permission.name,
      })),
    };
    return roleDto;
  }

  static toView(role: RoleDtoInterface): RoleInterface {
    const roleView: RoleInterface = {
      id: role.id,
      role: role.role,
      permissionsList: role.permissionsList.map((permission) => ({
        id: permission.id,
        permission: permission.permission,
        name: permission.name,
      })),
    };
    return roleView;
  }
}
