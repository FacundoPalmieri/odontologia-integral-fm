import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { LoginInterface } from "../domain/interfaces/login.interface";
import { Observable } from "rxjs";
import { UserInterface } from "../domain/interfaces/user.interface";

@Injectable({ providedIn: "root" })
export class AuthService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  logIn(login: LoginInterface): Observable<UserInterface> {
    return this.http.post<UserInterface>(`${this.apiUrl}/auth/login`, login);
  }
}
