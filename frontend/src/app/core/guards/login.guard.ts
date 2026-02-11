import { inject, Injectable } from "@angular/core";
import { CanActivate, Router } from "@angular/router";
import { LocalStorageService } from "../../shared/services/local-storage.service";

@Injectable({
  providedIn: "root",
})
export class LoginGuard implements CanActivate {
  private readonly localStorageService = inject(LocalStorageService);
  private readonly router = inject(Router);

  canActivate(): boolean {
    if (this.localStorageService.isLoggedIn()) {
      this.router.navigate(["/"]);
      return false;
    }
    return true;
  }
}
