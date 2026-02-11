import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { UserSerializer } from "../domain/serializers/user.serializer";
import { environment } from "../../../environments/environment";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../../../shared/interfaces/api-response.interface";
import { UserInterface } from "../domain/interfaces/user.interface";
import { UserDto } from "../domain/dtos/user.dto";

/**
 * Service for managing system users.
 *
 * This service handles all user-related operations including:
 * - CRUD operations for user accounts
 * - Retrieving paginated and sorted user lists
 * - Managing user profiles and credentials
 * - Data serialization between DTOs and domain models
 */
@Injectable({ providedIn: "root" })
export class UserService {
  private readonly userSerializer = new UserSerializer();
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Retrieves a paginated and sorted list of all users.
   *
   * @param page - Page number (0-indexed), defaults to 0
   * @param size - Number of items per page, defaults to 100
   * @param sortBy - Field name to sort by, defaults to 'username'
   * @param direction - Sort direction ('asc' or 'desc'), defaults to 'asc'
   * @returns Observable with paginated user data
   */
  getAll(
    page: number = 0,
    size: number = 100,
    sortBy: string = "username",
    direction: string = "asc",
  ): Observable<ApiResponseInterface<PagedDataInterface<UserDto[]>>> {
    const params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString())
      .set("sortBy", sortBy)
      .set("direction", direction);

    return this.http.get<ApiResponseInterface<PagedDataInterface<UserDto[]>>>(
      `${this.apiUrl}/user/all`,
      { params },
    );
  }

  /**
   * Creates a new user account.
   *
   * Serializes the user data and sends it to the backend API.
   *
   * @param user - The user data to create
   * @returns Observable with the created user data
   */
  create(user: UserInterface): Observable<ApiResponseInterface<UserDto>> {
    const serializedUser = this.userSerializer.toCreateDto(user);
    return this.http.post<ApiResponseInterface<UserDto>>(
      `${this.apiUrl}/user`,
      serializedUser,
    );
  }

  /**
   * Updates an existing user account.
   *
   * Serializes the updated user data and sends it to the backend API.
   *
   * @param user - The updated user data
   * @returns Observable with the updated user data
   */
  update(user: UserInterface): Observable<ApiResponseInterface<UserDto>> {
    const userSerialized = this.userSerializer.toCreateDto(user);
    return this.http.patch<ApiResponseInterface<UserDto>>(
      `${this.apiUrl}/user/${user.id}`,
      userSerialized,
    );
  }

  /**
   * Retrieves a single user by their ID.
   *
   * Deserializes the response data into the domain model.
   *
   * @param id - The ID of the user to retrieve
   * @returns Observable with the user data
   */
  getById(id: number): Observable<ApiResponseInterface<UserInterface>> {
    return this.http
      .get<ApiResponseInterface<UserDto>>(`${this.apiUrl}/user/${id}`)
      .pipe(
        map((response) => ({
          ...response,
          data: this.userSerializer.toView(response.data),
        })),
      );
  }
}
