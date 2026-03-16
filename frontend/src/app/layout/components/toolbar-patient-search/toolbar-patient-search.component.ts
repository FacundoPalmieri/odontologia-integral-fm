import {
  Component,
  computed,
  effect,
  inject,
  OnInit,
  signal,
} from "@angular/core";
import { FormsModule } from "@angular/forms";
import { Router } from "@angular/router";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { MatOptionModule } from "@angular/material/core";
import { IconsModule } from "../../../core/modules/tabler-icons.module";
import { PatientListStore } from "../../../features/patients/data/store/patient-list.store";
import { PatientDto } from "../../../features/patients/data/dtos/patient.dto";
import { ChangeDetectionStrategy } from "@angular/core";

@Component({
  selector: "app-toolbar-patient-search",
  templateUrl: "./toolbar-patient-search.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, MatAutocompleteModule, MatOptionModule, IconsModule],
})
export class ToolbarPatientSearchComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly patientListStore = inject(PatientListStore);

  searchQuery = signal<string>("");

  filteredPatients = computed<PatientDto[]>(() => {
    const query = this.searchQuery().toLowerCase().trim();
    if (query.length < 3) return [];
    return this.patientListStore
      .patients()
      .filter((p) => {
        const firstName = p.person?.firstName?.toLowerCase() ?? "";
        const lastName = p.person?.lastName?.toLowerCase() ?? "";
        const dni = p.person?.dni?.toLowerCase() ?? "";
        return (
          firstName.includes(query) ||
          lastName.includes(query) ||
          dni.includes(query)
        );
      })
      .slice(0, 15);
  });

  constructor() {
    effect(() => {
      const query = this.searchQuery();
      if (query.length >= 3 && !this.patientListStore.hasPatients()) {
        this.patientListStore.loadPatients({
          page: 0,
          size: 500,
          sortBy: "person.lastName",
          direction: "asc",
        });
      }
    });
  }

  ngOnInit(): void {}

  selectPatient(patient: PatientDto): void {
    this.searchQuery.set("");
    this.router.navigate(["/patients/edit", patient.person.id], {
      state: { patient },
    });
  }

  getPatientFullName(patient: PatientDto | null): string {
    if (!patient) return "";
    return `${patient.person?.firstName ?? ""} ${patient.person?.lastName ?? ""}`.trim();
  }
}
