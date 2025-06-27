import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { LoginInterface } from "../domain/interfaces/login.interface";
import { BehaviorSubject, catchError, Observable, tap, throwError } from "rxjs";
import { AuthUserInterface } from "../domain/interfaces/auth-user.interface";
import { ResetPasswordInterface } from "../domain/interfaces/reset-password.interface";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { UserDataInterface } from "../domain/interfaces/user-data.interface";
import { LogoutInterface } from "../domain/interfaces/logout.interface";
import { RefreshTokenDataDto } from "../domain/dto/refresh-token-data.dto";
import { Router } from "@angular/router";
import { UserService } from "./user.service";
import { UserInterface } from "../domain/interfaces/user.interface";
import { RoleInterface } from "../domain/interfaces/role.interface";

@Injectable({ providedIn: "root" })
export class AuthService {
  http = inject(HttpClient);
  userService = inject(UserService);
  router = inject(Router);
  apiUrl = environment.apiUrl;

  refreshTokenInProgress = false;
  refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<
    string | null
  >(null);

  login(
    login: LoginInterface
  ): Observable<ApiResponseInterface<AuthUserInterface>> {
    return this.http.post<ApiResponseInterface<AuthUserInterface>>(
      `${this.apiUrl}/auth/login`,
      login
    );
  }

  logout(logout: LogoutInterface): Observable<ApiResponseInterface<string>> {
    return this.http.delete<ApiResponseInterface<string>>(
      `${this.apiUrl}/auth/logout`,
      {
        body: logout,
      }
    );
  }

  refreshToken(
    refreshTokenData: RefreshTokenDataDto
  ): Observable<ApiResponseInterface<AuthUserInterface>> {
    this.refreshTokenInProgress = true;
    this.refreshTokenSubject.next(null);
    let roles: RoleInterface[];
    return this.http
      .post<ApiResponseInterface<AuthUserInterface>>(
        `${this.apiUrl}/auth/token/refresh`,
        refreshTokenData
      )
      .pipe(
        tap((response) => {
          this.doLogin(response.data);
          this.updateRoles(response.data.idUser);
          this.refreshTokenSubject.next(response.data.jwt);
          this.refreshTokenInProgress = false;
        }),
        catchError((error) => {
          this.refreshTokenInProgress = false;
          this.dologout();
          this.router.navigateByUrl("/login");
          this.refreshTokenSubject.next(null);
          return throwError(() => error);
        })
      );
  }

  updateRoles(id: number) {
    let userData = this.getUserData();
    this.userService
      .getById(id)
      .subscribe((response: ApiResponseInterface<UserInterface>) => {
        userData = {
          ...userData!,
          roles: response.data.rolesList,
        };
        this.doLogin(userData);
      });
  }

  resetPasswordRequest(
    email: string
  ): Observable<ApiResponseInterface<string>> {
    const params = new HttpParams().set("email", email);

    return this.http.post<ApiResponseInterface<string>>(
      `${this.apiUrl}/auth/password/reset-request`,
      null,
      { params }
    );
  }

  resetPassword(
    resetData: ResetPasswordInterface
  ): Observable<ApiResponseInterface<string>> {
    return this.http.post<ApiResponseInterface<string>>(
      `${this.apiUrl}/auth/password/reset`,
      resetData
    );
  }

  doLogin(authUserData: AuthUserInterface) {
    const userData: UserDataInterface = {
      idUser: authUserData.idUser,
      jwt: authUserData.jwt,
      refreshToken: authUserData.refreshToken,
      roles: authUserData.roles,
      username: authUserData.username,
      person: authUserData.person,
    };

    localStorage.setItem("userData", JSON.stringify(userData));
  }

  dologout(): void {
    localStorage.removeItem("userData");
  }

  getJwtToken(): string | null {
    const userData = localStorage.getItem("userData");
    if (userData) {
      return JSON.parse(userData).jwt;
    }
    return null;
  }

  getUserData(): UserDataInterface | null {
    const userData = localStorage.getItem("userData");
    if (userData) {
      return JSON.parse(userData);
    }
    return null;
  }

  isLoggedIn(): boolean {
    const token = this.getJwtToken();

    if (!token) return false;

    const payload = this.getJWTokenPayload(token);
    if (!payload) return false;

    return !this.isTokenExpired(payload.exp);
  }

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

  private getJWTokenPayload(token: string) {
    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      return payload;
    } catch (error) {
      return null;
    }
  }

  private isTokenExpired(expiration: number): boolean {
    if (!expiration) {
      return true;
    }
    const now = Math.floor(Date.now() / 1000);
    return expiration < now;
  }
}
