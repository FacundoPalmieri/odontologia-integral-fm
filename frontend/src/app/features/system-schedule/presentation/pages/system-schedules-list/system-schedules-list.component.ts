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
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { ApiResponseInterface } from "../../../../../shared/interfaces/api-response.interface";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";
import { ScheduleEditDialogComponent } from "../../components/schedule-edit-dialog/edit-schedule-dialog.component";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { SystemScheduleService } from "../../../services/system-schedule.service";
import { SystemScheduleInterface } from "../../../domain/interfaces/system-schedule.interface";
import { SystemScheduleUpdateDto } from "../../../domain/dtos/system-schedule.dto";
import { PageToolbarComponent } from "../../../../../shared/components/page-toolbar/page-toolbar.component";

@Component({
  selector: "app-system-schedules-list",
  templateUrl: "./system-schedules-list.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
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
    PageToolbarComponent,
  ],
})
export class SystemSchedulesListComponent implements OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly dialog = inject(MatDialog);
  private readonly systemScheduleService = inject(SystemScheduleService);
  private readonly snackbarService = inject(SnackbarService);

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

  editSchedule(schedule: SystemScheduleInterface) {
    if (schedule != null) {
      const dialogRef = this.dialog.open(ScheduleEditDialogComponent, {
        data: { schedule: schedule },
      });
      dialogRef
        .afterClosed()
        .subscribe((scheduleUpdateDto: SystemScheduleUpdateDto) => {
          if (scheduleUpdateDto) {
            this.systemScheduleService
              .update(scheduleUpdateDto)
              .pipe(takeUntil(this._destroy$))
              .subscribe((response: ApiResponseInterface<string>) => {
                this.snackbarService.openSnackbar(
                  response.message,
                  3000,
                  "center",
                  "top",
                  SnackbarTypeEnum.Success,
                );
                this._getSchedules();
              });
          }
        });
    }
  }

  private _getSchedules() {
    this.systemScheduleService
      .getAll()
      .pipe(takeUntil(this._destroy$))
      .subscribe(
        (response: ApiResponseInterface<SystemScheduleInterface[]>) => {
          this.schedules.set(response.data);
        },
      );
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
