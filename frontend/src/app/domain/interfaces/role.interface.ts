import { ActionsEnum } from "../../utils/enums/permissions.enum";

export interface RoleInterface {
  id: number;
  name: string;
  label: string;
  permissionsList?: PermissionInterface[];
}

export interface PermissionInterface {
  id: number;
  name: string;
  label: string;
  actions?: ActionInterface[];
}

export interface ActionInterface {
  id: number;
  name: ActionsEnum;
  label: string;
}
