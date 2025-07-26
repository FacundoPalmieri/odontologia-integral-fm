import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../domain/interfaces/api-response.interface";
import { ActionInterface } from "../domain/interfaces/role.interface";

@Injectable({ providedIn: "root" })
export class ActionService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  getAll(): Observable<ApiResponseInterface<ActionInterface[]>> {
    return this.http.get<ApiResponseInterface<ActionInterface[]>>(
      `${this.apiUrl}/action/all`
    );
  }
}
