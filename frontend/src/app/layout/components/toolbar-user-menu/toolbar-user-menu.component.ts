import {
  Component,
  inject,
  OnDestroy,
  OnInit,
  ChangeDetectionStrategy,
  signal,
} from "@angular/core";
import { Router } from "@angular/router";
import { Subject, takeUntil } from "rxjs";
import { MatMenuModule } from "@angular/material/menu";
import { MatButtonModule } from "@angular/material/button";
import { MatDividerModule } from "@angular/material/divider";
import { IconsModule } from "../../../core/modules/tabler-icons.module";
import { AuthService } from "../../../features/auth/services/auth.service";
import { LocalStorageService } from "../../../shared/services/local-storage.service";
import { PersonDataService } from "../../../shared/services/person-data.service";
import { ApiResponseInterface } from "../../../shared/interfaces/api-response.interface";
import { UserDataInterface } from "../../../features/auth/data/interfaces/auth.interface";

@Component({
  selector: "app-toolbar-user-menu",
  templateUrl: "./toolbar-user-menu.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatMenuModule, MatButtonModule, MatDividerModule, IconsModule],
})
export class ToolbarUserMenuComponent implements OnInit, OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly localStorageService = inject(LocalStorageService);
  private readonly personDataService = inject(PersonDataService);

  userData: UserDataInterface | null = this.localStorageService.getUserData();
  avatar = signal<string>("img/doctor-avatar.png");

  ngOnInit(): void {
    if (this.userData?.person?.id) {
      this.personDataService
        .getAvatar(this.userData.person.id)
        .subscribe((avatarUrl) => {
          this.avatar.set(avatarUrl ?? "img/doctor-avatar.png");
        });
    }
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  getRoles(): string {
    if (this.userData?.roles && this.userData.roles.length > 0) {
      return this.userData.roles.map((role) => role.label).join(", ");
    }
    return "";
  }

  goToProfile(): void {
    this.router.navigate(["/profile"]);
  }

  isDeveloper(): boolean {
    if (!this.userData?.roles) return false;
    return this.userData.roles.some(
      (role) =>
        role.name.toLowerCase().includes("developer") ||
        role.label.toLowerCase().includes("desarrollador"),
    );
  }

  logout(): void {
    const logoutData = this.localStorageService.getLogoutData();
    this.authService
      .logout(logoutData!)
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<string>) => {
        if (response.success) {
          this.localStorageService.doLogout();
          this.router.navigate(["/login"]);
        }
      });
  }
}
