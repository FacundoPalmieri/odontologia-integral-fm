import { Routes } from "@angular/router";

export const USER_ROUTES: Routes = [
  {
    path: "",
    loadComponent: () =>
      import("./presentation/pages/users-list/users-list.component").then(
        (m) => m.UsersListComponent,
      ),
  },
  {
    path: "create",
    loadComponent: () =>
      import("./presentation/pages/user-create-page/user-create-page.component").then(
        (m) => m.UserCreatePageComponent,
      ),
  },
  {
    path: "edit/:id",
    loadComponent: () =>
      import("./presentation/pages/user-edit-page/user-edit-page.component").then(
        (m) => m.UserEditPageComponent,
      ),
  },
];
