import {
  Component,
  inject,
  effect,
  DestroyRef,
  AfterViewInit,
  ViewChild,
  OnInit,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatCardModule } from "@angular/material/card";
import { MatTableDataSource, MatTableModule } from "@angular/material/table";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatButtonModule } from "@angular/material/button";
import { MatChipsModule } from "@angular/material/chips";
import { MatFormFieldModule } from "@angular/material/form-field";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { Router } from "@angular/router";
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
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

@Component({
  selector: "app-patients-list",
  templateUrl: "./patients-list.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatCardModule,
    MatTableModule,
    MatTooltipModule,
    MatButtonModule,
    MatChipsModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
  ],
})
export class PatientsListComponent implements OnInit, AfterViewInit {
  private readonly router = inject(Router);
  private readonly accessControlService = inject(AccessControlService);
  private readonly destroyRef = inject(DestroyRef);

  readonly store = inject(PatientListStore);

  readonly patientsFilter = new FormControl("");
  readonly patientsDataSource = new MatTableDataSource<PatientDto>([]);
  readonly skeletonRows: PatientDto[] = Array(5).fill({}) as PatientDto[];

  get isTableEmpty(): boolean {
    return (
      !this.store.isLoading() &&
      this.patientsDataSource.filteredData.length === 0
    );
  }

  readonly displayedColumns: string[] = [
    "avatar",
    "person.firstName",
    "person.lastName",
    "person.contactEmails",
    "person.dni",
    "person.contactPhone",
  ];

  canCreate = false;
  canRead = false;

  @ViewChild(MatPaginator) set paginator(p: MatPaginator) {
    if (p) this.patientsDataSource.paginator = p;
  }

  @ViewChild(MatSort) patientsSort!: MatSort;

  constructor() {
    effect(() => {
      this.patientsDataSource.data = this.store.patients();
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
    this._setupFilterListener();
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

  openOdontogram(patient: PatientDto): void {
    this.router.navigate([
      `patients/${patient.person.id}/odontogram/${patient.person.id}`,
    ]);
  }

  private _loadPatients(): void {
    this.store.loadPatients({
      page: this.store.pageIndex(),
      size: this.store.pageSize(),
      sortBy: this.store.sortBy(),
      direction: this.store.sortDirection() as "asc" | "desc",
    });
  }

  private _setupFilterListener(): void {
    this.patientsFilter.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((value) => {
        this.patientsDataSource.filter = value?.trim().toLowerCase() ?? "";
        this.patientsDataSource.paginator?.firstPage();
      });
  }
}
