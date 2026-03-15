import { Routes } from "@angular/router";

export const PATIENT_ROUTES: Routes = [
  {
    path: "",
    loadComponent: () =>
      import("./presentation/pages/patients-list/patients-list.component").then(
        (m) => m.PatientsListComponent,
      ),
  },
  {
    path: "create",
    loadComponent: () =>
      import("./presentation/pages/patient-create-page/patient-create-page.component").then(
        (m) => m.PatientCreatePageComponent,
      ),
  },
  {
    path: "edit/:id",
    loadComponent: () =>
      import("./presentation/pages/patient-edit-page/patient-edit-page.component").then(
        (m) => m.PatientEditPageComponent,
      ),
  },
  {
    path: ":id/consultation",
    loadComponent: () =>
      import("./presentation/pages/consultation-page/consultation-page.component").then(
        (m) => m.ConsultationPageComponent,
      ),
  },
];
