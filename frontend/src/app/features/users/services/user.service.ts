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

@Injectable({ providedIn: "root" })
export class UserService {
  private readonly userSerializer = new UserSerializer();
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

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

  create(user: UserInterface): Observable<ApiResponseInterface<UserDto>> {
    const serializedUser = this.userSerializer.toCreateDto(user);
    return this.http.post<ApiResponseInterface<UserDto>>(
      `${this.apiUrl}/user`,
      serializedUser,
    );
  }

  update(user: UserInterface): Observable<ApiResponseInterface<UserDto>> {
    const userSerialized = this.userSerializer.toCreateDto(user);
    return this.http.patch<ApiResponseInterface<UserDto>>(
      `${this.apiUrl}/user/${user.id}`,
      userSerialized,
    );
  }

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
