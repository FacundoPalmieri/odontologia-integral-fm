import { inject, Injectable } from "@angular/core";
import { CanActivate, Router } from "@angular/router";
import { LocalStorageService } from "../../shared/services/local-storage.service";

@Injectable({
  providedIn: "root",
})
export class AuthGuard implements CanActivate {
  private readonly localStorageService = inject(LocalStorageService);
  private readonly router = inject(Router);

  canActivate(): boolean {
    if (this.localStorageService.isLoggedIn()) {
      return true;
    } else {
      this.router.navigate(["/login"]);
      return false;
    }
  }
}
