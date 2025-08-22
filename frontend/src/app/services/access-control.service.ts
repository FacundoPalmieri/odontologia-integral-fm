import { Injectable } from "@angular/core";
import { ActionsEnum, PermissionsEnum } from "../utils/enums/permissions.enum";

@Injectable({ providedIn: "root" })
export class AccessControlService {
  private permissionsMap: Map<string, Set<string>> = new Map();

  initializePermissions(): void {
    if (this.permissionsMap.size > 0) return;

    const userDataString = localStorage.getItem("userData");
    if (!userDataString) return;

    const userData = JSON.parse(userDataString);
    const roles = userData?.roles || [];

    roles.forEach((role: any) => {
      role.permissionsList.forEach((perm: any) => {
        const actions = perm.actions.map((a: any) => a.name);
        this.permissionsMap.set(perm.name, new Set(actions));
      });
    });
  }

  private hasPermission(
    permission: PermissionsEnum,
    action: ActionsEnum
  ): boolean {
    const actions = this.permissionsMap.get(permission);
    return actions?.has(action) || false;
  }

  can(permission: PermissionsEnum, action: ActionsEnum): boolean {
    return this.hasPermission(permission, action);
  }
}
