import {
  Component,
  computed,
  inject,
  OnDestroy,
  OnInit,
  ViewChildren,
  QueryList,
  signal,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { RouterModule } from "@angular/router";
import { MatButtonModule } from "@angular/material/button";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatListModule } from "@angular/material/list";
import { MatSidenavModule } from "@angular/material/sidenav";
import { MatMenuModule, MatMenu } from "@angular/material/menu";
import { IconsModule } from "../core/modules/tabler-icons.module";
import { ThemeService } from "../core/services/theme.service";
import { PermissionFactory } from "../shared/utils/factories/permission.factory";
import { MenuItemInterface } from "../shared/interfaces/menu-item.interface";
import { FullscreenService } from "../core/services/fullscreen.service";
import { Subject } from "rxjs";
import { PersonDataService } from "../shared/services/person-data.service";
import { PermissionsEnum } from "../shared/utils/enums/permissions.enum";
import { AccessControlService } from "../core/services/access-control.service";
import { LocalStorageService } from "../shared/services/local-storage.service";
import { PermissionInterface } from "../features/roles/data/interfaces/permission.interface";
import { ToolbarPatientSearchComponent } from "./components/toolbar-patient-search/toolbar-patient-search.component";
import { ToolbarUserMenuComponent } from "./components/toolbar-user-menu/toolbar-user-menu.component";
import { ToolbarThemeToggleComponent } from "./components/toolbar-theme-toggle/toolbar-theme-toggle.component";

@Component({
  selector: "app-layout",
  templateUrl: "./layout.component.html",
  styleUrl: "./layout.component.scss",
  standalone: true,
  imports: [
    CommonModule,
    MatSidenavModule,
    MatToolbarModule,
    MatButtonModule,
    MatListModule,
    MatTooltipModule,
    MatMenuModule,
    RouterModule,
    IconsModule,
    ToolbarPatientSearchComponent,
    ToolbarUserMenuComponent,
    ToolbarThemeToggleComponent,
  ],
})
export class LayoutComponent implements OnInit, OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly themeService = inject(ThemeService);
  private readonly personDataService = inject(PersonDataService);
  private readonly accessControlService = inject(AccessControlService);
  private readonly localStorageService = inject(LocalStorageService);

  fullScreenService = inject(FullscreenService);
  currentTheme = computed(() => this.themeService.currentTheme());
  logoSrc = computed(() => {
    const theme = this.currentTheme();
    return theme.id.includes("dark")
      ? "img/odontologia_fm.jpg"
      : "img/odontologia_fm.jpg";
  });
  userData = this.localStorageService.getUserData();
  permissions: string[] = [];
  private menuItems = PermissionFactory.createPermissions();
  filteredMenuItems: MenuItemInterface[] = [];
  expandedMenus: { [label: string]: boolean } = {};
  @ViewChildren("menuTemplate") menuTemplates!: QueryList<MatMenu>;
  isDrawerOpened = signal<boolean>(true);

  homeMenu: MenuItemInterface = {
    permissionEnum: PermissionsEnum.HOME,
    route: "/home",
    icon: "home",
    label: "Inicio",
    subtitle: "Panel principal",
    bgColor: "bg-indigo-100",
    textColor: "text-indigo-600",
  };

  constructor() {
    if (this.localStorageService.isLoggedIn()) {
      this.accessControlService.initializePermissions();
    }
    const savedState = localStorage.getItem("sidenav_opened");
    if (savedState !== null) {
      this.isDrawerOpened.set(savedState === "true");
    }
  }

  ngOnInit() {
    if (this.userData?.roles && this.userData?.roles.length > 0) {
      this.userData.roles.forEach((role) => {
        if (role.permissionsList) {
          role.permissionsList.forEach(
            (permissionObject: PermissionInterface) => {
              this.permissions.push(permissionObject.name);
            },
          );
        }
      });
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
              this.permissions.includes(child.permissionEnum),
            )
          : undefined,
      }));

    return [this.homeMenu, ...filteredItems];
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
      (item) => item.children && item.children.length > 0,
    );

    const menuIndex = menusWithChildren.findIndex(
      (item) => item.label === label,
    );
    return menus[menuIndex] || null;
  }

  toggleDrawer() {
    this.isDrawerOpened.update((opened) => !opened);
    localStorage.setItem("sidenav_opened", String(this.isDrawerOpened()));
  }
}
