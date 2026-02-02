import { Component, inject, signal, OnDestroy } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatCardModule } from "@angular/material/card";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatButtonModule } from "@angular/material/button";
import { MatChipsModule } from "@angular/material/chips";
import { Subject, takeUntil } from "rxjs";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { LoaderService } from "../../../../../core/services/loader.service";
import {
  ActionsEnum,
  PermissionsEnum,
} from "../../../../../shared/utils/enums/permissions.enum";
import { ApiResponseInterface } from "../../../../../shared/interfaces/api-response.interface";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";
import { HolidayService } from "../../../services/holiday.service";
import { MatDialog } from "@angular/material/dialog";
import { AccessControlService } from "../../../../../core/services/access-control.service";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { HolidayCreateDialogComponent } from "../../components/holiday-create-dialog/holiday-create-dialog.component";
import { HolidayEditDialogComponent } from "../../components/holiday-edit-dialog/holiday-edit-dialog.component";
import { HolidayInterface } from "../../../domain/interfaces/holiday.interface";
import { PageToolbarComponent } from "../../../../../shared/components/page-toolbar/page-toolbar.component";

@Component({
  selector: "app-holidays",
  templateUrl: "./holidays.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    MatCardModule,
    MatTooltipModule,
    MatButtonModule,
    MatChipsModule,
    PageToolbarComponent,
  ],
})
export class HolidaysListComponent implements OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly loaderService = inject(LoaderService);
  private readonly accessControlService = inject(AccessControlService);
  private readonly holidayService = inject(HolidayService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly dialog = inject(MatDialog);

  holidays = signal<HolidayInterface[]>([]);

  canCreate = signal<boolean>(false);
  canRead = signal<boolean>(false);
  canUpdate = signal<boolean>(false);

  collapsedMonths: { [monthName: string]: boolean } = {};

  constructor() {
    this._loadInitialData();
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  createHoliday() {
    const dialogRef = this.dialog.open(HolidayCreateDialogComponent);
    dialogRef
      .afterClosed()
      .subscribe((holiday: Omit<HolidayInterface, "id">) => {
        if (holiday) {
          this.holidayService
            .create(holiday)
            .pipe(takeUntil(this._destroy$))
            .subscribe((response: ApiResponseInterface<HolidayInterface>) => {
              this.snackbarService.openSnackbar(
                response.message,
                6000,
                "center",
                "top",
                SnackbarTypeEnum.Success,
              );
              this._loadHolidays();
            });
        }
      });
  }

  toggleMonth(monthName: string) {
    this.collapsedMonths[monthName] = !this.collapsedMonths[monthName];
  }

  isMonthCollapsed(monthName: string): boolean {
    return !!this.collapsedMonths[monthName];
  }

  editHoliday(holiday: HolidayInterface) {
    const dialogRef = this.dialog.open(HolidayEditDialogComponent, {
      data: { holiday },
    });
    dialogRef.afterClosed().subscribe((holiday: HolidayInterface) => {
      if (holiday) {
        this.holidayService
          .update(holiday)
          .pipe(takeUntil(this._destroy$))
          .subscribe((response: ApiResponseInterface<HolidayInterface>) => {
            this.snackbarService.openSnackbar(
              response.message,
              6000,
              "center",
              "top",
              SnackbarTypeEnum.Success,
            );
            this._loadHolidays();
          });
      }
    });
  }

  getHolidayTypeClass(type: string): string {
    const baseClasses = "px-2 py-1 rounded-full text-xs font-medium";

    switch (type.toLowerCase()) {
      case "inamovible":
        return `${baseClasses} bg-red-100 text-red-800`;
      case "puente":
        return `${baseClasses} bg-yellow-100 text-yellow-800`;
      case "trasladable":
        return `${baseClasses} bg-green-100 text-green-800`;
      default:
        return `${baseClasses} bg-gray-100 text-gray-800`;
    }
  }

  getHolidaysByMonth() {
    const holidays = this.holidays();
    const grouped = holidays.reduce(
      (acc, holiday) => {
        const date = new Date(holiday.date + "T00:00:00");

        const monthKey = `${date.getFullYear()}-${String(
          date.getMonth() + 1,
        ).padStart(2, "0")}`;
        const monthName = date.toLocaleDateString("es-ES", {
          year: "numeric",
          month: "long",
        });

        if (!acc[monthKey]) {
          acc[monthKey] = {
            monthName: monthName.charAt(0).toUpperCase() + monthName.slice(1),
            holidays: [],
          };
        }
        acc[monthKey].holidays.push(holiday);
        return acc;
      },
      {} as Record<string, { monthName: string; holidays: HolidayInterface[] }>,
    );

    return Object.values(grouped).sort((a, b) => {
      const dateA = new Date(a.holidays[0].date + "T00:00:00");
      const dateB = new Date(b.holidays[0].date + "T00:00:00");
      return dateA.getTime() - dateB.getTime();
    });
  }

  private _loadInitialData() {
    this._loadHolidays();
    this._loadPermissionsFlags();
  }

  private _loadPermissionsFlags() {
    this.canCreate.set(
      this.accessControlService.can(
        PermissionsEnum.CONFIGURATION,
        ActionsEnum.CREATE,
      ),
    );
    this.canRead.set(
      this.accessControlService.can(
        PermissionsEnum.CONFIGURATION,
        ActionsEnum.READ,
      ),
    );
    this.canUpdate.set(
      this.accessControlService.can(
        PermissionsEnum.CONFIGURATION,
        ActionsEnum.UPDATE,
      ),
    );
  }

  private _loadHolidays() {
    this.holidayService
      .getAll()
      .pipe(takeUntil(this._destroy$))
      .subscribe(
        (response: ApiResponseInterface<HolidayInterface[]>) => {
          const holidays = response.data ?? [];
          this.holidays.set(holidays);
          this.loaderService.hide();
        },
        (error) => {
          console.error("Error al cargar los feriados:", error);
          this.loaderService.hide();
          this.snackbarService.openSnackbar(
            "Error al cargar feriados.",
            6000,
            "center",
            "top",
            SnackbarTypeEnum.Error,
          );
        },
      );
  }
}
