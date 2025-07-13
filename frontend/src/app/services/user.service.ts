import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { map, Observable } from "rxjs";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../domain/interfaces/api-response.interface";
import { UserInterface } from "../domain/interfaces/user.interface";
import {
  UserCreateDtoInterface,
  UserDtoInterface,
} from "../domain/dto/user.dto";
import { UserSerializer } from "../domain/serializers/user.serializer";

@Injectable({ providedIn: "root" })
export class UserService {
  private readonly userSerializer = new UserSerializer();
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(
    page: number = 0,
    size: number = 100,
    sortBy: string = "username",
    direction: string = "asc"
  ): Observable<ApiResponseInterface<PagedDataInterface<UserDtoInterface[]>>> {
    const params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString())
      .set("sortBy", sortBy)
      .set("direction", direction);

    return this.http.get<
      ApiResponseInterface<PagedDataInterface<UserDtoInterface[]>>
    >(`${this.apiUrl}/user/all`, { params });
  }

  create(
    user: UserInterface
  ): Observable<ApiResponseInterface<UserDtoInterface>> {
    const serializedUser = this.userSerializer.toCreateDto(user);
    return this.http.post<ApiResponseInterface<UserDtoInterface>>(
      `${this.apiUrl}/user`,
      serializedUser
    );
  }

  update(
    user: UserInterface
  ): Observable<ApiResponseInterface<UserDtoInterface>> {
    const userSerialized = this.userSerializer.toCreateDto(user);
    return this.http.patch<ApiResponseInterface<UserDtoInterface>>(
      `${this.apiUrl}/user`,
      userSerialized
    );
  }

  getById(id: number): Observable<ApiResponseInterface<UserInterface>> {
    return this.http
      .get<ApiResponseInterface<UserDtoInterface>>(`${this.apiUrl}/user/${id}`)
      .pipe(
        map((response) => ({
          ...response,
          data: this.userSerializer.toView(response.data),
        }))
      );
  }
}
