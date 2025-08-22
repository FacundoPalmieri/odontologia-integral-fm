import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { map, Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { PermissionInterface } from "../domain/interfaces/role.interface";
import { PermissionDtoInterface } from "../domain/dto/role.dto";
import { PermissionSerializer } from "../domain/serializers/permission.serializer";

@Injectable({ providedIn: "root" })
export class PermissionService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(): Observable<ApiResponseInterface<PermissionInterface[]>> {
    return this.http
      .get<ApiResponseInterface<PermissionDtoInterface[]>>(
        `${this.apiUrl}/permission/all`
      )
      .pipe(
        map((response) => ({
          ...response,
          data: response.data.map((permission) =>
            PermissionSerializer.toView(permission)
          ),
        }))
      );
  }

  getById(id: number): Observable<ApiResponseInterface<PermissionInterface>> {
    return this.http
      .get<ApiResponseInterface<PermissionDtoInterface>>(
        `${this.apiUrl}/permission/${id}`
      )
      .pipe(
        map((response) => ({
          ...response,
          data: PermissionSerializer.toView(response.data),
        }))
      );
  }
}
