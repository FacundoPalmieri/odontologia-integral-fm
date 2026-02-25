import { inject, Injectable } from "@angular/core";
import { AccessControlService } from "../../core/services/access-control.service";
import {
  LogoutInterface,
  UserDataInterface,
} from "../../features/auth/domain/interfaces/auth.interface";
import { RoleEnum } from "../utils/enums/role.enum";

/**
 * Service for managing user data in localStorage.
 *
 * This service handles all localStorage operations related to user authentication
 * and authorization, including:
 * - Storing and retrieving user data
 * - Managing JWT tokens
 * - Checking user roles and permissions
 * - Validating authentication status
 */
@Injectable({ providedIn: "root" })
export class LocalStorageService {
  accessControlService = inject(AccessControlService);

  /**
   * Stores user authentication data in localStorage.
   *
   * This method is called after successful login to persist user data
   * and initialize the access control system.
   *
   * @param authUserData - The authenticated user data including tokens and roles
   * @returns void
   */
  doLogin(authUserData: UserDataInterface) {
    const userData: UserDataInterface = {
      idUser: authUserData.idUser,
      jwt: authUserData.jwt,
      refreshToken: authUserData.refreshToken,
      roles: authUserData.roles,
      username: authUserData.username,
      person: authUserData.person,
      dentist: authUserData.dentist,
    };

    localStorage.setItem("userData", JSON.stringify(userData));
    this.accessControlService.initializePermissions();
  }

  /**
   * Removes user data from localStorage.
   *
   * This method is called during logout to clear all stored user information.
   *
   * @returns void
   */
  doLogout(): void {
    localStorage.removeItem("userData");
  }

  /**
   * Retrieves the JWT access token from localStorage.
   *
   * @returns The JWT token string, or null if not found
   */
  getJwtToken(): string | null {
    const userData = localStorage.getItem("userData");
    if (userData) {
      return JSON.parse(userData).jwt;
    }
    return null;
  }

  /**
   * Retrieves the complete user data from localStorage.
   *
   * @returns The user data object, or null if not found
   */
  getUserData(): UserDataInterface | null {
    const userData = localStorage.getItem("userData");
    if (userData) {
      return JSON.parse(userData);
    }
    return null;
  }

  /**
   * Retrieves the user's primary role.
   *
   * Returns the first role assigned to the user.
   *
   * @returns The user's role enum value
   */
  getUserRole(): RoleEnum {
    const userData = this.getUserData();
    return userData?.roles[0].name as RoleEnum;
  }

  /**
   * Checks if the user has a specific role.
   *
   * @param role - The role to check
   * @returns True if the user has the specified role, false otherwise
   */
  hasRole(role: RoleEnum): boolean {
    const userRole = this.getUserRole();
    return userRole === role;
  }

  /**
   * Checks if the user is a dentist.
   *
   * @returns True if the user is a dentist, false otherwise
   */
  isDentist(): boolean {
    return this.getUserData()?.dentist!;
  }

  /**
   * Checks if the user is a secretary.
   *
   * @returns True if the user has the secretary role, false otherwise
   */
  isSecretary(): boolean {
    return this.hasRole(RoleEnum.SECRETARY);
  }

  /**
   * Checks if the user is an administrator.
   *
   * @returns True if the user has the administrator role, false otherwise
   */
  isAdministrator(): boolean {
    return this.hasRole(RoleEnum.ADMINISTRATOR);
  }

  /**
   * Checks if the user is currently logged in.
   *
   * Validates that a token exists and is not expired.
   *
   * @returns True if the user is logged in with a valid token, false otherwise
   */
  isLoggedIn(): boolean {
    const token = this.getJwtToken();

    if (!token) return false;

    const payload = this.getJWTokenPayload(token);
    if (!payload) return false;

    return !this.isTokenExpired(payload.exp);
  }

  /**
   * Prepares logout data from stored user information.
   *
   * Creates a logout request object with the necessary data to invalidate
   * the user's session on the server.
   *
   * @returns Logout data object, or null if no user data is stored
   */
  getLogoutData(): LogoutInterface | null {
    const userData = this.getUserData();
    if (userData != null) {
      const logoutData: LogoutInterface = {
        refreshToken: userData.refreshToken,
        idUser: userData.idUser,
        username: userData.username,
      };
      return logoutData;
    } else {
      return null;
    }
  }

  /**
   * Decodes and extracts the payload from a JWT token.
   *
   * @param token - The JWT token string
   * @returns The decoded token payload, or null if decoding fails
   * @private
   */
  private getJWTokenPayload(token: string) {
    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      return payload;
    } catch (error) {
      return null;
    }
  }

  /**
   * Checks if a JWT token has expired.
   *
   * @param expiration - The expiration timestamp from the token payload
   * @returns True if the token is expired, false otherwise
   * @private
   */
  private isTokenExpired(expiration: number): boolean {
    if (!expiration) {
      return true;
    }
    const now = Math.floor(Date.now() / 1000);
    return expiration < now;
  }
}
