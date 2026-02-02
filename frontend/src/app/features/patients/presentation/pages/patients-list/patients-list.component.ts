import {
  AfterViewInit,
  Component,
  inject,
  OnDestroy,
  signal,
  ViewChild,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatToolbarModule } from "@angular/material/toolbar";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatTableDataSource, MatTableModule } from "@angular/material/table";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { Subject, takeUntil } from "rxjs";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { PageToolbarComponent } from "../../../../../shared/components/page-toolbar/page-toolbar.component";
import { Router, RouterModule } from "@angular/router";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../../../../../shared/interfaces/api-response.interface";
import { PersonDataService } from "../../../../../shared/services/person-data.service";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { LoaderService } from "../../../../../core/services/loader.service";
import { MatChipsModule } from "@angular/material/chips";
import {
  ActionsEnum,
  PermissionsEnum,
} from "../../../../../shared/utils/enums/permissions.enum";
import { PatientService } from "../../../services/patient.service";
import { AccessControlService } from "../../../../../core/services/access-control.service";
import { PatientDto } from "../../../domain/dtos/patient.dto";

@Component({
  selector: "app-patients-list",
  templateUrl: "./patients-list.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatCardModule,
    MatButtonModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatTooltipModule,
    MatDialogModule,
    RouterModule,
    MatProgressSpinnerModule,
    MatChipsModule,
  ],
})
export class PatientsListComponent implements OnDestroy, AfterViewInit {
  private readonly _destroy$ = new Subject<void>();
  private readonly router = inject(Router);
  private readonly patientService = inject(PatientService);
  private readonly personDataService = inject(PersonDataService);
  private readonly loaderService = inject(LoaderService);
  private readonly accessControlService = inject(AccessControlService);
  dialog = inject(MatDialog);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  patients = signal<PatientDto[]>([]);
  patientsFilter = new FormControl("");
  patientsDataSource = new MatTableDataSource<PatientDto>([]);
  displayedColumns: string[] = [
    "avatar",
    "person.firstName",
    "person.lastName",
    "person.contactEmails",
    "person.dni",
    "action",
  ];

  currentPage = 0;
  pageSize = 10000;
  sortBy = "person.lastName";
  sortDirection = "asc";

  canRead = signal<boolean>(false);
  canCreate = signal<boolean>(false);

  constructor() {
    this._loadInitialData();
  }

  ngAfterViewInit() {
    this.paginator.page.pipe(takeUntil(this._destroy$)).subscribe(() => {
      this.currentPage = this.paginator.pageIndex;
      this.pageSize = this.paginator.pageSize;
      this._loadData();
    });

    this.sort.sortChange.pipe(takeUntil(this._destroy$)).subscribe((sort) => {
      this.sortBy = sort.active;
      this.sortDirection = sort.direction;
      this.currentPage = 0;
      if (this.paginator) {
        this.paginator.pageIndex = 0;
      }
      this._loadData();
    });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  create() {
    this.router.navigate(["/patients/create"]);
  }

  openOdontogram(patient: PatientDto) {
    this.router.navigate([
      `patients/${patient.person.id}/odontogram/${patient.person.id}}`,
    ]);
  }

  viewFile(patient: PatientDto): void {
    this.router.navigate(["/patients/edit", patient.person.id], {
      state: { patient },
    });
  }

  private _loadInitialData() {
    this._loadData();
    this._setupFilters();
    this._loadPermissionsFlags();
  }

  private _loadPermissionsFlags() {
    this.canRead.set(
      this.accessControlService.can(PermissionsEnum.PATIENTS, ActionsEnum.READ),
    );
    this.canCreate.set(
      this.accessControlService.can(
        PermissionsEnum.PATIENTS,
        ActionsEnum.CREATE,
      ),
    );
  }

  private _loadData() {
    this.loaderService.show();
    this.patientService
      .getAll(this.currentPage, this.pageSize, this.sortBy, this.sortDirection)
      .pipe(takeUntil(this._destroy$))
      .subscribe(
        (response: ApiResponseInterface<PagedDataInterface<PatientDto[]>>) => {
          const patients = response.data.content;
          this.patients.set(patients);

          if (this.paginator) {
            this.paginator.length = response.data.totalElements;
          }

          patients.forEach((patient) => {
            if (patient.person?.id) {
              this.personDataService
                .getAvatar(patient.person.id)
                .subscribe((avatar: string | null) => {
                  if (avatar) {
                    patient.avatarUrl = avatar;
                  } else {
                    const gender = patient.person?.gender?.toLowerCase();
                    patient.avatarUrl =
                      gender === "femenino"
                        ? "img/women-avatar.png"
                        : "img/men-avatar.png";
                  }
                  this.patients.set([...this.patients()]);
                });
            }
          });

          this.patientsDataSource.data = patients;
          if (!this.patientsDataSource.sort) {
            this.patientsDataSource.sort = this.sort;
          }
          this.loaderService.hide();
        },
      );
  }

  private _setupFilters() {
    this.patientsFilter.valueChanges.subscribe((filterValue) => {
      this.patientsDataSource.filter = filterValue?.trim().toLowerCase()!;

      if (this.patientsDataSource.paginator) {
        this.patientsDataSource.paginator.firstPage();
      }
    });
  }
}
