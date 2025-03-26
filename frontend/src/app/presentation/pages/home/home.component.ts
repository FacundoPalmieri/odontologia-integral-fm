import { Component, computed, inject, OnInit } from "@angular/core";
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
import { TreatmentReferencesSidenavService } from "../../../services/treatment-references-sidenav.service";
import { TreatmentReferencesComponent } from "../../components/treatment-references/treatment-references.component";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";

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
  ],
})
export class HomeComponent implements OnInit {
  themeService = inject(ThemeService);
  authService = inject(AuthService);
  router = inject(Router);
  fullScreenService = inject(FullscreenService);
  treatmentReferencesService = inject(TreatmentReferencesSidenavService);
  showFiller = false;
  currentTheme = computed(() => this.themeService.currentTheme());
  userData: UserDataInterface | null = this.authService.getUserData();
  permissions: string[] = [];
  private menuItems = PermissionFactory.createPermissions();
  filteredMenuItems: MenuItemInterface[] = [];
  isFullscreen = false;

  constructor() {}

  ngOnInit() {
    if (this.userData?.roleAndPermission) {
      this.permissions = this.userData.roleAndPermission.slice(1);
      this.filteredMenuItems = this.filterMenuItems();
    }
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
      .subscribe((response: ApiResponseInterface<string>) => {
        if (response.success) {
          this.authService.dologout();
          this.router.navigate(["/login"]);
        }
      });
  }

  obtenerRole(): string | undefined {
    return this.userData?.roleAndPermission[0].split("_")[1];
  }

  toggleTheme() {
    const newTheme = this.currentTheme().id === "light" ? "dark" : "light";
    this.themeService.setTheme(newTheme);
  }
}
