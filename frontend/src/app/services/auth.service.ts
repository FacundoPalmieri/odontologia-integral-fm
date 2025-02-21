import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { LoginInterface } from "../domain/interfaces/login.interface";
import { Observable } from "rxjs";
import { AuthUserInterface } from "../domain/interfaces/auth-user.interface";

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

  doLogin(authUserData: AuthUserInterface) {
    const userData = {
      username: authUserData.username,
      jwt: authUserData.jwt,
    };

    localStorage.setItem("userData", JSON.stringify(userData));
  }
}
