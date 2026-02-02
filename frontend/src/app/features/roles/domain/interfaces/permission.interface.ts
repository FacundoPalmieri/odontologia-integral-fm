import { ActionsEnum } from "../../../../shared/utils/enums/permissions.enum";

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
