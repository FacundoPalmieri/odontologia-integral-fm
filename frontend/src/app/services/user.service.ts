import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { UserInterface } from "../domain/interfaces/user.interface";

@Injectable({ providedIn: "root" })
export class UserService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(): Observable<ApiResponseInterface<UserInterface[]>> {
    return this.http.get<ApiResponseInterface<UserInterface[]>>(
      `${this.apiUrl}/api/users/get/all`
    );
  }

  getById(id: number): Observable<ApiResponseInterface<UserInterface>> {
    return this.http.get<ApiResponseInterface<UserInterface>>(
      `${this.apiUrl}/api/users/${id}`
    );
  }
}
