import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { map, Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { PermissionInterface } from "../domain/interfaces/permission.interface";

@Injectable({ providedIn: "root" })
export class PermissionService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(): Observable<ApiResponseInterface<PermissionInterface[]>> {
    return this.http
      .get<ApiResponseInterface<any[]>>(
        `${this.apiUrl}/api/permissions/get/all`
      )
      .pipe(
        map((response: ApiResponseInterface<any[]>) => {
          if (response && response.data) {
            const convertedData = response.data.map((item) => ({
              id: item.id,
              permissionName: item.permission,
            }));
            return { ...response, data: convertedData };
          }
          return response;
        })
      );
  }

  getById(id: number): Observable<ApiResponseInterface<PermissionInterface>> {
    return this.http
      .get<ApiResponseInterface<any>>(`${this.apiUrl}/api/permissions/${id}`)
      .pipe(
        map((response: ApiResponseInterface<any>) => {
          if (response && response.data) {
            const convertedData = {
              id: response.data.id,
              permissionName: response.data.permission,
            };
            return { ...response, data: convertedData };
          }
          return response;
        })
      );
  }
}
