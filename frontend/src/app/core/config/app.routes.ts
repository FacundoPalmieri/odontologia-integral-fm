import { Routes } from "@angular/router";
import { LayoutComponent } from "../../layout/layout.component";
import { AuthGuard } from "../guards/auth.guard";
import { LoginGuard } from "../guards/login.guard";

export const routes: Routes = [
  {
    path: "",
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: "",
        redirectTo: "home",
        pathMatch: "full",
      },
      {
        path: "home",
        loadComponent: () =>
          import("../../features/home/presentation/pages/home/home.component").then(
            (m) => m.HomeComponent,
          ),
      },
      {
        path: "dashboard",
        loadComponent: () =>
          import("../../features/dashboard/presentation/pages/dashboard/dashboard.component").then(
            (m) => m.DashboardComponent,
          ),
      },
      {
        path: "appointments",
        loadComponent: () =>
          import("../../features/appointments/presentation/pages/appointments/appointments.component").then(
            (m) => m.AppointmentsComponent,
          ),
      },
      {
        path: "patients",
        loadChildren: () =>
          import("../../features/patients/patients.routes").then(
            (m) => m.PATIENT_ROUTES,
          ),
      },
      {
        path: "inventory",
        loadComponent: () =>
          import("../../features/inventory/presentation/pages/inventory/inventory.component").then(
            (m) => m.InventoryComponent,
          ),
      },
      {
        path: "finances",
        loadComponent: () =>
          import("../../features/finance/presentation/pages/finance/finance.component").then(
            (m) => m.FinanceComponent,
          ),
      },
      {
        path: "reports",
        loadComponent: () =>
          import("../../features/reports/presentation/pages/reports/reports.component").then(
            (m) => m.ReportsComponent,
          ),
      },
      {
        path: "configuration/users",
        loadChildren: () =>
          import("../../features/users/users.routes").then(
            (m) => m.USER_ROUTES,
          ),
      },
      {
        path: "configuration/roles",
        loadComponent: () =>
          import("../../features/roles/presentation/pages/roles-list/roles-list.component").then(
            (m) => m.RolesListComponent,
          ),
      },
      {
        path: "configuration/holidays",
        loadComponent: () =>
          import("../../features/holidays/presentation/pages/holidays/holidays.component").then(
            (m) => m.HolidaysListComponent,
          ),
      },
      {
        path: "system/parameters",
        loadComponent: () =>
          import("../../features/system-parameters/presentation/pages/parameters-list/parameters-list.component").then(
            (m) => m.ParametersListComponent,
          ),
      },
      {
        path: "system/schedules",
        loadComponent: () =>
          import("../../features/system-schedule/presentation/pages/system-schedules-list/system-schedules-list.component").then(
            (m) => m.SystemSchedulesListComponent,
          ),
      },
      {
        path: "profile",
        loadComponent: () =>
          import("../../features/users/presentation/pages/user-profile/user-profile.component").then(
            (m) => m.UserProfileComponent,
          ),
      },
      {
        path: "calendar",
        loadComponent: () =>
          import("../../features/calendar/presentation/pages/calendar/calendar.component").then(
            (m) => m.CalendarComponent,
          ),
      },
      {
        path: "dentist-availability/:id",
        loadComponent: () =>
          import("../../features/dentist-availability/presentation/pages/dentist-availability/dentist-availability.component").then(
            (m) => m.DentistAvailabilityComponent,
          ),
      },
    ],
  },
  {
    path: "login",
    loadComponent: () =>
      import("../../features/auth/presentation/pages/login/login.component").then(
        (m) => m.LoginComponent,
      ),
    canActivate: [LoginGuard],
  },
  {
    path: "reset-password",
    loadComponent: () =>
      import("../../features/auth/presentation/pages/password-recovery/password-recovery.component").then(
        (m) => m.PasswordRecoveryComponent,
      ),
  },
  {
    path: "**",
    redirectTo: "home",
  },
];
