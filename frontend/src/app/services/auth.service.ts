import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";
import { catchError, throwError } from "rxjs";

@Injectable({ providedIn: "root" })
export class authService {
  http = inject(HttpClient);

  logIn() {
    return this.http.get("http://localhost:8080/products");
  }
}
