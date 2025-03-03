import { Routes } from "@angular/router";
import { LoginComponent } from "./pages/login/login.component";
import { PasswordRecoveryComponent } from "./pages/password-recovery/password-recovery.component";
import { HomeComponent } from "./pages/home/home.component";
import { AuthGuard } from "../utils/guards/auth.guard";
import { LoginGuard } from "../utils/guards/login.guard";
import { DashboardComponent } from "./pages/dashboard/dashboard.component";
import { ConfigurationComponent } from "./pages/configuration/configuration.component";
import { PatientsComponent } from "./pages/patients/patients.component";
import { AppointmentsComponent } from "./pages/appointments/appointments.component";
import { FinanceComponent } from "./pages/finance/finance.component";
import { SystemComponent } from "./pages/system/system.component";
import { InventoryComponent } from "./pages/inventory/inventory.component";

export const routes: Routes = [
  {
    path: "",
    component: HomeComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: "dashboard",
        component: DashboardComponent,
      },
      {
        path: "patients",
        component: PatientsComponent,
      },
      {
        path: "finances",
        component: FinanceComponent,
      },
      {
        path: "inventory",
        component: InventoryComponent,
      },
      {
        path: "appointments",
        component: AppointmentsComponent,
      },
      {
        path: "configuration",
        component: ConfigurationComponent,
      },
      {
        path: "system",
        component: SystemComponent,
      },
    ],
  },
  {
    path: "login",
    component: LoginComponent,
    canActivate: [LoginGuard],
  },
  {
    path: "reset-password",
    component: PasswordRecoveryComponent,
  },
];
