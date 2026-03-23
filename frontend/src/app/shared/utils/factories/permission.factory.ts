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
        subtitle: "Administrar turnos y agenda",
        bgColor: "bg-red-100",
        textColor: "text-red-600",
      },
      {
        permissionEnum: PermissionsEnum.PATIENTS,
        route: "/patients",
        icon: "friends",
        label: "Pacientes",
        subtitle: "Directorio de pacientes",
        bgColor: "bg-blue-100",
        textColor: "text-blue-600",
      },
      // {
      //   permissionEnum: PermissionsEnum.SUPPLIES,
      //   route: "/inventory",
      //   icon: "packages",
      //   label: "Insumos",
      //   subtitle: "Gestión de inventario",
      //   bgColor: "bg-orange-100",
      //   textColor: "text-orange-600",
      // },
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
        subtitle: "Ajustes de la clínica",
        bgColor: "bg-gray-100",
        textColor: "text-gray-600",
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
        subtitle: "Parámetros del sistema",
        bgColor: "bg-teal-100",
        textColor: "text-teal-600",
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
