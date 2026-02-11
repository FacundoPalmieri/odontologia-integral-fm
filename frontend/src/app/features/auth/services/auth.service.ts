import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { UserService } from "../../users/services/user.service";
import { Router } from "@angular/router";
import { AccessControlService } from "../../../core/services/access-control.service";
import { environment } from "../../../environments/environment";
import { BehaviorSubject, catchError, Observable, tap, throwError } from "rxjs";
import {
  LoginInterface,
  LogoutInterface,
  ResetPasswordInterface,
  UserDataInterface,
} from "../domain/interfaces/auth.interface";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import { RefreshTokenDataDto } from "../domain/dtos/auth.dto";
import { RoleInterface } from "../../roles/domain/interfaces/role.interface";
import { LocalStorageService } from "../../../shared/services/local-storage.service";

@Injectable({ providedIn: "root" })
export class AuthService {
  http = inject(HttpClient);
  userService = inject(UserService);
  router = inject(Router);
  accessControlService = inject(AccessControlService);
  localStorageService = inject(LocalStorageService);
  apiUrl = environment.apiUrl;

  refreshTokenInProgress = false;
  refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<
    string | null
  >(null);

  login(
    login: LoginInterface,
  ): Observable<ApiResponseInterface<UserDataInterface>> {
    return this.http.post<ApiResponseInterface<UserDataInterface>>(
      `${this.apiUrl}/auth/login`,
      login,
    );
  }

  logout(logout: LogoutInterface): Observable<ApiResponseInterface<string>> {
    return this.http.delete<ApiResponseInterface<string>>(
      `${this.apiUrl}/auth/logout`,
      {
        body: logout,
      },
    );
  }

  refreshToken(
    refreshTokenData: RefreshTokenDataDto,
  ): Observable<ApiResponseInterface<UserDataInterface>> {
    this.refreshTokenInProgress = true;
    this.refreshTokenSubject.next(null);
    return this.http
      .post<
        ApiResponseInterface<UserDataInterface>
      >(`${this.apiUrl}/auth/token/refresh`, refreshTokenData)
      .pipe(
        tap((response) => {
          const userData = this.localStorageService.getUserData();
          this.localStorageService.doLogin(response.data);
          this.updateRoles(userData?.roles!);
          this.refreshTokenSubject.next(response.data.jwt);
          this.refreshTokenInProgress = false;
        }),
        catchError((error) => {
          this.refreshTokenInProgress = false;
          this.localStorageService.doLogout();
          this.router.navigateByUrl("/login");
          this.refreshTokenSubject.next(null);
          return throwError(() => error);
        }),
      );
  }

  resetPasswordRequest(
    email: string,
  ): Observable<ApiResponseInterface<string>> {
    const params = new HttpParams().set("email", email);

    return this.http.post<ApiResponseInterface<string>>(
      `${this.apiUrl}/auth/password/reset-request`,
      null,
      { params },
    );
  }

  resetPassword(
    resetData: ResetPasswordInterface,
  ): Observable<ApiResponseInterface<string>> {
    return this.http.post<ApiResponseInterface<string>>(
      `${this.apiUrl}/auth/password/reset`,
      resetData,
    );
  }

  updateRoles(roles: RoleInterface[]) {
    let userData = this.localStorageService.getUserData();
    userData = {
      ...userData!,
      roles: roles,
    };
    this.localStorageService.doLogin(userData);
  }
}
