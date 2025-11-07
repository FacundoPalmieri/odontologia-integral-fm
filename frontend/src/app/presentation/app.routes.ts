import { Routes } from "@angular/router";
import { LoginComponent } from "./pages/login/login.component";
import { PasswordRecoveryComponent } from "./pages/password-recovery/password-recovery.component";
import { HomeComponent } from "./pages/home/home.component";
import { AuthGuard } from "../utils/guards/auth.guard";
import { LoginGuard } from "../utils/guards/login.guard";
import { DashboardComponent } from "./pages/dashboard/dashboard.component";
import { AppointmentsComponent } from "./pages/appointments/appointments.component";
import { FinanceComponent } from "./pages/finance/finance.component";
import { InventoryComponent } from "./pages/inventory/inventory.component";
import { ConsultationRegisterComponent } from "./pages/consultation-register/consultation-register.component";
import { ReportsComponent } from "./pages/reports/reports.component";
import { PatientsListComponent } from "./pages/patients/patients-list/patients-list.component";
import { OdontogramEditComponent } from "./pages/patients/odontogram-edit/odontogram-edit.component";
import { PatientCreatePageComponent } from "./pages/patients/patient-create-page/patient-create-page.component";
import { PatientEditPageComponent } from "./pages/patients/patient-edit-page/patient-edit-page.component";
import { UserProfileComponent } from "./pages/user-profile/user-profile.component";
import { UsersListComponent } from "./pages/configuration/user/users-list/users-list.component";
import { UserCreatePageComponent } from "./pages/configuration/user/user-create-page/user-create-page.component";
import { UserEditPageComponent } from "./pages/configuration/user/user-edit-page/user-edit-page.component";
import { HolidaysListComponent } from "./pages/configuration/holiday/holidays-list/holidays-list.component";
import { ParametersListComponent } from "./pages/system/parameters/parameters-list/parameters-list.component";
import { SchedulesListComponent } from "./pages/system/schedule/schedules-list/schedules-list.component";
import { MessagesListComponent } from "./pages/system/messages/messages-list/messages-list.component";
import { RolesListComponent } from "./pages/configuration/role/roles-list/roles-list.component";
import { CalendarComponent } from "./components/calendar/calendar.component";

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
        component: SchedulesListComponent,
      },
      {
        path: "system/messages",
        component: MessagesListComponent,
      },
      {
        path: "profile",
        component: UserProfileComponent,
      },
      {
        path: "calendar",
        component: CalendarComponent,
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
