import { Component, inject, signal } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatIconModule } from "@angular/material/icon";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { HolidayTypeFactory } from "../../../../../features/holidays/utils/factories/holiday-type.factory";
import {
  HolidayInterface,
  HolidayTypeInterface,
} from "../../../domain/interfaces/holiday.interface";

@Component({
  selector: "app-holiday-edit-dialog",
  templateUrl: "./holiday-edit-dialog.component.html",
  standalone: true,
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
    MatSelectModule,
    IconsModule,
    MatIconModule,
    MatDatepickerModule,
  ],
})
export class HolidayEditDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<HolidayEditDialogComponent>);

  holidayForm: FormGroup = new FormGroup({});
  data: { holiday: HolidayInterface };
  holidaysTypes = signal<HolidayTypeInterface[]>(
    HolidayTypeFactory.createHolidayTypes(),
  );

  constructor() {
    this.data = inject(MAT_DIALOG_DATA);
    this._loadForm();
  }

  private _loadForm() {
    const holidayDate =
      typeof this.data.holiday.date === "string"
        ? new Date(this.data.holiday.date + "T00:00:00")
        : this.data.holiday.date;

    this.holidayForm = new FormGroup({
      id: new FormControl<number>(this.data.holiday.id, [Validators.required]),
      date: new FormControl<Date>(holidayDate, [Validators.required]),
      type: new FormControl<HolidayTypeInterface>(this.data.holiday.type, [
        Validators.required,
      ]),
      name: new FormControl<string>(this.data.holiday.name, [
        Validators.required,
      ]),
    });
  }

  save() {
    const holiday: HolidayInterface = {
      id: this.holidayForm.value.id,
      name: this.holidayForm.value.name,
      date: this.holidayForm.value.date,
      type: this.holidayForm.value.type,
    };

    this.dialogRef.close(holiday);
  }

  compareHolidayTypes(
    type1: HolidayTypeInterface,
    type2: HolidayTypeInterface,
  ): boolean {
    return type1 && type2 ? type1.value === type2.value : type1 === type2;
  }
}
