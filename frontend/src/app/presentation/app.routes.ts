import { Routes } from "@angular/router";
import { LoginComponent } from "./pages/login/login.component";
import { ContentComponent } from "./components/content/content.component";
import { PasswordRecoveryComponent } from "./pages/password-recovery/password-recovery.component";

export const routes: Routes = [
  {
    path: "",
    redirectTo: "login",
    pathMatch: "full",
  },
  {
    path: "login",
    component: LoginComponent,
  },
  {
    path: "password-recovery",
    component: PasswordRecoveryComponent,
  },
];
