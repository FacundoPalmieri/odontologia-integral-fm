import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { map, Observable } from "rxjs";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import { RoleInterface } from "../data/interfaces/role.interface";
import { RoleDto } from "../data/dtos/role.dto";
import { RoleSerializer } from "../data/serializers/role.serializer";

/**
 * Service for managing user roles.
 *
 * Roles group permissions together and are assigned to users to define
 * their access level in the system. This service handles:
 * - CRUD operations for roles
 * - Retrieving roles with their associated permissions
 * - Data serialization between DTOs and domain models
 */
@Injectable({ providedIn: "root" })
export class RoleService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Retrieves all roles in the system.
   *
   * Returns a list of all roles with their associated permissions.
   * The data is deserialized from DTOs to domain models.
   *
   * @returns Observable with array of role data
   */
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

  /**
   * Retrieves a specific role by its ID.
   *
   * Returns detailed information about a single role including
   * all its associated permissions. The data is deserialized from DTO to domain model.
   *
   * @param id - The ID of the role to retrieve
   * @returns Observable with the role data
   */
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

  /**
   * Creates a new role.
   *
   * Serializes the role data and sends it to the backend API.
   * The response is deserialized back to the domain model.
   *
   * @param role - The role data to create
   * @returns Observable with the created role data
   */
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

  /**
   * Updates an existing role.
   *
   * Serializes the updated role data and sends it to the backend API.
   * The response is deserialized back to the domain model.
   *
   * @param role - The updated role data
   * @returns Observable with the updated role data
   */
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
