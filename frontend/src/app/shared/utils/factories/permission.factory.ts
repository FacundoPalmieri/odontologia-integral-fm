import { PermissionsEnum } from "../enums/permissions.enum";
import { MenuItemInterface } from "../../interfaces/menu-item.interface";

export class PermissionFactory {
  static createPermissions(): MenuItemInterface[] {
    return [
      // {
      //   permissionEnum: PermissionsEnum.DASHBOARD,
      //   route: "/dashboard",
      //   icon: "chart-bar",
      //   label: "Dashboard",
      // },
      // {
      //   permissionEnum: PermissionsEnum.CONSULTATION,
      //   route: "/consultation",
      //   icon: "folder-plus",
      //   label: "Registro de Consultas",
      // },
      {
        permissionEnum: PermissionsEnum.APPOINTMENT_MANAGEMENT,
        route: "/appointments",
        icon: "calendar-plus",
        label: "Gestion de Turnos",
      },
      {
        permissionEnum: PermissionsEnum.PATIENTS,
        route: "/patients",
        icon: "friends",
        label: "Pacientes",
      },
      {
        permissionEnum: PermissionsEnum.SUPPLIES,
        route: "/inventory",
        icon: "packages",
        label: "Insumos",
      },
      // {
      //   permissionEnum: PermissionsEnum.FINANCE,
      //   route: "/finances",
      //   icon: "file-dollar",
      //   label: "Finanzas",
      // },
      // {
      //   permissionEnum: PermissionsEnum.REPORTS,
      //   route: "/reports",
      //   icon: "chart-histogram",
      //   label: "Reportes",
      // },
      {
        permissionEnum: PermissionsEnum.CONFIGURATION,
        route: "/configuration",
        icon: "settings",
        label: "Configuración",
        children: [
          {
            permissionEnum: PermissionsEnum.CONFIGURATION,
            route: "/configuration/users",
            icon: "user",
            label: "Usuarios",
          },
          {
            permissionEnum: PermissionsEnum.CONFIGURATION,
            route: "/configuration/roles",
            icon: "user-shield",
            label: "Roles",
          },
          {
            permissionEnum: PermissionsEnum.CONFIGURATION,
            route: "/configuration/holidays",
            icon: "calendar-cancel",
            label: "Feriados",
          },
        ],
      },
      {
        permissionEnum: PermissionsEnum.SYSTEM,
        route: "/system",
        icon: "device-desktop-cog",
        label: "Sistema",
        children: [
          {
            permissionEnum: PermissionsEnum.SYSTEM,
            route: "/system/parameters",
            icon: "adjustments-horizontal",
            label: "Parámetros",
          },
          {
            permissionEnum: PermissionsEnum.SYSTEM,
            route: "/system/schedules",
            icon: "clock-cog",
            label: "Tareas programadas",
          },
        ],
      },
    ];
  }
}
