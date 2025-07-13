import {
  Component,
  effect,
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
import { IconsModule } from "../../../../utils/tabler-icons.module";
import { PageToolbarComponent } from "../../../components/page-toolbar/page-toolbar.component";
import { PatientService } from "../../../../services/patient.service";
import { Router, RouterModule } from "@angular/router";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../../../../domain/interfaces/api-response.interface";
import { PatientDtoInterface } from "../../../../domain/dto/patient.dto";
import { PersonDataService } from "../../../../services/person-data.service";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { LoaderService } from "../../../../services/loader.service";

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
  ],
})
export class PatientsListComponent implements OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly router = inject(Router);
  private readonly patientService = inject(PatientService);
  private readonly personDataService = inject(PersonDataService);
  private readonly loaderService = inject(LoaderService);
  dialog = inject(MatDialog);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  patients = signal<PatientDtoInterface[]>([]);
  patientsFilter = new FormControl("");
  patientsDataSource = new MatTableDataSource<PatientDtoInterface>([]);
  displayedColumns: string[] = [
    "avatar",
    "person.firstName",
    "person.lastName",
    "person.contactEmails",
    "person.dni",
    "action",
  ];

  currentPage = 0;
  pageSize = 10;
  sortBy = "person.lastName";
  sortDirection = "asc";

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

  openOdontogram(patient: PatientDtoInterface) {
    this.router.navigate([
      `patients/${patient.person.id}/odontogram/${patient.person.id}}`,
    ]);
  }

  viewFile(patient: PatientDtoInterface): void {
    this.router.navigate(["/patients/edit", patient.person.id], {
      state: { patient },
    });
  }

  private _loadInitialData() {
    this._loadData();
    this._setupFilters();
  }

  private _loadData() {
    this.loaderService.show();
    this.patientService
      .getAll(this.currentPage, this.pageSize, this.sortBy, this.sortDirection)
      .pipe(takeUntil(this._destroy$))
      .subscribe(
        (
          response: ApiResponseInterface<
            PagedDataInterface<PatientDtoInterface[]>
          >
        ) => {
          const patients = response.data.content;
          this.patients.set(patients);

          if (this.paginator) {
            this.paginator.length = response.data.totalElements;
          }

          patients.forEach((patient) => {
            if (patient.person?.id) {
              this.personDataService
                .getAvatar(patient.person.id)
                .subscribe((avatar: any) => {
                  patient.avatarUrl = avatar;
                  this.patients.set([...this.patients()]);
                });
            }
          });

          this.patientsDataSource.data = patients;
          if (!this.patientsDataSource.sort) {
            this.patientsDataSource.sort = this.sort;
          }
          this.loaderService.hide();
        }
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
