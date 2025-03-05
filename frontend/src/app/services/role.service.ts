import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { RoleInterface } from "../domain/interfaces/rol.interface";

@Injectable({ providedIn: "root" })
export class RoleService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(): Observable<ApiResponseInterface<RoleInterface[]>> {
    return this.http.get<ApiResponseInterface<RoleInterface[]>>(
      `${this.apiUrl}/api/roles/get/all`
    );
  }

  getById(id: number): Observable<ApiResponseInterface<RoleInterface>> {
    return this.http.get<ApiResponseInterface<RoleInterface>>(
      `${this.apiUrl}/api/roles/${id}`
    );
  }
}
