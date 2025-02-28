import { inject, Injectable } from "@angular/core";
import { CanActivate, Router } from "@angular/router";
import { AuthService } from "../../services/auth.service";

@Injectable({
  providedIn: "root",
})
export class LoginGuard implements CanActivate {
  authService = inject(AuthService);
  router = inject(Router);
  constructor() {}

  canActivate(): boolean {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(["/"]);
      return false;
    }
    return true;
  }
}
