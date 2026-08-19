import {
  Component,
  effect,
  inject,
  OnDestroy,
  signal,
  computed,
  ViewChild,
} from "@angular/core";

import { MatToolbarModule } from "@angular/material/toolbar";
import { MatFormFieldModule } from "@angular/material/form-field";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatCardModule } from "@angular/material/card";
import { MatButtonModule } from "@angular/material/button";
import { MatTableDataSource, MatTableModule } from "@angular/material/table";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { Subject, takeUntil } from "rxjs";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { PageToolbarComponent } from "../../../../../shared/components/page-toolbar/page-toolbar.component";
import { ApiResponseInterface } from "../../../../../shared/interfaces/api-response.interface";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";
import { ParametersEditDialogComponent } from "../../components/parameter-edit-dialog/parameter-edit-dialog.component";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { SystemParameterUpdateDto } from "../../../data/dtos/system-parameter.dto";
import { SystemParameterInterface } from "../../../data/interfaces/system-parameter.interface";
import { SystemParameterService } from "../../../services/system-parameter.service";
import { EmptyStateComponent } from "../../../../../shared/components/empty-state/empty-state.component";

@Component({
  selector: "app-parameters-list",
  templateUrl: "./parameters-list.component.html",
  standalone: true,
  imports: [
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
    EmptyStateComponent
],
})
export class ParametersListComponent implements OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly dialog = inject(MatDialog);
  private readonly systemParameterService = inject(SystemParameterService);
  private readonly snackbarService = inject(SnackbarService);

  systemParameters = signal<SystemParameterInterface[]>([]);
  systemParametersFilter = new FormControl("");
  systemParametersDataSource: MatTableDataSource<SystemParameterInterface> =
    new MatTableDataSource();
  systemParametersDisplayedColumns: string[] = [
    "id",
    "description",
    "value",
    "action",
  ];

  @ViewChild("systemParametersPaginator") set paginator(
    paginator: MatPaginator,
  ) {
    this.systemParametersDataSource.paginator = paginator;
  }
  @ViewChild("systemParametersSort") set sort(sort: MatSort) {
    this.systemParametersDataSource.sort = sort;
  }

  get isTableEmpty(): boolean {
    return (
      !this.systemParameters() ||
      this.systemParametersDataSource.filteredData.length === 0
    );
  }

  constructor() {
    this.loadInitialData();

    effect(() => {
      if (this.systemParameters()) {
        this.systemParametersDataSource.data = this.systemParameters();
      }
    });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  loadInitialData() {
    this._getSystemParameters();
    this._setupFilters();
  }

  editSystemParameter(systemParameter: SystemParameterInterface) {
    if (systemParameter != null) {
      const dialogRef = this.dialog.open(ParametersEditDialogComponent, {
        data: { systemParameter: systemParameter },
      });
      dialogRef
        .afterClosed()
        .subscribe((systemParameterUpdateDto: SystemParameterUpdateDto) => {
          if (systemParameterUpdateDto) {
            this.systemParameterService
              .update(systemParameterUpdateDto)
              .pipe(takeUntil(this._destroy$))
              .subscribe((response: ApiResponseInterface<string>) => {
                this.snackbarService.openSnackbar(
                  response.message,
                  3000,
                  "center",
                  "top",
                  SnackbarTypeEnum.Success,
                );
                this._getSystemParameters();
              });
          }
        });
    }
  }

  private _getSystemParameters() {
    this.systemParameterService
      .getAll()
      .pipe(takeUntil(this._destroy$))
      .subscribe(
        (response: ApiResponseInterface<SystemParameterInterface[]>) => {
          this.systemParameters.set(response.data);
        },
      );
  }

  private _setupFilters() {
    this.systemParametersFilter.valueChanges.subscribe((filterValue) => {
      this.systemParametersDataSource.filter = filterValue
        ?.trim()
        .toLowerCase()!;

      if (this.systemParametersDataSource.paginator) {
        this.systemParametersDataSource.paginator.firstPage();
      }
    });
  }
}
