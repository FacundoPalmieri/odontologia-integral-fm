import {
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
import { MatFormFieldModule } from "@angular/material/form-field";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatCardModule } from "@angular/material/card";
import { ConfigService } from "../../../services/config.service";
import { ApiResponseInterface } from "../../../domain/interfaces/api-response.interface";
import { MatButtonModule } from "@angular/material/button";
import { MatTableDataSource, MatTableModule } from "@angular/material/table";
import { SnackbarService } from "../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../utils/enums/snackbar-type.enum";
import { MessageInterface } from "../../../domain/interfaces/message.interface";
import { MatSort, MatSortModule } from "@angular/material/sort";
import { MatPaginator, MatPaginatorModule } from "@angular/material/paginator";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { EditMessageDialogComponent } from "./edit-message-dialog/edit-message-dialog.component";
import { Subject, takeUntil } from "rxjs";
import { MessageCreateDtoInterface } from "../../../domain/dto/message.dto";
import { ScheduleInterface } from "../../../domain/interfaces/schedule.interface";
import { ScheduleUpdateDtoInterface } from "../../../domain/dto/schedule.dto";
import { EditScheduleDialogComponent } from "./edit-schedule-dialog/edit-schedule-dialog.component";

@Component({
  selector: "app-system",
  templateUrl: "./system.component.html",
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
export class SystemComponent implements OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  readonly dialog = inject(MatDialog);
  configService = inject(ConfigService);
  snackbarService = inject(SnackbarService);
  tokenForm: FormGroup;
  displayedColumns: string[] = ["id", "key", "value", "locale", "action"];
  messages = signal<MessageInterface[]>([]);
  messagesFilter = new FormControl("");
  messagesDataSource: MatTableDataSource<MessageInterface> =
    new MatTableDataSource();

  schedules = signal<any[]>([]);
  schedulesFilter = new FormControl("");
  schedulesDataSource: MatTableDataSource<any> = new MatTableDataSource();

  @ViewChild("messagesPaginator") messagesPaginator!: MatPaginator;
  @ViewChild("messagesSort") messagesSort!: MatSort;

  @ViewChild("schedulePaginator") schedulePaginator!: MatPaginator;
  @ViewChild("scheduleSort") scheduleSort!: MatSort;

  constructor() {
    this.tokenForm = new FormGroup({
      jwtExpiration: new FormControl<number>(0, [Validators.required]),
      attempts: new FormControl<number>(0, [Validators.required]),
      refreshTokenExpiration: new FormControl<number>(0, [Validators.required]),
    });
    this.loadInitialData();

    effect(() => {
      if (this.messages()) {
        this.messagesDataSource.data = this.messages();
        this.messagesDataSource.paginator = this.messagesPaginator;
        this.messagesDataSource.sort = this.messagesSort;
      }
    });

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
    this._getFailedAttempts();
    this._getMessages();
    this._getTokenExpirationTime();
    this._getRefreshTokenExpirationTime();
    this._getSchedules();
    this._setupFilters();
  }

  updateTokenExpirationTime() {
    const time = this.tokenForm.value.jwtExpiration;

    this.configService
      .updateTokenExpirationTime(time)
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<number>) => {
        this.snackbarService.openSnackbar(
          response.message,
          3000,
          "center",
          "top",
          SnackbarTypeEnum.Success
        );
        this._getTokenExpirationTime();
      });
  }

  updateFailedAttempts() {
    const attempts = this.tokenForm.value.attempts;

    this.configService
      .updateFailedAttempts(attempts)
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<number>) => {
        this.snackbarService.openSnackbar(
          response.message,
          3000,
          "center",
          "top",
          SnackbarTypeEnum.Success
        );
        this._getFailedAttempts();
      });
  }

  updateRefreshTokenExpirationTime() {
    const refreshTokenExpiration = this.tokenForm.value.refreshTokenExpiration;

    this.configService
      .updateRefreshTokenExpirationTime(refreshTokenExpiration)
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<number>) => {
        this.snackbarService.openSnackbar(
          response.message,
          3000,
          "center",
          "top",
          SnackbarTypeEnum.Success
        );
        this._getRefreshTokenExpirationTime();
      });
  }

  editMessage(message: MessageInterface) {
    if (message != null) {
      const dialogRef = this.dialog.open(EditMessageDialogComponent, {
        data: { message: message },
      });
      dialogRef.afterClosed().subscribe((message: MessageInterface) => {
        if (message) {
          const messageDto: MessageCreateDtoInterface = {
            id: message.id,
            key: message.key,
            locale: message.locale,
            value: message.value,
          };
          this.configService
            .updateMessage(messageDto)
            .pipe(takeUntil(this._destroy$))
            .subscribe((response: ApiResponseInterface<MessageInterface>) => {
              this.snackbarService.openSnackbar(
                response.message,
                3000,
                "center",
                "top",
                SnackbarTypeEnum.Success
              );
              this._getMessages();
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

  editSchedule(schedule: ScheduleInterface) {
    if (schedule != null) {
      const dialogRef = this.dialog.open(EditScheduleDialogComponent, {
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

  private _getTokenExpirationTime() {
    this.configService
      .getTokenExpirationTime()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<number>) => {
        if (response.success) {
          this.tokenForm.patchValue({
            jwtExpiration: response.data,
          });
        }
      });
  }

  private _getRefreshTokenExpirationTime() {
    this.configService
      .getRefreshTokenExpirationTime()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<number>) => {
        if (response.success) {
          this.tokenForm.patchValue({
            refreshTokenExpiration: response.data,
          });
        }
      });
  }

  private _getFailedAttempts() {
    this.configService
      .getFailedAttempts()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<number>) => {
        if (response.success) {
          this.tokenForm.patchValue({
            attempts: response.data,
          });
        }
      });
  }

  private _getSchedules() {
    this.configService
      .getSchedules()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<ScheduleInterface[]>) => {
        this.schedules.set(response.data);
      });
  }

  private _getMessages() {
    this.configService
      .getMessages()
      .pipe(takeUntil(this._destroy$))
      .subscribe((response: ApiResponseInterface<MessageInterface[]>) => {
        this.messages.set(response.data);
      });
  }

  private _setupFilters() {
    this.messagesFilter.valueChanges.subscribe((filterValue) => {
      this.messagesDataSource.filter = filterValue?.trim().toLowerCase()!;

      if (this.messagesDataSource.paginator) {
        this.messagesDataSource.paginator.firstPage();
      }
    });

    this.schedulesFilter.valueChanges.subscribe((filterValue) => {
      this.schedulesDataSource.filter = filterValue?.trim().toLowerCase()!;

      if (this.schedulesDataSource.paginator) {
        this.schedulesDataSource.paginator.firstPage();
      }
    });
  }
}
