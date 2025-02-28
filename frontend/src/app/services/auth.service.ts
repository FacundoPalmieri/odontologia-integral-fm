import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { LoginInterface } from "../domain/interfaces/login.interface";
import { Observable } from "rxjs";
import { AuthUserInterface } from "../domain/interfaces/auth-user.interface";
import { ResetPasswordInterface } from "../domain/interfaces/reset-password.interface";
import { ApiResponseInterface } from "../domain/interfaces/api-error.interface";

@Injectable({ providedIn: "root" })
export class AuthService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  logIn(login: LoginInterface): Observable<AuthUserInterface> {
    return this.http.post<AuthUserInterface>(
      `${this.apiUrl}/auth/login`,
      login
    );
  }

  resetPasswordRequest(email: string): Observable<ApiResponseInterface> {
    const params = new HttpParams().set("email", email);

    return this.http.post<ApiResponseInterface>(
      `${this.apiUrl}/auth/request/reset-password`,
      null,
      { params }
    );
  }

  resetPassword(
    resetData: ResetPasswordInterface
  ): Observable<ApiResponseInterface> {
    return this.http.post<ApiResponseInterface>(
      `${this.apiUrl}/auth/reset-password`,
      resetData
    );
  }

  doLogin(authUserData: AuthUserInterface) {
    const userData = {
      username: authUserData.username,
      jwt: authUserData.jwt,
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
}
