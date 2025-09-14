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
import { IconsModule } from "../../../../../utils/tabler-icons.module";
import { PageToolbarComponent } from "../../../../components/page-toolbar/page-toolbar.component";
import { ConfigService } from "../../../../../services/config.service";
import { SnackbarService } from "../../../../../services/snackbar.service";
import { ScheduleInterface } from "../../../../../domain/interfaces/config.interface";
import { ScheduleUpdateDtoInterface } from "../../../../../domain/dto/config.dto";
import { ApiResponseInterface } from "../../../../../domain/interfaces/api-response.interface";
import { SnackbarTypeEnum } from "../../../../../utils/enums/snackbar-type.enum";
import { ScheduleEditDialogComponent } from "../schedule-edit-dialog/edit-schedule-dialog.component";

@Component({
  selector: "app-schedules-list",
  templateUrl: "./schedules-list.component.html",
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
export class SchedulesListComponent implements OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  readonly dialog = inject(MatDialog);
  configService = inject(ConfigService);
  snackbarService = inject(SnackbarService);

  schedules = signal<any[]>([]);
  schedulesFilter = new FormControl("");
  schedulesDataSource: MatTableDataSource<any> = new MatTableDataSource();
  schedulesDisplayedColumns: string[] = ["id", "label", "cron", "action"];

  @ViewChild("schedulePaginator") schedulePaginator!: MatPaginator;
  @ViewChild("scheduleSort") scheduleSort!: MatSort;

  constructor() {
    this.loadInitialData();

    effect(() => {
      if (this.schedules()) {
        this.schedulesDataSource.data = this.schedules();
        this.schedulesDataSource.paginator = this.schedulePaginator;
        this.schedulesDataSource.sort = this.scheduleSort;
      }
    });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  loadInitialData() {
    this._getSchedules();
    this._setupFilters();
  }

  editSchedule(schedule: ScheduleInterface) {
    if (schedule != null) {
      const dialogRef = this.dialog.open(ScheduleEditDialogComponent, {
        data: { schedule: schedule },
      });
      dialogRef
        .afterClosed()
        .subscribe((scheduleUpdateDto: ScheduleUpdateDtoInterface) => {
          if (scheduleUpdateDto) {
            this.configService
              .updateSchedule(scheduleUpdateDto)
              .pipe(takeUntil(this._destroy$))
              .subscribe((response: ApiResponseInterface<string>) => {
                this.snackbarService.openSnackbar(
                  response.message,
                  3000,
                  "center",
                  "top",
                  SnackbarTypeEnum.Success
                );
                this._getSchedules();
              });
          }
        });
    } else
      this.snackbarService.openSnackbar(
        "Ocurrió un error el editar el elemento",
        3000,
        "center",
        "bottom",
        SnackbarTypeEnum.Error
      );
  }

  private _getSchedules() {
    this.configService
      .getSchedules()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<ScheduleInterface[]>) => {
        this.schedules.set(response.data);
      });
  }

  private _setupFilters() {
    this.schedulesFilter.valueChanges.subscribe((filterValue) => {
      this.schedulesDataSource.filter = filterValue?.trim().toLowerCase()!;

      if (this.schedulesDataSource.paginator) {
        this.schedulesDataSource.paginator.firstPage();
      }
    });
  }
}
