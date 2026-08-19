import {
  Component,
  inject,
  effect,
  DestroyRef,
  AfterViewInit,
  ViewChild,
  OnInit,
  signal,
  ChangeDetectionStrategy,
} from "@angular/core";

import { MatCardModule } from "@angular/material/card";
import { MatChipsModule } from "@angular/material/chips";
import { MatFormFieldModule } from "@angular/material/form-field";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatButtonToggleModule } from "@angular/material/button-toggle";
import { MatTooltipModule } from "@angular/material/tooltip";
import { Router } from "@angular/router";
import { takeUntilDestroyed, toSignal } from "@angular/core/rxjs-interop";
import { computed } from "@angular/core";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../../../../shared/components/page-toolbar/page-toolbar.component";
import {
  ActionsEnum,
  PermissionsEnum,
} from "../../../../../shared/utils/enums/permissions.enum";
import { AccessControlService } from "../../../../../core/services/access-control.service";
import { PatientDto } from "../../../data/dtos/patient.dto";
import { PatientListStore } from "../../../data/store/patient-list.store";
import { PatientsTableComponent } from "../../components/patients-table/patients-table.component";
import { PatientsCardsComponent } from "../../components/patients-cards/patients-cards.component";
import { MatSort, MatSortModule } from "@angular/material/sort";

@Component({
  selector: "app-patients-list",
  templateUrl: "./patients-list.component.html",
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonToggleModule,
    MatTooltipModule,
    MatSortModule,
    PatientsTableComponent,
    PatientsCardsComponent
],
})
export class PatientsListComponent implements OnInit, AfterViewInit {
  private readonly router = inject(Router);
  private readonly accessControlService = inject(AccessControlService);
  private readonly destroyRef = inject(DestroyRef);

  readonly store = inject(PatientListStore);

  readonly patientsFilter = new FormControl("");
  readonly filterValue = toSignal(this.patientsFilter.valueChanges, {
    initialValue: "",
  });

  readonly filteredPatients = computed(() => {
    const filter = (this.filterValue() ?? "").trim().toLowerCase();
    const allPatients = this.store.patients();

    if (!filter) return allPatients;

    return allPatients.filter((p) => {
      const term =
        `${p.person.firstName} ${p.person.lastName} ${p.person.dni} ${p.person.contactEmails?.[0] || ""} ${p.person.contactPhone?.[0]?.phone || ""}`.toLowerCase();
      return term.includes(filter);
    });
  });

  readonly tableSkeletonRows: PatientDto[] = Array(5).fill({}) as PatientDto[];
  readonly cardsSkeletonRows: PatientDto[] = Array(4).fill({}) as PatientDto[];
  readonly viewMode = signal<"table" | "cards">(
    (localStorage.getItem("patientsViewMode") as "table" | "cards") || "table",
  );

  @ViewChild(MatSort) patientsSort!: MatSort;

  canCreate = false;
  canRead = false;

  constructor() {
    effect(() => {
      localStorage.setItem("patientsViewMode", this.viewMode());
    });
  }

  ngOnInit(): void {
    this.canCreate = this.accessControlService.can(
      PermissionsEnum.PATIENTS,
      ActionsEnum.CREATE,
    );
    this.canRead = this.accessControlService.can(
      PermissionsEnum.PATIENTS,
      ActionsEnum.READ,
    );

    if (!this.canRead) return;

    this._loadPatients();
  }

  ngAfterViewInit(): void {
    if (!this.patientsSort) return;

    this.patientsSort.sortChange
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((sort) => {
        this.store.updateSort(sort.active, sort.direction);
        this._loadPatients();
      });
  }

  createPatient(): void {
    this.router.navigate(["/patients/create"]);
  }

  viewProfile(patient: PatientDto): void {
    this.router.navigate(["/patients/edit", patient.person.id], {
      state: { patient },
    });
  }

  openConsultation(patient: PatientDto): void {
    this.router.navigate([`patients/${patient.person.id}/consultation`]);
  }

  private _loadPatients(): void {
    this.store.loadPatients({
      page: this.store.pageIndex(),
      size: this.store.pageSize(),
      sortBy: this.store.sortBy(),
      direction: this.store.sortDirection() as "asc" | "desc",
    });
  }
}
