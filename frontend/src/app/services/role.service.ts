import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { RoleInterface } from "../domain/interfaces/role.interface";
import { RoleUpdateDto } from "../domain/dto/role-update.dto";
import { RoleCreateDto } from "../domain/dto/role-create.dto";

@Injectable({ providedIn: "root" })
export class RoleService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(): Observable<ApiResponseInterface<RoleInterface[]>> {
    return this.http.get<ApiResponseInterface<RoleInterface[]>>(
      `${this.apiUrl}/api/role/all`
    );
  }

  getById(id: number): Observable<ApiResponseInterface<RoleInterface>> {
    return this.http.get<ApiResponseInterface<RoleInterface>>(
      `${this.apiUrl}/api/role/${id}`
    );
  }

  create(role: RoleCreateDto): Observable<ApiResponseInterface<RoleInterface>> {
    return this.http.post<ApiResponseInterface<RoleInterface>>(
      `${this.apiUrl}/api/role`,
      role
    );
  }

  update(role: RoleUpdateDto): Observable<ApiResponseInterface<RoleInterface>> {
    return this.http.patch<ApiResponseInterface<RoleInterface>>(
      `${this.apiUrl}/api/role`,
      role
    );
  }
}
