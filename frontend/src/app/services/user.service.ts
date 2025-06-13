import { HttpClient, HttpParams } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../domain/interfaces/api-response.interface";
import { UserInterface } from "../domain/interfaces/user.interface";
import { UserCreateDtoInterface } from "../domain/dto/user.dto";

@Injectable({ providedIn: "root" })
export class UserService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(
    page: number = 0,
    size: number = 100
  ): Observable<ApiResponseInterface<PagedDataInterface<UserInterface[]>>> {
    const params = new HttpParams()
      .set("page", page.toString())
      .set("size", size.toString());

    return this.http.get<
      ApiResponseInterface<PagedDataInterface<UserInterface[]>>
    >(`${this.apiUrl}/user/all`, { params });
  }

  getById(id: number): Observable<ApiResponseInterface<UserInterface>> {
    return this.http.get<ApiResponseInterface<UserInterface>>(
      `${this.apiUrl}/user/${id}`
    );
  }

  create(
    user: UserCreateDtoInterface
  ): Observable<ApiResponseInterface<UserInterface>> {
    return this.http.post<ApiResponseInterface<UserInterface>>(
      `${this.apiUrl}/user`,
      user
    );
  }

  update(user: UserInterface): Observable<ApiResponseInterface<UserInterface>> {
    return this.http.patch<ApiResponseInterface<UserInterface>>(
      `${this.apiUrl}/user`,
      user
    );
  }
}
