import { Routes } from "@angular/router";
import { LayoutComponent } from "../../layout/layout.component";
import { AuthGuard } from "../guards/auth.guard";
import { HomeComponent } from "../../features/home/presentation/pages/home/home.component";
import { DashboardComponent } from "../../features/dashboard/presentation/pages/dashboard/dashboard.component";
import { AppointmentsComponent } from "../../features/appointments/presentation/pages/appointments/appointments.component";
import { InventoryComponent } from "../../features/inventory/presentation/pages/inventory/inventory.component";
import { FinanceComponent } from "../../features/finance/presentation/pages/finance/finance.component";
import { ReportsComponent } from "../../features/reports/presentation/pages/reports/reports.component";
import { UsersListComponent } from "../../features/users/presentation/pages/users-list/users-list.component";
import { UserCreatePageComponent } from "../../features/users/presentation/pages/user-create-page/user-create-page.component";
import { UserEditPageComponent } from "../../features/users/presentation/pages/user-edit-page/user-edit-page.component";
import { RolesListComponent } from "../../features/roles/presentation/pages/roles-list/roles-list.component";
import { HolidaysListComponent } from "../../features/holidays/presentation/pages/holidays/holidays.component";
import { UserProfileComponent } from "../../features/users/presentation/pages/user-profile/user-profile.component";
import { DentistAvailabilityComponent } from "../../features/dentist-availability/presentation/pages/dentist-availability/dentist-availability.component";
import { LoginComponent } from "../../features/auth/presentation/pages/login/login.component";
import { LoginGuard } from "../guards/login.guard";
import { PasswordRecoveryComponent } from "../../features/auth/presentation/pages/password-recovery/password-recovery.component";
import { PatientCreatePageComponent } from "../../features/patients/presentation/pages/patient-create-page/patient-create-page.component";
import { PatientsListComponent } from "../../features/patients/presentation/pages/patients-list/patients-list.component";
import { PatientEditPageComponent } from "../../features/patients/presentation/pages/patient-edit-page/patient-edit-page.component";
import { OdontogramEditComponent } from "../../features/patients/presentation/pages/odontogram-edit/odontogram-edit.component";
import { ParametersListComponent } from "../../features/system-parameters/presentation/pages/parameters-list/parameters-list.component";
import { SystemSchedulesListComponent } from "../../features/system-schedule/presentation/pages/system-schedules-list/system-schedules-list.component";
import { CalendarComponent } from "../../features/calendar/presentation/pages/calendar/calendar.component";

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
        component: HomeComponent,
      },
      {
        path: "dashboard",
        component: DashboardComponent,
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
        path: "configuration/users",
        component: UsersListComponent,
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
        path: "configuration/roles",
        component: RolesListComponent,
      },
      {
        path: "configuration/holidays",
        component: HolidaysListComponent,
      },
      {
        path: "system/parameters",
        component: ParametersListComponent,
      },
      {
        path: "system/schedules",
        component: SystemSchedulesListComponent,
      },
      {
        path: "profile",
        component: UserProfileComponent,
      },
      {
        path: "calendar",
        component: CalendarComponent,
      },
      {
        path: "dentist-availability/:id",
        component: DentistAvailabilityComponent,
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
