import { RoleCreateDto, RoleDto } from "../dtos/role.dto";
import { RoleInterface } from "../interfaces/role.interface";

export class RoleSerializer {
  static toUpdateDto(role: RoleInterface): RoleCreateDto {
    const roleDto: RoleCreateDto = {
      id: role.id,
      name: role.name,
      label: role.label,
      permissionsList: role.permissionsList!.map((permission) => ({
        permissionId: permission.id,
        actionId: permission.actions!.map((action) => action.id),
      })),
    };
    return roleDto;
  }

  static toCreateDto(role: RoleInterface): RoleCreateDto {
    const roleDto: RoleCreateDto = {
      id: role.id,
      name: role.name,
      label: role.label,
      permissionsList: role.permissionsList!.map((permission) => ({
        permissionId: permission.id,
        actionId: permission.actions!.map((action) => action.id),
      })),
    };
    return roleDto;
  }

  static toView(role: RoleDto): RoleInterface {
    const roleView: RoleInterface = {
      id: role.id,
      name: role.name,
      label: role.label,
      permissionsList: role.permissionsList,
    };
    return roleView;
  }
}
