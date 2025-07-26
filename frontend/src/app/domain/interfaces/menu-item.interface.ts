import { PermissionsEnum } from "../../utils/enums/permissions.enum";

export interface MenuItemInterface {
  label: string;
  icon: string;
  route: string;
  permissionEnum: PermissionsEnum;
}
