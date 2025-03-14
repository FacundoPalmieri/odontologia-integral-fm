import { Component, computed, inject } from "@angular/core";
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
  ],
})
export class HomeComponent {
  themeService = inject(ThemeService);
  authService = inject(AuthService);
  router = inject(Router);
  showFiller = false;
  currentTheme = computed(() => this.themeService.currentTheme());
  userData: UserDataInterface | null = this.authService.getUserData();

  menuItems = [
    { label: "Dashboard", icon: "chart-bar", route: "/dashboard" },
    {
      label: "Registro de Consultas",
      icon: "folder-plus",
      route: "/consultation",
    },
    {
      label: "Gestión de Turnos",
      icon: "calendar-plus",
      route: "/appointments",
    },
    {
      label: "Historia Clínica",
      icon: "folder-search",
      route: "/medical-record",
    },
    { label: "Insumos", icon: "packages", route: "/inventory" },
    { label: "Finanzas", icon: "file-dollar", route: "/finances" },
    { label: "Reportes", icon: "chart-histogram", route: "/reports" },
    { label: "Configuración", icon: "settings", route: "/configuration" },
    { label: "Sistema", icon: "device-desktop-cog", route: "/system" },
  ];
  constructor() {}

  logout() {
    this.authService.logout();
    this.router.navigate(["/login"]);
  }

  obtenerRole(): string | undefined {
    return this.userData?.roleAndPermission[0].split("_")[1];
  }

  toggleTheme() {
    const newTheme = this.currentTheme().id === "light" ? "dark" : "light";
    this.themeService.setTheme(newTheme);
  }
}
