import { Component, computed, inject, OnDestroy, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";
import { MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatListModule } from "@angular/material/list";
import { MatSidenavModule } from "@angular/material/sidenav";
import { MatMenuModule } from "@angular/material/menu";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { ThemeService } from "../../../services/theme.service";
import { AuthService } from "../../../services/auth.service";
import { UserDataInterface } from "../../../domain/interfaces/user-data.interface";
import { MatDividerModule } from "@angular/material/divider";
import { PermissionFactory } from "../../../utils/factories/permission.factory";
import { MenuItemInterface } from "../../../domain/interfaces/menu-item.interface";
import { FullscreenService } from "../../../services/fullscreen.service";
import { TreatmentService } from "../../../services/treatment.service";
import { TreatmentReferencesComponent } from "../../components/treatment-references/treatment-references.component";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";
import { Subject, takeUntil } from "rxjs";
import { MatBadgeModule } from "@angular/material/badge";
import { PersonDataService } from "../../../services/person-data.service";
import { AccessControlService } from "../../../services/access-control.service";

@Component({
  selector: "app-home",
  templateUrl: "./home.component.html",
  styleUrl: "./home.component.scss",
  standalone: true,
  imports: [
    CommonModule,
    MatSidenavModule,
    MatToolbarModule,
    MatIconModule,
    MatButtonModule,
    MatListModule,
    MatTooltipModule,
    MatMenuModule,
    MatDividerModule,
    RouterModule,
    IconsModule,
    TreatmentReferencesComponent,
    MatBadgeModule,
  ],
})
export class HomeComponent implements OnInit, OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly themeService = inject(ThemeService);
  private readonly authService = inject(AuthService);
  private readonly personDataService = inject(PersonDataService);
  private readonly accessControlService = inject(AccessControlService);

  router = inject(Router);
  fullScreenService = inject(FullscreenService);
  treatmentReferencesService = inject(TreatmentService);
  currentTheme = computed(() => this.themeService.currentTheme());
  userData: UserDataInterface | null = this.authService.getUserData();
  permissions: string[] = [];
  private menuItems = PermissionFactory.createPermissions();
  filteredMenuItems: MenuItemInterface[] = [];
  avatar: string | null = null;

  constructor() {
    if (this.authService.isLoggedIn()) {
      this.accessControlService.initializePermissions();
    }
  }

  ngOnInit() {
    if (this.userData?.roles && this.userData?.roles.length > 0) {
      this.userData.roles.forEach((role) => {
        if (role.permissionsList) {
          role.permissionsList.forEach((permissionObject) => {
            this.permissions.push(permissionObject.name);
          });
        }
      });
      if (this.userData.person?.id) {
        this.personDataService
          .getAvatar(this.userData.person.id)
          .subscribe((avatar) => {
            this.avatar = avatar;
          });
      }
      this.permissions = [...new Set(this.permissions)];
      this.filteredMenuItems = this.filterMenuItems();
    }
    // Revisar si es necesario para todos los roles.
    if (this.personDataService.nationalities().length === 0) {
      this.personDataService.loadAllCatalogs().subscribe();
    }
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  private filterMenuItems(): MenuItemInterface[] {
    return this.menuItems.filter((item) =>
      this.permissions.includes(item.permissionEnum)
    );
  }

  logout() {
    const logoutData = this.authService.getLogoutData();
    this.authService
      .logout(logoutData!)
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<string>) => {
        if (response.success) {
          this.authService.dologout();
          this.router.navigate(["/login"]);
        }
      });
  }

  getRoles(): string {
    if (this.userData?.roles && this.userData.roles.length > 0) {
      return this.userData.roles.map((role) => role.label).join(", ");
    }
    return "";
  }

  toggleTheme() {
    const newTheme = this.currentTheme().id === "light" ? "dark" : "light";
    this.themeService.setTheme(newTheme);
  }

  goToProfile() {
    this.router.navigate(["/profile"]);
  }
}
