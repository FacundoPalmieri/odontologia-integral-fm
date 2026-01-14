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
import { LockTypeModeEnum } from "../../../../utils/enums/calendar/lock-type-mode.enum";
import { CalendarService } from "../../../../services/calendar.service";
import {
  CalendarLockInterface,
  CalendarLockTypeInterface,
  CalendarLockTypeModeInterface,
} from "../../../../domain/interfaces/calendar.interface";
import { Subject, takeUntil } from "rxjs";
import { ApiResponseInterface } from "../../../../domain/interfaces/api-response.interface";
import { SnackbarService } from "../../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../utils/enums/snackbar-type.enum";

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
  private readonly snackbarService = inject(SnackbarService);
  dialogRef = inject(MatDialogRef<CreateCalendarLockDialogComponent>);
  data = inject<{ personId: number }>(MAT_DIALOG_DATA, { optional: false });

  @ViewChild("startPicker") startPicker!: MatDatepicker<Date>;
  @ViewChild("endPicker") endPicker!: MatDatepicker<Date>;

  // Expose enums to template
  LockTypeModeEnum = LockTypeModeEnum;
  RecurrenceEnum = RecurrenceEnum;

  // EventForm
  eventForm = new FormGroup({
    calendarLockType: new FormControl<CalendarLockTypeInterface | null>(null, [
      Validators.required,
    ]),
    mode: new FormControl<CalendarLockTypeModeInterface | null>(null),
    days: new FormControl<DayEnum[]>([]),
    startDate: new FormControl<Date | null>(null, [Validators.required]),
    endDate: new FormControl<Date | null>(null, [Validators.required]),
    startTime: new FormControl<string>(""),
    endTime: new FormControl<string>(""),
    recurrence: new FormControl<RecurrenceEnum>(RecurrenceEnum.NONE),
    observation: new FormControl<string>(""),
  });

  // Calendar lock types
  calendarLockTypes = signal<CalendarLockTypeInterface[]>([]);

  // Available modes for selected lock type
  availableModes = signal<(CalendarLockTypeModeInterface | string)[]>([]);

  // Current selected mode
  selectedMode = signal<CalendarLockTypeModeInterface | string | null>(null);

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

  // Recurrence options based on mode
  availableRecurrences = signal<
    Array<{ value: RecurrenceEnum; label: string }>
  >([]);

  // Error messages
  endDateRangeError: string | null = null;
  modeSelectionError: string | null = null;

  // Minimum date for date pickers (tomorrow)
  minDate: Date;

  constructor() {
    // Set minimum date to tomorrow
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(0, 0, 0, 0);
    this.minDate = tomorrow;

    this._getCalendarLockTypes();
    this.updateAvailableDays();

    // Listen to lock type changes
    this.eventForm
      .get("calendarLockType")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe((lockType) => {
        this.onLockTypeChange(lockType);
      });

    // Listen to mode changes
    this.eventForm
      .get("mode")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe((mode) => {
        this.onModeChange(mode);
      });

    // Listen to date changes
    this.eventForm
      .get("startDate")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe(() => {
        this.onStartDateChange();
      });

    this.eventForm
      .get("endDate")
      ?.valueChanges.pipe(takeUntil(this._destroy$))
      .subscribe(() => {
        this.onEndDateChange();
      });
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

  onLockTypeChange(lockType: CalendarLockTypeInterface | null): void {
    if (!lockType) {
      this.availableModes.set([]);
      this.selectedMode.set(null);
      this.eventForm.get("mode")?.setValue(null);
      return;
    }

    // Set available modes
    this.availableModes.set(lockType.modes || []);

    // If only one mode, auto-select it
    if (lockType.modes && lockType.modes.length === 1) {
      const singleMode = lockType.modes[0];

      // Handle both string and object formats from API
      const modeValue =
        typeof singleMode === "string" ? (singleMode as any) : singleMode;

      this.eventForm.get("mode")?.setValue(modeValue);
      this.selectedMode.set(modeValue);
      this.onModeChange(modeValue);
    } else if (lockType.modes && lockType.modes.length > 1) {
      // Multiple modes: require user selection
      this.eventForm.get("mode")?.setValidators([Validators.required]);
      this.eventForm.get("mode")?.updateValueAndValidity();
      // Reset form until mode is selected
      this.resetFormForModeSelection();
    } else {
      // No modes defined
      console.warn("No modes defined for lock type:", lockType);
      this.selectedMode.set(null);
      this.resetFormForModeSelection();
    }
  }

  onModeChange(mode: CalendarLockTypeModeInterface | string | null): void {
    if (!mode) {
      this.selectedMode.set(null);
      return;
    }

    this.selectedMode.set(mode as any);

    // Extract mode name - handle both string and object formats
    const modeName =
      typeof mode === "string"
        ? (mode as LockTypeModeEnum)
        : (mode.name as LockTypeModeEnum);

    this.adaptFormToMode(modeName);
  }

  private resetFormForModeSelection(): void {
    // Clear all fields except lock type and mode
    this.eventForm.patchValue({
      days: [],
      startDate: null,
      endDate: null,
      startTime: "",
      endTime: "",
      recurrence: RecurrenceEnum.NONE,
    });

    // Clear validators
    this.eventForm.get("days")?.clearValidators();
    this.eventForm.get("startTime")?.clearValidators();
    this.eventForm.get("endTime")?.clearValidators();
    this.eventForm.get("recurrence")?.clearValidators();

    this.eventForm.get("days")?.updateValueAndValidity();
    this.eventForm.get("startTime")?.updateValueAndValidity();
    this.eventForm.get("endTime")?.updateValueAndValidity();
    this.eventForm.get("recurrence")?.updateValueAndValidity();
  }

  private adaptFormToMode(mode: LockTypeModeEnum): void {
    // Reset validators
    this.eventForm.get("days")?.clearValidators();
    this.eventForm.get("startTime")?.clearValidators();
    this.eventForm.get("endTime")?.clearValidators();
    this.eventForm.get("recurrence")?.clearValidators();

    switch (mode) {
      case LockTypeModeEnum.POINTUAL:
        this.setupPointualMode();
        break;
      case LockTypeModeEnum.DAYS_IN_RANGE_NO_RECURRENCE:
        this.setupDaysInRangeNoRecurrenceMode();
        break;
      case LockTypeModeEnum.DAILY_CONTINUOUS:
        this.setupDailyContinuousMode();
        break;
      case LockTypeModeEnum.RECURRENT_PATTERN:
        this.setupRecurrentPatternMode();
        break;
    }
  }

  private setupPointualMode(): void {
    // POINTUAL: startDate == endDate, no days, no recurrence, times required
    this.eventForm.get("startTime")?.setValidators([Validators.required]);
    this.eventForm.get("endTime")?.setValidators([Validators.required]);
    this.eventForm.get("recurrence")?.setValue(RecurrenceEnum.NONE);

    // Clear days
    this.eventForm.get("days")?.setValue([]);
    this.selectedDays = [];

    // Update validity
    this.eventForm.get("days")?.updateValueAndValidity();
    this.eventForm.get("startTime")?.updateValueAndValidity();
    this.eventForm.get("endTime")?.updateValueAndValidity();
    this.eventForm.get("recurrence")?.updateValueAndValidity();

    // Set available recurrences
    this.availableRecurrences.set([]);
  }

  private setupDaysInRangeNoRecurrenceMode(): void {
    // DAYS_IN_RANGE_NO_RECURRENCE: days required, no recurrence, times required, max 7 days range
    this.eventForm.get("days")?.setValidators([Validators.required]);
    this.eventForm.get("startTime")?.setValidators([Validators.required]);
    this.eventForm.get("endTime")?.setValidators([Validators.required]);
    this.eventForm.get("recurrence")?.setValue(RecurrenceEnum.NONE);

    // Update validity
    this.eventForm.get("days")?.updateValueAndValidity();
    this.eventForm.get("startTime")?.updateValueAndValidity();
    this.eventForm.get("endTime")?.updateValueAndValidity();
    this.eventForm.get("recurrence")?.updateValueAndValidity();

    // Set available recurrences
    this.availableRecurrences.set([]);
  }

  private setupDailyContinuousMode(): void {
    // DAILY_CONTINUOUS: no days, recurrence DAILY, times 00:00 - 23:59
    this.eventForm.get("recurrence")?.setValue(RecurrenceEnum.DAILY);
    this.eventForm.get("startTime")?.setValue("00:00");
    this.eventForm.get("endTime")?.setValue("23:59");

    // Clear days
    this.eventForm.get("days")?.setValue([]);
    this.selectedDays = [];

    // Update validity
    this.eventForm.get("days")?.updateValueAndValidity();
    this.eventForm.get("startTime")?.updateValueAndValidity();
    this.eventForm.get("endTime")?.updateValueAndValidity();
    this.eventForm.get("recurrence")?.updateValueAndValidity();

    // Set available recurrences
    this.availableRecurrences.set([]);
  }

  private setupRecurrentPatternMode(): void {
    // RECURRENT_PATTERN: days required, recurrence required (not NONE), times required
    this.eventForm.get("days")?.setValidators([Validators.required]);
    this.eventForm.get("startTime")?.setValidators([Validators.required]);
    this.eventForm.get("endTime")?.setValidators([Validators.required]);
    this.eventForm.get("recurrence")?.setValidators([Validators.required]);

    // Update validity
    this.eventForm.get("days")?.updateValueAndValidity();
    this.eventForm.get("startTime")?.updateValueAndValidity();
    this.eventForm.get("endTime")?.updateValueAndValidity();
    this.eventForm.get("recurrence")?.updateValueAndValidity();

    // Calculate available recurrences based on date range
    this.updateAvailableRecurrences();
  }

  private updateAvailableRecurrences(): void {
    const startDate = this.eventForm.get("startDate")?.value;
    const endDate = this.eventForm.get("endDate")?.value;

    // If no date range, clear recurrences
    if (!startDate || !endDate) {
      this.availableRecurrences.set([]);
      return;
    }

    const daysDiff = this.getDaysDifference(startDate, endDate);
    const recurrences: Array<{ value: RecurrenceEnum; label: string }> = [];

    // Always include WEEKLY as base option
    recurrences.push({ value: RecurrenceEnum.WEEKLY, label: "Semanal" });

    // Less than 7 days: only WEEKLY (auto-select)
    if (daysDiff < 7) {
      this.availableRecurrences.set(recurrences);
      this.eventForm.get("recurrence")?.setValue(RecurrenceEnum.WEEKLY);
      return;
    }

    // 7-13 days: WEEKLY only (but show selector)
    if (daysDiff >= 7 && daysDiff < 14) {
      this.availableRecurrences.set(recurrences);
      return;
    }

    // 14-30 days: WEEKLY + BIWEEKLY
    if (daysDiff >= 14 && daysDiff < 31) {
      recurrences.push({ value: RecurrenceEnum.BIWEEKLY, label: "Quincenal" });
      this.availableRecurrences.set(recurrences);
      return;
    }

    // 31-364 days: WEEKLY + BIWEEKLY + MONTHLY
    if (daysDiff >= 31 && daysDiff < 365) {
      recurrences.push({ value: RecurrenceEnum.BIWEEKLY, label: "Quincenal" });
      recurrences.push({ value: RecurrenceEnum.MONTHLY, label: "Mensual" });
      this.availableRecurrences.set(recurrences);
      return;
    }

    // 365+ days: All options including YEARLY
    if (daysDiff >= 365) {
      recurrences.push({ value: RecurrenceEnum.BIWEEKLY, label: "Quincenal" });
      recurrences.push({ value: RecurrenceEnum.MONTHLY, label: "Mensual" });
      recurrences.push({ value: RecurrenceEnum.YEARLY, label: "Anual" });
      this.availableRecurrences.set(recurrences);
      return;
    }
  }

  // Helper method to extract mode name from either string or object
  private getModeNameFromValue(
    mode: CalendarLockTypeModeInterface | string | null
  ): LockTypeModeEnum | null {
    if (!mode) return null;
    return typeof mode === "string"
      ? (mode as LockTypeModeEnum)
      : (mode.name as LockTypeModeEnum);
  }

  // Helper methods for template to access mode properties safely
  getModeName(mode: CalendarLockTypeModeInterface | string): string {
    return typeof mode === "string" ? mode : mode.name;
  }

  getModeLabel(mode: CalendarLockTypeModeInterface | string): string {
    if (typeof mode === "string") {
      // Fallback labels for string modes
      const labels: Record<string, string> = {
        POINTUAL: "Bloqueo Puntual",
        DAYS_IN_RANGE_NO_RECURRENCE: "Días Discontinuos sin Recurrencia",
        DAILY_CONTINUOUS: "Bloqueo Continuo Diario",
        RECURRENT_PATTERN: "Patrón Recurrente",
      };
      return labels[mode] || mode;
    }
    return mode.label;
  }

  getModeDescription(
    mode: CalendarLockTypeModeInterface | string | null
  ): string {
    if (!mode) return "Selecciona un modo para continuar";
    if (typeof mode === "string") {
      // Fallback descriptions for string modes
      const descriptions: Record<string, string> = {
        POINTUAL: "Bloqueo único en una fecha específica",
        DAYS_IN_RANGE_NO_RECURRENCE:
          "Bloqueo de días específicos dentro de un rango de fechas, sin recurrencia automática",
        DAILY_CONTINUOUS:
          "Bloqueo diario continuo en un rango de fechas (por ejemplo: vacaciones o licencias)",
        RECURRENT_PATTERN:
          "Bloqueo recurrente basado en un patrón semanal (por ejemplo: todos los lunes y miércoles)",
      };
      return descriptions[mode] || mode;
    }
    return mode.description;
  }

  // Getters for template
  get shouldShowModeSelector(): boolean {
    return this.availableModes().length > 1;
  }

  get shouldShowDaysSelector(): boolean {
    const modeName = this.getModeNameFromValue(this.selectedMode());
    if (!modeName) return false;
    return (
      modeName === LockTypeModeEnum.DAYS_IN_RANGE_NO_RECURRENCE ||
      modeName === LockTypeModeEnum.RECURRENT_PATTERN
    );
  }

  get shouldShowTimeSelector(): boolean {
    const modeName = this.getModeNameFromValue(this.selectedMode());
    if (!modeName) return false;
    return (
      modeName === LockTypeModeEnum.POINTUAL ||
      modeName === LockTypeModeEnum.DAYS_IN_RANGE_NO_RECURRENCE ||
      modeName === LockTypeModeEnum.RECURRENT_PATTERN
    );
  }

  get shouldShowRecurrenceSelector(): boolean {
    const modeName = this.getModeNameFromValue(this.selectedMode());
    if (!modeName) return false;
    return modeName === LockTypeModeEnum.RECURRENT_PATTERN;
  }

  get shouldDisableEndDate(): boolean {
    const modeName = this.getModeNameFromValue(this.selectedMode());
    if (!modeName) return false;
    return modeName === LockTypeModeEnum.POINTUAL;
  }

  get maxDateRange(): number | null {
    const modeName = this.getModeNameFromValue(this.selectedMode());
    if (!modeName) return null;
    return modeName === LockTypeModeEnum.DAYS_IN_RANGE_NO_RECURRENCE ? 7 : null;
  }

  onStartDateChange(): void {
    this.startDate = this.eventForm.get("startDate")?.value || null;

    // Clear previous error
    this.endDateRangeError = null;

    const modeName = this.getModeNameFromValue(this.selectedMode());
    if (!modeName) return;

    // For POINTUAL mode, auto-set endDate to startDate
    if (modeName === LockTypeModeEnum.POINTUAL && this.startDate) {
      this.eventForm.get("endDate")?.setValue(this.startDate);
      this.endDate = this.startDate;
    }

    // For RECURRENT_PATTERN mode, update available recurrences
    if (modeName === LockTypeModeEnum.RECURRENT_PATTERN) {
      this.updateAvailableRecurrences();
    }

    this.updateAvailableDays();
    this.validateSelectedDays();
  }

  onEndDateChange(): void {
    this.endDate = this.eventForm.get("endDate")?.value || null;

    // Clear previous error
    this.endDateRangeError = null;

    const modeName = this.getModeNameFromValue(this.selectedMode());
    if (!modeName) return;

    // Validate max range for DAYS_IN_RANGE_NO_RECURRENCE
    if (
      modeName === LockTypeModeEnum.DAYS_IN_RANGE_NO_RECURRENCE &&
      this.startDate &&
      this.endDate
    ) {
      const daysDiff = this.getDaysDifference(this.startDate, this.endDate);
      if (daysDiff > 6) {
        // Set error message
        this.endDateRangeError = "El rango máximo para este modo es de 7 días";
        // Reset endDate
        this.eventForm.get("endDate")?.setValue(null);
        this.endDate = null;
        return;
      }
    }

    // For RECURRENT_PATTERN mode, update available recurrences and validate
    if (modeName === LockTypeModeEnum.RECURRENT_PATTERN) {
      this.updateAvailableRecurrences();

      // Validate current recurrence against new date range
      const currentRecurrence = this.eventForm.get("recurrence")?.value;
      if (currentRecurrence && this.startDate && this.endDate) {
        const daysDiff = this.getDaysDifference(this.startDate, this.endDate);

        // Check if current recurrence is still valid
        if (currentRecurrence === RecurrenceEnum.BIWEEKLY && daysDiff < 14) {
          this.endDateRangeError =
            "Para recurrencia quincenal, el rango mínimo debe ser de 14 días";
          // Reset to WEEKLY if available
          this.eventForm.get("recurrence")?.setValue(RecurrenceEnum.WEEKLY);
        } else if (
          currentRecurrence === RecurrenceEnum.MONTHLY &&
          daysDiff < 31
        ) {
          this.endDateRangeError =
            "Para recurrencia mensual, el rango mínimo debe ser de 31 días";
          // Reset to WEEKLY or BIWEEKLY depending on range
          if (daysDiff >= 14) {
            this.eventForm.get("recurrence")?.setValue(RecurrenceEnum.BIWEEKLY);
          } else {
            this.eventForm.get("recurrence")?.setValue(RecurrenceEnum.WEEKLY);
          }
        } else if (
          currentRecurrence === RecurrenceEnum.YEARLY &&
          daysDiff < 365
        ) {
          this.endDateRangeError =
            "Para recurrencia anual, el rango mínimo debe ser de 365 días";
          // Reset to appropriate recurrence
          if (daysDiff >= 31) {
            this.eventForm.get("recurrence")?.setValue(RecurrenceEnum.MONTHLY);
          } else if (daysDiff >= 14) {
            this.eventForm.get("recurrence")?.setValue(RecurrenceEnum.BIWEEKLY);
          } else {
            this.eventForm.get("recurrence")?.setValue(RecurrenceEnum.WEEKLY);
          }
        }
      }
    }

    this.updateAvailableDays();
    this.validateSelectedDays();
  }

  private getDaysDifference(start: Date, end: Date): number {
    const diffTime = Math.abs(end.getTime() - start.getTime());
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }

  openStartDatePicker(): void {
    this.startPicker.open();
  }

  openEndDatePicker(): void {
    if (!this.shouldDisableEndDate) {
      this.endPicker.open();
    }
  }

  getEndDateFilter = (date: Date | null): boolean => {
    if (!date || !this.startDate) return true;

    const modeName = this.getModeNameFromValue(this.selectedMode());
    if (!modeName) return true;

    // For DAYS_IN_RANGE_NO_RECURRENCE, limit to 7 days
    if (modeName === LockTypeModeEnum.DAYS_IN_RANGE_NO_RECURRENCE) {
      const maxDate = new Date(this.startDate);
      maxDate.setDate(maxDate.getDate() + 6);
      return date <= maxDate;
    }

    return true;
  };

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
    this.eventForm.get("days")?.setValue(this.selectedDays);
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
      this.eventForm.get("startTime")?.setValue("00:00");
      this.eventForm.get("endTime")?.setValue("23:59");
    } else {
      this.eventForm.get("startTime")?.setValue("");
      this.eventForm.get("endTime")?.setValue("");
    }
  }

  onCancel() {
    this.dialogRef.close();
  }

  save() {
    // Clear previous errors
    this.modeSelectionError = null;

    if (this.eventForm.valid) {
      const formValue = this.eventForm.value;
      const mode = this.selectedMode();

      if (!mode) {
        this.modeSelectionError = "Debe seleccionar un modo de bloqueo";
        return;
      }

      // Extract mode name - handle both string and object formats from API
      const modeName =
        typeof mode === "string"
          ? (mode as LockTypeModeEnum)
          : ((mode as CalendarLockTypeModeInterface).name as LockTypeModeEnum);

      // Build CalendarLockInterface
      const calendarLock: CalendarLockInterface = {
        calendarLockType: formValue.calendarLockType!,
        mode: modeName,
        days: formValue.days || [],
        recurrence: formValue.recurrence || RecurrenceEnum.NONE,
        startDate: formValue.startDate!,
        endDate: formValue.endDate!,
        startTime: formValue.startTime || "00:00",
        endTime: formValue.endTime || "23:59",
        observation: formValue.observation || "",
      };

      // Call service to create lock
      this.calendarService
        .createCalendarLock(calendarLock, this.data.personId)
        .pipe(takeUntil(this._destroy$))
        .subscribe({
          next: (response: ApiResponseInterface<CalendarLockInterface>) => {
            if (response.success) {
              this.snackbarService.openSnackbar(
                response.message,
                6000,
                "center",
                "top",
                SnackbarTypeEnum.Success
              );
              this.dialogRef.close({ success: true, data: response.data });
            }
          },
        });
    } else {
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
