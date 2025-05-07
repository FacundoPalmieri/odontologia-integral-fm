import {
  AfterViewInit,
  Component,
  effect,
  inject,
  OnDestroy,
  signal,
  ViewChild,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../components/page-toolbar/page-toolbar.component";
import { PatientInterface } from "../../../domain/interfaces/patient.interface";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatCardModule } from "@angular/material/card";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatTableDataSource, MatTableModule } from "@angular/material/table";
import { PatientService } from "../../../services/patient.service";
import { MatButtonModule } from "@angular/material/button";
import { MatTooltipModule } from "@angular/material/tooltip";
import { Subject, takeUntil } from "rxjs";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { CreatePatientDialogComponent } from "../../components/patient-create-dialog/create-patient-dialog.component";

@Component({
  selector: "app-patients",
  templateUrl: "./patients.component.html",
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
  ],
})
export class PatientsComponent implements OnDestroy, AfterViewInit {
  private readonly _destroy$ = new Subject<void>();
  patientService = inject(PatientService);
  dialog = inject(MatDialog);
  patients = signal<PatientInterface[]>([]);

  patientsFilter = new FormControl("");
  patientsDataSource: MatTableDataSource<PatientInterface> =
    new MatTableDataSource();
  displayedColumns: string[] = [
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
        // Descomentar cuando exista el método en la API para recuperar todos los pacientes
        // this.patientsDataSource.paginator = this.paginator;
        // this.patientsDataSource.sort = this.sort;
      }
    });
  }

  // Quitar cuando exista el método en la API.
  ngAfterViewInit(): void {
    this.patientsDataSource.paginator = this.paginator;
    this.patientsDataSource.sort = this.sort;
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  create() {
    const dialogRef = this.dialog.open(CreatePatientDialogComponent, {
      width: "1400px",
    });
  }

  private _loadInitialData() {
    this.patientService
      .getAll()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response) => {
        this.patients.set(response.data);
        this.patientsDataSource.paginator = this.paginator;
        this.patientsDataSource.sort = this.sort;
      });

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
