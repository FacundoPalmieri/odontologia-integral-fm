import { Component, inject, signal, OnDestroy } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatCardModule } from "@angular/material/card";
import { MatTooltipModule } from "@angular/material/tooltip";
import { MatButtonModule } from "@angular/material/button";
import { MatChipsModule } from "@angular/material/chips";
import { Subject, takeUntil } from "rxjs";
import { IconsModule } from "../../../../../utils/tabler-icons.module";
import { MatToolbarModule } from "@angular/material/toolbar";
import { PageToolbarComponent } from "../../../../components/page-toolbar/page-toolbar.component";
import { LoaderService } from "../../../../../services/loader.service";
import { AccessControlService } from "../../../../../services/access-control.service";
import { SnackbarService } from "../../../../../services/snackbar.service";
import {
  ActionsEnum,
  PermissionsEnum,
} from "../../../../../utils/enums/permissions.enum";
import {
  ApiResponseInterface,
  PagedDataInterface,
} from "../../../../../domain/interfaces/api-response.interface";
import { SnackbarTypeEnum } from "../../../../../utils/enums/snackbar-type.enum";
import { HolidayService } from "../../../../../services/holiday.service";
import { HolidayInterface } from "../../../../../domain/interfaces/holiday.interface";
import { MatDialog } from "@angular/material/dialog";
import { HolidayEditDialogComponent } from "../holiday-edit-dialog/holiday-edit-dialog.component";

@Component({
  selector: "app-holidays-list",
  templateUrl: "./holidays-list.component.html",
  standalone: true,
  imports: [
    CommonModule,
    IconsModule,
    MatToolbarModule,
    PageToolbarComponent,
    MatCardModule,
    MatTooltipModule,
    MatButtonModule,
    MatChipsModule,
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
    // TODO: Implementar navegación a crear feriado
    console.log("Crear feriado");
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
              SnackbarTypeEnum.Success
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
    const grouped = holidays.reduce((acc, holiday) => {
      const date = new Date(holiday.date + "T00:00:00");

      const monthKey = `${date.getFullYear()}-${String(
        date.getMonth() + 1
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
    }, {} as Record<string, { monthName: string; holidays: HolidayInterface[] }>);

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
        ActionsEnum.CREATE
      )
    );
    this.canRead.set(
      this.accessControlService.can(
        PermissionsEnum.CONFIGURATION,
        ActionsEnum.READ
      )
    );
    this.canUpdate.set(
      this.accessControlService.can(
        PermissionsEnum.CONFIGURATION,
        ActionsEnum.UPDATE
      )
    );
  }

  private _loadHolidays() {
    this.holidayService
      .getAll(0, 1000, "date", "asc") // Cargar todos los feriados - temporal hasta quitar pagincación del lado del backend
      .pipe(takeUntil(this._destroy$))
      .subscribe(
        (
          response: ApiResponseInterface<PagedDataInterface<HolidayInterface[]>>
        ) => {
          const holidays = response.data?.content ?? [];
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
            SnackbarTypeEnum.Error
          );
        }
      );
  }
}
