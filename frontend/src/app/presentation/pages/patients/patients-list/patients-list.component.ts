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
import { CreatePatientDialogComponent } from "../../../components/create-patient-dialog/create-patient-dialog.component";
import { Router, RouterModule } from "@angular/router";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../../../../domain/interfaces/api-response.interface";
import { PatientDtoInterface } from "../../../../domain/dto/patient.dto";
import { PersonDataService } from "../../../../services/person-data.service";

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
  ],
})
export class PatientsListComponent implements OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  patientService = inject(PatientService);
  personDataService = inject(PersonDataService);
  dialog = inject(MatDialog);
  router = inject(Router);
  patients = signal<PatientDtoInterface[]>([]);

  patientsFilter = new FormControl("");
  patientsDataSource: MatTableDataSource<PatientDtoInterface> =
    new MatTableDataSource();
  displayedColumns: string[] = [
    "avatar",
    "firstName",
    "lastName",
    "dni",
    "email",
    "phone",
    "action",
  ];

  @ViewChild(MatPaginator)
  paginator!: MatPaginator;
  @ViewChild(MatSort)
  sort!: MatSort;

  constructor() {
    this._loadInitialData();

    effect(() => {
      if (this.patients()) {
        this.patientsDataSource.data = this.patients();
        this.patientsDataSource.paginator = this.paginator;
        this.patientsDataSource.sort = this.sort;
      }
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
    this.patientService
      .getAll()
      .pipe(takeUntil(this._destroy$))
      .subscribe(
        (
          response: ApiResponseInterface<
            PagedDataInterface<PatientDtoInterface[]>
          >
        ) => {
          const patients = response.data.content;
          this.patients.set(patients);
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
          this.patientsDataSource.paginator = this.paginator;
          this.patientsDataSource.sort = this.sort;
        }
      );

    this._setupFilters();
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
