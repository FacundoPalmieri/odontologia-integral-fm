import { Component, inject, OnDestroy, signal, ViewChild } from "@angular/core";
import { MatDatepicker } from "@angular/material/datepicker";
import {
  MatDialogModule,
  MatDialogRef,
  MAT_DIALOG_DATA,
} from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatNativeDateModule } from "@angular/material/core";
import { MatButtonModule } from "@angular/material/button";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatIconModule } from "@angular/material/icon";
import { MatChipsModule } from "@angular/material/chips";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import {
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { IconsModule } from "../../../../utils/tabler-icons.module";
import { DayEnum, RecurrenceEnum } from "../../../../utils/enums/day.enum";
import { CalendarService } from "../../../../services/calendar.service";
import {
  CalendarLockInterface,
  CalendarLockTypeInterface,
} from "../../../../domain/interfaces/calendar.interface";
import { Subject, takeUntil } from "rxjs";
import { ApiResponseInterface } from "../../../../domain/interfaces/api-response.interface";

@Component({
  selector: "app-create-calendar-lock-dialog",
  templateUrl: "./create-calendar-lock-dialog.component.html",
  standalone: true,
  imports: [
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatButtonModule,
    MatCheckboxModule,
    MatIconModule,
    MatChipsModule,
    MatAutocompleteModule,
    FormsModule,
    ReactiveFormsModule,
    IconsModule,
  ],
})
export class CreateCalendarLockDialogComponent implements OnDestroy {
  private readonly _destroy$ = new Subject<void>();
  private readonly calendarService = inject(CalendarService);
  dialogRef = inject(MatDialogRef<CreateCalendarLockDialogComponent>);
  data = inject<{ personId: number }>(MAT_DIALOG_DATA, { optional: false });

  @ViewChild("startPicker") startPicker!: MatDatepicker<Date>;
  @ViewChild("endPicker") endPicker!: MatDatepicker<Date>;

  // EventForm
  eventForm = new FormGroup({
    calendarLockType: new FormControl<CalendarLockTypeInterface | null>(null, [
      Validators.required,
    ]),
    days: new FormControl<DayEnum[]>([], [Validators.required]),
    startDate: new FormControl<Date | null>(null, [Validators.required]),
    endDate: new FormControl<Date | null>(null, [Validators.required]),
    startTime: new FormControl<string>("", [Validators.required]),
    endTime: new FormControl<string>("", [Validators.required]),
    recurrence: new FormControl<RecurrenceEnum>(RecurrenceEnum.NONE, [
      Validators.required,
    ]),
    observation: new FormControl<string>(""),
  });

  // Calendar lock types
  calendarLockTypes = signal<CalendarLockTypeInterface[]>([]);

  // Date range
  startDate: Date | null = null;
  endDate: Date | null = null;

  // Time range
  isAllDay = false;

  // Days of week
  selectedDays: DayEnum[] = [];
  allDaysSelected = false;

  // All day options with labels
  private allWeekDays = [
    { value: DayEnum.MONDAY, label: "Lunes" },
    { value: DayEnum.TUESDAY, label: "Martes" },
    { value: DayEnum.WEDNESDAY, label: "Miércoles" },
    { value: DayEnum.THURSDAY, label: "Jueves" },
    { value: DayEnum.FRIDAY, label: "Viernes" },
    { value: DayEnum.SATURDAY, label: "Sábado" },
    { value: DayEnum.SUNDAY, label: "Domingo" },
  ];

  // Available days based on date range
  availableDays: Array<{ value: DayEnum; label: string }> = [];

  // Professional
  selectedProfessional: any = null;

  // Flag to show/hide time and days fields
  get shouldShowTimeAndDays(): boolean {
    const lockType = this.eventForm.get("calendarLockType")?.value;
    if (!lockType) return true;

    const hiddenTypes = ["Enfermedad", "Vacaciones"];
    return !hiddenTypes.includes(lockType.name);
  }

  constructor() {
    this._getCalendarLockTypes();
    this.updateAvailableDays();

    // Listen to lock type changes to update validators
    this.eventForm
      .get("calendarLockType")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe(() => {
        this.updateFieldValidators();
      });
  }

  private updateFieldValidators(): void {
    const startTimeControl = this.eventForm.get("startTime");
    const endTimeControl = this.eventForm.get("endTime");
    const daysControl = this.eventForm.get("days");
    const recurrenceControl = this.eventForm.get("recurrence");

    if (this.shouldShowTimeAndDays) {
      // Add required validators
      startTimeControl?.setValidators([Validators.required]);
      endTimeControl?.setValidators([Validators.required]);
      daysControl?.setValidators([Validators.required]);
    } else {
      // Remove validators
      startTimeControl?.clearValidators();
      endTimeControl?.clearValidators();
      daysControl?.clearValidators();

      // Set all-day times and NONE recurrence for Vacaciones or Enfermedad
      startTimeControl?.setValue("00:00:00");
      endTimeControl?.setValue("23:59:59");
      recurrenceControl?.setValue(RecurrenceEnum.NONE);

      // Set all days between start and end date
      this.setAllDaysInRange();
    }

    // Update validity
    startTimeControl?.updateValueAndValidity();
    endTimeControl?.updateValueAndValidity();
    daysControl?.updateValueAndValidity();
  }

  private setAllDaysInRange(): void {
    const startDate = this.eventForm.get("startDate")?.value;
    const endDate = this.eventForm.get("endDate")?.value;

    if (!startDate || !endDate) {
      this.eventForm.get("days")?.setValue([]);
      this.selectedDays = [];
      this.allDaysSelected = false;
      return;
    }

    const daysInRange = new Set<DayEnum>();
    const currentDate = new Date(startDate);
    const end = new Date(endDate);

    // Iterate through each day in the range
    while (currentDate <= end) {
      const dayOfWeek = currentDate.getDay();
      const dayEnum = this.getDayEnumFromDayOfWeek(dayOfWeek);
      daysInRange.add(dayEnum);
      currentDate.setDate(currentDate.getDate() + 1);
    }

    // Set all days in the range
    this.selectedDays = Array.from(daysInRange);
    this.eventForm.get("days")?.setValue(this.selectedDays);
    this.allDaysSelected =
      this.selectedDays.length === this.availableDays.length;
  }

  onStartDateChange(): void {
    this.startDate = this.eventForm.get("startDate")?.value || null;
    this.updateAvailableDays();
    this.validateSelectedDays();

    // If it's Vacaciones or Enfermedad, update days automatically
    if (!this.shouldShowTimeAndDays) {
      this.setAllDaysInRange();
    }
  }

  onEndDateChange(): void {
    this.endDate = this.eventForm.get("endDate")?.value || null;
    this.updateAvailableDays();
    this.validateSelectedDays();

    // If it's Vacaciones or Enfermedad, update days automatically
    if (!this.shouldShowTimeAndDays) {
      this.setAllDaysInRange();
    }
  }

  openStartDatePicker(): void {
    this.startPicker.open();
  }

  openEndDatePicker(): void {
    this.endPicker.open();
  }

  private _getCalendarLockTypes(): void {
    this.calendarService
      .getCalendarLockTypes()
      .pipe(takeUntil(this._destroy$))
      .subscribe(
        (response: ApiResponseInterface<CalendarLockTypeInterface[]>) => {
          this.calendarLockTypes.set(response.data);
        }
      );
  }

  private updateAvailableDays(): void {
    if (!this.startDate || !this.endDate) {
      this.availableDays = [];
      return;
    }

    const daysInRange = new Set<DayEnum>();
    const currentDate = new Date(this.startDate);
    const endDate = new Date(this.endDate);

    // Iterate through each day in the range
    while (currentDate <= endDate) {
      const dayOfWeek = currentDate.getDay();
      const dayEnum = this.getDayEnumFromDayOfWeek(dayOfWeek);
      daysInRange.add(dayEnum);
      currentDate.setDate(currentDate.getDate() + 1);
    }

    // Filter weekDays to only include days in range
    this.availableDays = this.allWeekDays.filter((day) =>
      daysInRange.has(day.value)
    );
  }

  private getDayEnumFromDayOfWeek(dayOfWeek: number): DayEnum {
    // JavaScript Date.getDay() returns: 0=Sunday, 1=Monday, ..., 6=Saturday
    const dayMap: { [key: number]: DayEnum } = {
      0: DayEnum.SUNDAY,
      1: DayEnum.MONDAY,
      2: DayEnum.TUESDAY,
      3: DayEnum.WEDNESDAY,
      4: DayEnum.THURSDAY,
      5: DayEnum.FRIDAY,
      6: DayEnum.SATURDAY,
    };
    return dayMap[dayOfWeek];
  }

  private validateSelectedDays(): void {
    // Remove selected days that are no longer available
    const availableDayValues = this.availableDays.map((day) => day.value);
    this.selectedDays = this.selectedDays.filter((day) =>
      availableDayValues.includes(day)
    );
    this.allDaysSelected =
      this.selectedDays.length === this.availableDays.length &&
      this.availableDays.length > 0;
  }

  toggleAllDays(event: any): void {
    this.allDaysSelected = event.checked;
    if (this.allDaysSelected) {
      this.selectedDays = this.availableDays.map((day) => day.value);
    } else {
      this.selectedDays = [];
    }
    this.eventForm.get("days")?.setValue(this.selectedDays);
  }

  toggleDay(day: DayEnum): void {
    const index = this.selectedDays.indexOf(day);
    if (index > -1) {
      this.selectedDays.splice(index, 1);
    } else {
      this.selectedDays.push(day);
    }
    this.allDaysSelected =
      this.selectedDays.length === this.availableDays.length &&
      this.availableDays.length > 0;
    this.eventForm.get("days")?.setValue(this.selectedDays);
  }

  isDaySelected(day: DayEnum): boolean {
    return this.selectedDays.includes(day);
  }

  onAllDayChange(isAllDay: boolean): void {
    if (isAllDay) {
      // Si se selecciona "Todo el día", establecer horarios completos
      this.eventForm.get("startTime")?.setValue("00:00:00");
      this.eventForm.get("endTime")?.setValue("23:59:59");
    } else {
      // Si se deselecciona, limpiar los valores para que el usuario los ingrese
      this.eventForm.get("startTime")?.setValue("");
      this.eventForm.get("endTime")?.setValue("");
    }
  }

  onCancel() {
    this.dialogRef.close();
  }

  save() {
    if (this.eventForm.valid) {
      const formValue = this.eventForm.value;

      // Determinar si debe usar horarios de todo el día
      const useAllDayTimes = this.isAllDay || !this.shouldShowTimeAndDays;

      // Construir el objeto CalendarLockInterface
      const calendarLock: CalendarLockInterface = {
        calendarLockType: formValue.calendarLockType!,
        days: formValue.days || [],
        recurrence: formValue.recurrence || RecurrenceEnum.NONE,
        startDate: formValue.startDate!,
        endDate: formValue.endDate!,
        startTime: useAllDayTimes ? "00:00:00" : formValue.startTime || "",
        endTime: useAllDayTimes ? "23:59:59" : formValue.endTime || "",
        observation: formValue.observation || "",
      };

      // Llamar al servicio para crear el bloqueo
      this.calendarService
        .createCalendarLock(calendarLock, this.data.personId)
        .pipe(takeUntil(this._destroy$))
        .subscribe({
          next: (response) => {
            this.dialogRef.close(response.data);
          },
        });
    } else {
      // Mark all fields as touched to show validation errors
      Object.keys(this.eventForm.controls).forEach((key) => {
        this.eventForm.get(key)?.markAsTouched();
      });
    }
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }
}
