import { inject, Injectable } from "@angular/core";
import { AccessControlService } from "../../core/services/access-control.service";
import {
  LogoutInterface,
  UserDataInterface,
} from "../../features/auth/domain/interfaces/auth.interface";
import { RoleEnum } from "../utils/enums/role.enum";

@Injectable({ providedIn: "root" })
export class LocalStorageService {
  accessControlService = inject(AccessControlService);

  /**
   * Realiza el login del usuario en el localStorage
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

  doLogout(): void {
    localStorage.removeItem("userData");
  }

  /**
   * Obtiene el token jwt
   */
  getJwtToken(): string | null {
    const userData = localStorage.getItem("userData");
    if (userData) {
      return JSON.parse(userData).jwt;
    }
    return null;
  }

  /**
   * Obtiene los datos del usuario
   */
  getUserData(): UserDataInterface | null {
    const userData = localStorage.getItem("userData");
    if (userData) {
      return JSON.parse(userData);
    }
    return null;
  }

  /**
   * Obtiene el rol del usuario
   */
  getUserRole(): RoleEnum {
    const userData = this.getUserData();
    return userData?.roles[0].name as RoleEnum;
  }

  /**
   * Verifica si el usuario tiene un rol específico
   */
  hasRole(role: RoleEnum): boolean {
    const userRole = this.getUserRole();
    return userRole === role;
  }

  /**
   * Verifica si el usuario es dentista
   */
  isDentist(): boolean {
    return this.getUserData()?.dentist!;
  }

  /**
   * Verifica si el usuario es secretario
   */
  isSecretary(): boolean {
    return this.hasRole(RoleEnum.SECRETARY);
  }

  /**
   * Verifica si el usuario es administrador
   */
  isAdministrator(): boolean {
    return this.hasRole(RoleEnum.ADMINISTRATOR);
  }

  /**
   * Verifica si el usuario está logueado
   */
  isLoggedIn(): boolean {
    const token = this.getJwtToken();

    if (!token) return false;

    const payload = this.getJWTokenPayload(token);
    if (!payload) return false;

    return !this.isTokenExpired(payload.exp);
  }

  /**
   * Obtiene los datos de logout
   */
  getLogoutData(): LogoutInterface | null {
    const userData = this.getUserData();
    if (userData != null) {
      const logoutData: LogoutInterface = {
        jwt: userData?.jwt,
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
   * Obtiene el payload del token
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
   * Verifica si el token ha expirado
   */
  private isTokenExpired(expiration: number): boolean {
    if (!expiration) {
      return true;
    }
    const now = Math.floor(Date.now() / 1000);
    return expiration < now;
  }
}
