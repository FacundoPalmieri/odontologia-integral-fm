import { Routes } from "@angular/router";
import { LoginComponent } from "./pages/login/login.component";
import { PasswordRecoveryComponent } from "./pages/password-recovery/password-recovery.component";
import { HomeComponent } from "./pages/home/home.component";
import { AuthGuard } from "../utils/guards/auth.guard";
import { LoginGuard } from "../utils/guards/login.guard";
import { DashboardComponent } from "./pages/dashboard/dashboard.component";
import { ConfigurationComponent } from "./pages/configuration/configuration.component";
import { AppointmentsComponent } from "./pages/appointments/appointments.component";
import { FinanceComponent } from "./pages/finance/finance.component";
import { SystemComponent } from "./pages/system/system.component";
import { InventoryComponent } from "./pages/inventory/inventory.component";
import { ConsultationRegisterComponent } from "./pages/consultation-register/consultation-register.component";
import { ReportsComponent } from "./pages/reports/reports.component";
import { PatientsListComponent } from "./pages/patients/patients-list/patients-list.component";
import { OdontogramEditComponent } from "./pages/patients/odontogram-edit/odontogram-edit.component";
import { UserCreatePageComponent } from "./pages/configuration/user-create-page/user-create-page.component";
import { UserEditPageComponent } from "./pages/configuration/user-edit-page/user-edit-page.component";
import { PatientCreatePageComponent } from "./pages/patients/patient-create-page/patient-create-page.component";
import { PatientEditPageComponent } from "./pages/patients/patient-edit-page/patient-edit-page.component";

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
        path: "consultation",
        component: ConsultationRegisterComponent,
      },
      {
        path: "appointments",
        component: AppointmentsComponent,
      },
      {
        path: "patients",
        component: PatientsListComponent,
      },
      {
        path: "patients/create",
        component: PatientCreatePageComponent,
      },
      {
        path: "patients/edit/:id",
        component: PatientEditPageComponent,
      },
      {
        path: "patients/:id/odontogram/:id",
        component: OdontogramEditComponent,
      },
      {
        path: "inventory",
        component: InventoryComponent,
      },
      {
        path: "finances",
        component: FinanceComponent,
      },
      {
        path: "reports",
        component: ReportsComponent,
      },
      {
        path: "configuration",
        component: ConfigurationComponent,
      },
      {
        path: "configuration/users/create",
        component: UserCreatePageComponent,
      },
      {
        path: "configuration/users/edit/:id",
        component: UserEditPageComponent,
      },
      {
        path: "users/:id",
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
