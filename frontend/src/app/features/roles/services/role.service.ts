import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { map, Observable } from "rxjs";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import { RoleInterface } from "../domain/interfaces/role.interface";
import { RoleSerializer } from "../domain/serializers/role.serializer";
import { RoleDto } from "../domain/dtos/role.dto";

@Injectable({ providedIn: "root" })
export class RoleService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(): Observable<ApiResponseInterface<RoleInterface[]>> {
    return this.http
      .get<ApiResponseInterface<RoleDto[]>>(`${this.apiUrl}/role/all`)
      .pipe(
        map((response) => ({
          ...response,
          data: response.data.map((role) => RoleSerializer.toView(role)),
        })),
      );
  }

  getById(id: number): Observable<ApiResponseInterface<RoleInterface>> {
    return this.http
      .get<ApiResponseInterface<RoleDto>>(`${this.apiUrl}/role/${id}`)
      .pipe(
        map((response) => ({
          ...response,
          data: RoleSerializer.toView(response.data),
        })),
      );
  }

  create(role: RoleInterface): Observable<ApiResponseInterface<RoleInterface>> {
    const roleCreateDto = RoleSerializer.toCreateDto(role);
    return this.http
      .post<ApiResponseInterface<RoleDto>>(`${this.apiUrl}/role`, roleCreateDto)
      .pipe(
        map((response) => ({
          ...response,
          data: RoleSerializer.toView(response.data),
        })),
      );
  }

  update(role: RoleInterface): Observable<ApiResponseInterface<RoleInterface>> {
    const roleUpdateDto = RoleSerializer.toUpdateDto(role);
    return this.http
      .patch<
        ApiResponseInterface<RoleDto>
      >(`${this.apiUrl}/role`, roleUpdateDto)
      .pipe(
        map((response) => ({
          ...response,
          data: RoleSerializer.toView(response.data),
        })),
      );
  }
}
