import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { UserInterface } from "../domain/interfaces/user.interface";
import { UserUpdateDto } from "../domain/dto/user-update.dto";
import { UserCreateDto } from "../domain/dto/user-create.dto";

@Injectable({ providedIn: "root" })
export class UserService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(): Observable<ApiResponseInterface<UserInterface[]>> {
    return this.http.get<ApiResponseInterface<UserInterface[]>>(
      `${this.apiUrl}/api/users/get/all`
    );
  }

  getById(id: number): Observable<ApiResponseInterface<UserInterface>> {
    return this.http.get<ApiResponseInterface<UserInterface>>(
      `${this.apiUrl}/api/users/${id}`
    );
  }

  createUser(
    user: UserCreateDto
  ): Observable<ApiResponseInterface<UserInterface>> {
    return this.http.post<ApiResponseInterface<UserInterface>>(
      `${this.apiUrl}/api/users/create`,
      user
    );
  }

  updateUser(
    user: UserUpdateDto
  ): Observable<ApiResponseInterface<UserInterface>> {
    console.log(user);
    return this.http.patch<ApiResponseInterface<UserInterface>>(
      `${this.apiUrl}/api/users/update`,
      user
    );
  }
}
