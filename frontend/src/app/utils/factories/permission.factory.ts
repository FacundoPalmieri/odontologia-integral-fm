import { PermissionsEnum } from "../enums/permissions.enum";

interface PermissionItem {
  permissionEnum: PermissionsEnum;
  route: string;
  icon: string;
  label: string;
}

export class PermissionFactory {
  static createPermissions(): PermissionItem[] {
    return [
      {
        permissionEnum: PermissionsEnum.DASHBOARD,
        route: "/dashboard",
        icon: "chart-bar",
        label: "Dashboard",
      },
      {
        permissionEnum: PermissionsEnum.CONSULTATION_RECORD,
        route: "/consultation",
        icon: "folder-plus",
        label: "Registro de Consultas",
      },
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
      {
        permissionEnum: PermissionsEnum.FINANCE,
        route: "/finances",
        icon: "file-dollar",
        label: "Finanzas",
      },
      {
        permissionEnum: PermissionsEnum.REPORTS,
        route: "/reports",
        icon: "chart-histogram",
        label: "Reportes",
      },
      {
        permissionEnum: PermissionsEnum.CONFIGURATION,
        route: "/configuration",
        icon: "settings",
        label: "Configuración",
      },
      {
        permissionEnum: PermissionsEnum.SYSTEM,
        route: "/system",
        icon: "device-desktop-cog",
        label: "Sistema",
      },
    ];
  }
}
