import { PermissionsEnum } from "../../utils/enums/permission.enum";

export interface MenuItemInterface {
  label: string;
  icon: string;
  route: string;
  permissionEnum: PermissionsEnum;
}
