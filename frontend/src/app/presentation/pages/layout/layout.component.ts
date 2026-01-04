import {
  Component,
  computed,
  inject,
  OnDestroy,
  OnInit,
  ViewChildren,
  QueryList,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { Router, RouterModule } from "@angular/router";
import { MatIconModule } from "@angular/material/icon";
import { MatButtonModule } from "@angular/material/button";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatListModule } from "@angular/material/list";
import { MatSidenavModule } from "@angular/material/sidenav";
import { MatMenuModule, MatMenu } from "@angular/material/menu";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { ThemeService } from "../../../services/theme.service";
import { AuthService } from "../../../services/auth.service";
import { UserDataInterface } from "../../../domain/interfaces/user-data.interface";
import { MatDividerModule } from "@angular/material/divider";
import { PermissionFactory } from "../../../utils/factories/permission.factory";
import { MenuItemInterface } from "../../../domain/interfaces/menu-item.interface";
import { FullscreenService } from "../../../services/fullscreen.service";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";
import { Subject, takeUntil } from "rxjs";
import { MatBadgeModule } from "@angular/material/badge";
import { PersonDataService } from "../../../services/person-data.service";
import { AccessControlService } from "../../../services/access-control.service";
import { PermissionsEnum } from "../../../utils/enums/permissions.enum";

@Component({
  selector: "app-layout",
  templateUrl: "./layout.component.html",
  styleUrl: "./layout.component.scss",
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
    MatBadgeModule,
  ],
})
export class LayoutComponent implements OnInit, OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly themeService = inject(ThemeService);
  private readonly authService = inject(AuthService);
  private readonly personDataService = inject(PersonDataService);
  private readonly accessControlService = inject(AccessControlService);
  private readonly router = inject(Router);

  fullScreenService = inject(FullscreenService);
  currentTheme = computed(() => this.themeService.currentTheme());
  logoSrc = computed(() => {
    const theme = this.currentTheme();
    return theme.id.includes("dark")
      ? "img/logo_dark.jpg"
      : "img/logo_light.jpg";
  });
  userData: UserDataInterface | null = this.authService.getUserData();
  permissions: string[] = [];
  private menuItems = PermissionFactory.createPermissions();
  filteredMenuItems: MenuItemInterface[] = [];
  avatar: string | null = null;
  expandedMenus: { [label: string]: boolean } = {};
  @ViewChildren("menuTemplate") menuTemplates!: QueryList<MatMenu>;

  homeMenu: MenuItemInterface = {
    permissionEnum: PermissionsEnum.HOME,
    route: "/home",
    icon: "home",
    label: "Inicio",
  };

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
            if (avatar) {
              this.avatar = avatar;
            } else {
              this.avatar = "img/doctor-avatar.png";
            }
          });
      }
      this.permissions = [...new Set(this.permissions)];
      this.filteredMenuItems = this.filterMenuItems();
    }
    if (
      this.personDataService.nationalities().length === 0 &&
      this.userData?.roles
    ) {
      this.personDataService.loadAllCatalogs().subscribe();
    }
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  private filterMenuItems(): MenuItemInterface[] {
    const filteredItems = this.menuItems
      .filter((item) => this.permissions.includes(item.permissionEnum))
      .map((item) => ({
        ...item,
        children: item.children
          ? item.children.filter((child) =>
              this.permissions.includes(child.permissionEnum)
            )
          : undefined,
      }));

    return [this.homeMenu, ...filteredItems];
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

  getAvailableThemes() {
    return this.themeService.getThemes();
  }

  setTheme(themeId: string) {
    this.themeService.setTheme(themeId);
  }

  isCurrentTheme(themeId: string): boolean {
    return this.currentTheme().id === themeId;
  }

  goToProfile() {
    this.router.navigate(["/profile"]);
  }

  isDeveloper(): boolean {
    if (!this.userData?.roles) return false;
    return this.userData.roles.some(
      (role) =>
        role.name.toLowerCase().includes("developer") ||
        role.label.toLowerCase().includes("desarrollador")
    );
  }

  toggleSubmenu(label: string) {
    this.expandedMenus[label] = !this.expandedMenus[label];
  }

  getMenuForItem(label: string): MatMenu | null {
    if (!this.menuTemplates) {
      return null;
    }

    const menus = this.menuTemplates.toArray();
    const menusWithChildren = this.filteredMenuItems.filter(
      (item) => item.children && item.children.length > 0
    );

    const menuIndex = menusWithChildren.findIndex(
      (item) => item.label === label
    );
    return menus[menuIndex] || null;
  }
}
