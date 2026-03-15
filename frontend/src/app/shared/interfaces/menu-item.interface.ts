import { PermissionsEnum } from "../utils/enums/permissions.enum";

export interface MenuItemInterface {
  label: string;
  subtitle?: string;
  icon: string;
  bgColor?: string;
  textColor?: string;
  route: string;
  permissionEnum: PermissionsEnum;
  children?: MenuItemInterface[];
}
