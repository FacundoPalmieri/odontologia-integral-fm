import { PermissionsEnum } from "../enums/permission.enum";

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
        permissionEnum: PermissionsEnum.Dashboard,
        route: "/dashboard",
        icon: "chart-bar",
        label: "Dashboard",
      },
      {
        permissionEnum: PermissionsEnum.RegistroDeConsultas,
        route: "/consultation",
        icon: "folder-plus",
        label: "Registro de Consultas",
      },
      {
        permissionEnum: PermissionsEnum.GestionDeTurnos,
        route: "/appointments",
        icon: "calendar-plus",
        label: "Gestion de Turnos",
      },
      {
        permissionEnum: PermissionsEnum.Pacientes,
        route: "/patients",
        icon: "friends",
        label: "Pacientes",
      },
      {
        permissionEnum: PermissionsEnum.Insumos,
        route: "/inventory",
        icon: "packages",
        label: "Insumos",
      },
      {
        permissionEnum: PermissionsEnum.Finanzas,
        route: "/finances",
        icon: "file-dollar",
        label: "Finanzas",
      },
      {
        permissionEnum: PermissionsEnum.Reportes,
        route: "/reports",
        icon: "chart-histogram",
        label: "Reportes",
      },
      {
        permissionEnum: PermissionsEnum.Configuracion,
        route: "/configuration",
        icon: "settings",
        label: "Configuración",
      },
      {
        permissionEnum: PermissionsEnum.Sistema,
        route: "/system",
        icon: "device-desktop-cog",
        label: "Sistema",
      },
    ];
  }
}
