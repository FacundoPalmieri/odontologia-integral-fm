import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { map, Observable } from "rxjs";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import { PermissionInterface } from "../data/interfaces/permission.interface";
import { PermissionDto } from "../data/dtos/permission.dto";
import { PermissionSerializer } from "../data/serializers/permission.serializer";

/**
 * Service for managing permissions.
 *
 * Permissions define what resources can be accessed and what actions
 * can be performed on those resources. This service handles:
 * - Retrieving all permissions
 * - Getting specific permissions by ID
 * - Data serialization between DTOs and domain models
 */
@Injectable({ providedIn: "root" })
export class PermissionService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Retrieves all available permissions in the system.
   *
   * Returns a list of all permissions with their associated actions.
   * The data is deserialized from DTOs to domain models.
   *
   * @returns Observable with array of permission data
   */
  getAll(): Observable<ApiResponseInterface<PermissionInterface[]>> {
    return this.http
      .get<
        ApiResponseInterface<PermissionDto[]>
      >(`${this.apiUrl}/permission/all`)
      .pipe(
        map((response) => ({
          ...response,
          data: response.data.map((permission) =>
            PermissionSerializer.toView(permission),
          ),
        })),
      );
  }

  /**
   * Retrieves a specific permission by its ID.
   *
   * Returns detailed information about a single permission including
   * all its associated actions. The data is deserialized from DTO to domain model.
   *
   * @param id - The ID of the permission to retrieve
   * @returns Observable with the permission data
   */
  getById(id: number): Observable<ApiResponseInterface<PermissionInterface>> {
    return this.http
      .get<
        ApiResponseInterface<PermissionDto>
      >(`${this.apiUrl}/permission/${id}`)
      .pipe(
        map((response) => ({
          ...response,
          data: PermissionSerializer.toView(response.data),
        })),
      );
  }
}
