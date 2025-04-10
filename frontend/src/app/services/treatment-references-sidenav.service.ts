import { Injectable, signal } from "@angular/core";

@Injectable({
  providedIn: "root",
})
export class TreatmentReferencesSidenavService {
  sidenavOpen = signal(false);

  toggleSidenav(): void {
    this.sidenavOpen.set(!this.sidenavOpen());
  }
}
