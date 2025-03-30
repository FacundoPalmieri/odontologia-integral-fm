import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { LoginInterface } from "../domain/interfaces/login.interface";
import { Observable } from "rxjs";
import { AuthUserInterface } from "../domain/interfaces/auth-user.interface";
import { ResetPasswordInterface } from "../domain/interfaces/reset-password.interface";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { UserDataInterface } from "../domain/interfaces/user-data.interface";
import { LogoutInterface } from "../domain/interfaces/logout.interface";

@Injectable({ providedIn: "root" })
export class AuthService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  login(login: LoginInterface): Observable<AuthUserInterface> {
    return this.http.post<AuthUserInterface>(
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

  resetPasswordRequest(
    email: string
  ): Observable<ApiResponseInterface<string>> {
    const params = new HttpParams().set("email", email);

    return this.http.post<ApiResponseInterface<string>>(
      `${this.apiUrl}/auth/password/reset`,
      null,
      { params }
    );
  }

  resetPassword(
    resetData: ResetPasswordInterface
  ): Observable<ApiResponseInterface<string>> {
    return this.http.post<ApiResponseInterface<string>>(
      `${this.apiUrl}/auth/password/reset-request`,
      resetData
    );
  }

  doLogin(authUserData: AuthUserInterface) {
    const userData: UserDataInterface = {
      username: authUserData.username,
      jwt: authUserData.jwt,
      refreshToken: authUserData.refreshToken,
      roleAndPermission: authUserData.roleAndPermission,
    };

    localStorage.setItem("userData", JSON.stringify(userData));
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
        user_id: 2, // TODO -> no tengo el id en el login
        username: userData.username,
      };
      return logoutData;
    } else {
      return null;
    }
  }

  dologout(): void {
    localStorage.removeItem("userData");
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
