import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
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
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(
    page: number = 0,
    size: number = 100
  ): Observable<ApiResponseInterface<PagedDataInterface<UserDtoInterface[]>>> {
    const params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    return this.http.get<
      ApiResponseInterface<PagedDataInterface<UserDtoInterface[]>>
    >(`${this.apiUrl}/user/all`, { params });
  }

  getById(id: number): Observable<ApiResponseInterface<UserInterface>> {
    return this.http.get<ApiResponseInterface<UserInterface>>(
      `${this.apiUrl}/user/${id}`
    );
  }

  create(
    user: UserInterface
  ): Observable<ApiResponseInterface<UserDtoInterface>> {
    const serializedUser = UserSerializer.toCreateDto(user);
    return this.http.post<ApiResponseInterface<UserDtoInterface>>(
      `${this.apiUrl}/user`,
      serializedUser
    );
  }

  update(
    user: UserInterface
  ): Observable<ApiResponseInterface<UserDtoInterface>> {
    return this.http.patch<ApiResponseInterface<UserDtoInterface>>(
      `${this.apiUrl}/user`,
      user
    );
  }
}
