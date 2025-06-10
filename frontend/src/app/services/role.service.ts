import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { map, Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { RoleInterface } from "../domain/interfaces/role.interface";
import { RoleDtoInterface } from "../domain/dto/role.dto";
import { RoleSerializer } from "../domain/serializers/role.serializer";

@Injectable({ providedIn: "root" })
export class RoleService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(): Observable<ApiResponseInterface<RoleInterface[]>> {
    return this.http
      .get<ApiResponseInterface<RoleDtoInterface[]>>(`${this.apiUrl}/role/all`)
      .pipe(
        map((response) => ({
          ...response,
          data: response.data.map((role) => RoleSerializer.toView(role)),
        }))
      );
  }

  getById(id: number): Observable<ApiResponseInterface<RoleInterface>> {
    return this.http
      .get<ApiResponseInterface<RoleDtoInterface>>(`${this.apiUrl}/role/${id}`)
      .pipe(
        map((response) => ({
          ...response,
          data: RoleSerializer.toView(response.data),
        }))
      );
  }

  create(role: RoleInterface): Observable<ApiResponseInterface<RoleInterface>> {
    return this.http.post<ApiResponseInterface<RoleDtoInterface>>(
      `${this.apiUrl}/role`,
      RoleSerializer.toCreateDto(role)
    );
  }

  update(role: RoleInterface): Observable<ApiResponseInterface<RoleInterface>> {
    return this.http.patch<ApiResponseInterface<RoleDtoInterface>>(
      `${this.apiUrl}/role`,
      RoleSerializer.toUpdateDto(role)
    );
  }
}
