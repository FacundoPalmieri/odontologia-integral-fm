import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { environment } from "../../../environments/environment";
import { Observable } from "rxjs";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import { ActionInterface } from "../domain/interfaces/permission.interface";

/**
 * Service for managing permission actions.
 *
 * Actions represent the operations that can be performed on resources
 * (e.g., CREATE, READ, UPDATE, DELETE, EXECUTE).
 * This service retrieves the available actions that can be assigned to permissions.
 */
@Injectable({ providedIn: "root" })
export class ActionService {
  http = inject(HttpClient);
  apiUrl = environment.apiUrl;

  /**
   * Retrieves all available permission actions.
   *
   * Returns a list of all actions that can be assigned to permissions,
   * such as CREATE, READ, UPDATE, DELETE, etc.
   *
   * @returns Observable with array of action data
   */
  getAll(): Observable<ApiResponseInterface<ActionInterface[]>> {
    return this.http.get<ApiResponseInterface<ActionInterface[]>>(
      `${this.apiUrl}/action/all`,
    );
  }
}
