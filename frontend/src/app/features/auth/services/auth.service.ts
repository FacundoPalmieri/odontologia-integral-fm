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

/**
 * Service for handling user authentication and authorization.
 *
 * This service manages:
 * - User login and logout operations
 * - JWT token refresh mechanism
 * - Password reset functionality
 * - User role updates
 */
@Injectable({ providedIn: "root" })
export class AuthService {
  http = inject(HttpClient);
  userService = inject(UserService);
  router = inject(Router);
  accessControlService = inject(AccessControlService);
  localStorageService = inject(LocalStorageService);
  apiUrl = environment.apiUrl;

  /** Flag indicating if a token refresh is currently in progress */
  refreshTokenInProgress = false;

  /** Subject for managing token refresh state */
  refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<
    string | null
  >(null);

  /**
   * Authenticates a user with their credentials.
   *
   * @param login - Login credentials (username/email and password)
   * @returns Observable with user data including JWT tokens
   */
  login(
    login: LoginInterface,
  ): Observable<ApiResponseInterface<UserDataInterface>> {
    return this.http.post<ApiResponseInterface<UserDataInterface>>(
      `${this.apiUrl}/auth/login`,
      login,
    );
  }

  /**
   * Logs out the current user and invalidates their tokens.
   *
   * @param logout - Logout data containing user ID and tokens
   * @returns Observable with logout confirmation
   */
  logout(logout: LogoutInterface): Observable<ApiResponseInterface<string>> {
    return this.http.delete<ApiResponseInterface<string>>(
      `${this.apiUrl}/auth/logout`,
      {
        body: logout,
      },
    );
  }

  /**
   * Refreshes the JWT access token using the refresh token.
   *
   * This method is called when the access token expires to obtain a new one
   * without requiring the user to log in again.
   *
   * @param refreshTokenData - Data containing the refresh token
   * @returns Observable with new user data including refreshed JWT
   */
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

  /**
   * Initiates a password reset request for a user.
   *
   * Sends a password reset email to the specified email address.
   *
   * @param email - Email address of the user requesting password reset
   * @returns Observable with confirmation message
   */
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

  /**
   * Completes the password reset process with a new password.
   *
   * @param resetData - Password reset data including token and new password
   * @returns Observable with confirmation message
   */
  resetPassword(
    resetData: ResetPasswordInterface,
  ): Observable<ApiResponseInterface<string>> {
    return this.http.post<ApiResponseInterface<string>>(
      `${this.apiUrl}/auth/password/reset`,
      resetData,
    );
  }

  /**
   * Updates the user's roles in localStorage.
   *
   * This method is used to refresh role information without requiring a full re-login.
   *
   * @param roles - Updated array of user roles
   * @returns void
   */
  updateRoles(roles: RoleInterface[]) {
    let userData = this.localStorageService.getUserData();
    userData = {
      ...userData!,
      roles: roles,
    };
    this.localStorageService.doLogin(userData);
  }
}
