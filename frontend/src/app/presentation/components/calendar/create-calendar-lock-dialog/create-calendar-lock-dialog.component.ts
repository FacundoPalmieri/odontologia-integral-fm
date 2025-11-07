import { Component, inject, ViewChild } from "@angular/core";
import { MatDatepicker } from "@angular/material/datepicker";
import { MatDialogModule, MatDialogRef } from "@angular/material/dialog";
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
import { FormsModule } from "@angular/forms";
import { IconsModule } from "../../../../utils/tabler-icons.module";
import { DayEnum } from "../../../../utils/enums/day.enum";

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
    IconsModule,
  ],
})
export class CreateCalendarLockDialogComponent {
  dialogRef = inject(MatDialogRef<CreateCalendarLockDialogComponent>);

  @ViewChild("startPicker") startPicker!: MatDatepicker<Date>;
  @ViewChild("endPicker") endPicker!: MatDatepicker<Date>;

  // Date range
  startDate: Date | null = null;
  endDate: Date | null = null;

  // Time range
  isAllDay = false;
  startTime: string = "";
  endTime: string = "";

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

  constructor() {
    this.updateAvailableDays();
  }

  professionals = [
    { id: 1, name: "Dr. Ana Martínez", specialty: "Odontología General" },
    { id: 2, name: "Dr. Pedro Rodríguez", specialty: "Ortodoncia" },
    { id: 3, name: "Dr. Laura Sánchez", specialty: "Periodoncia" },
  ];

  onStartDateChange(): void {
    this.updateAvailableDays();
    this.validateSelectedDays();
  }

  onEndDateChange(): void {
    this.updateAvailableDays();
    this.validateSelectedDays();
  }

  openStartDatePicker(): void {
    this.startPicker.open();
  }

  openEndDatePicker(): void {
    this.endPicker.open();
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
  }

  isDaySelected(day: DayEnum): boolean {
    return this.selectedDays.includes(day);
  }

  onCancel() {
    this.dialogRef.close();
  }

  onSave() {
    this.dialogRef.close();
  }
}
