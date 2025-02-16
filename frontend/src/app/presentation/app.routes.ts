import { Routes } from "@angular/router";
import { LoginComponent } from "./pages/login/login.component";
import { ContentComponent } from "./components/content/content.component";

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
];
