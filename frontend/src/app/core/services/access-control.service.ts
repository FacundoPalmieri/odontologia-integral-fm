import { Injectable } from "@angular/core";
import {
  ActionsEnum,
  PermissionsEnum,
} from "../../shared/utils/enums/permissions.enum";

/**
 * Service for managing user access control and permissions.
 *
 * This service handles the verification of user permissions based on their assigned roles.
 * It maintains a map of permissions and their associated actions, loaded from localStorage,
 * and provides methods to check if a user has specific permissions to perform certain actions.
 */
@Injectable({ providedIn: "root" })
export class AccessControlService {
  /**
   * Map storing permissions and their associated actions.
   * Key: permission name, Value: Set of action names
   */
  private permissionsMap: Map<string, Set<string>> = new Map();

  /**
   * Initializes the permissions map from user data stored in localStorage.
   *
   * This method loads the user's roles and their associated permissions from localStorage,
   * then populates the permissionsMap with all available permissions and actions.
   * If the map is already populated, it returns early to avoid redundant initialization.
   *
   * @returns void
   */
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

  /**
   * Checks if the user has a specific permission to perform a given action.
   *
   * @param permission - The permission to check
   * @param action - The action to verify within the permission
   * @returns True if the user has the permission and action, false otherwise
   * @private
   */
  private hasPermission(
    permission: PermissionsEnum,
    action: ActionsEnum,
  ): boolean {
    const actions = this.permissionsMap.get(permission);
    return actions?.has(action) || false;
  }

  /**
   * Public method to verify if the user can perform a specific action on a permission.
   *
   * This is the main method used throughout the application to check user permissions.
   *
   * @param permission - The permission to check
   * @param action - The action to verify
   * @returns True if the user is authorized to perform the action, false otherwise
   *
   * @example
   * ```typescript
   * if (accessControlService.can(PermissionsEnum.PATIENTS, ActionsEnum.CREATE)) {
   *   // User can create patients
   * }
   * ```
   */
  can(permission: PermissionsEnum, action: ActionsEnum): boolean {
    return this.hasPermission(permission, action);
  }
}
