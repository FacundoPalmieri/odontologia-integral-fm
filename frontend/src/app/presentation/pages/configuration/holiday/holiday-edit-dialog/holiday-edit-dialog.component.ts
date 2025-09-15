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
import { IconsModule } from "../../../../../utils/tabler-icons.module";
import { HolidayInterface } from "../../../../../domain/interfaces/holiday.interface";
import { MatDatepickerModule } from "@angular/material/datepicker";

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
  holidaysTypes = signal<string[]>(["Inamovible", "Puente", "Trasladable"]);

  constructor() {
    this.data = inject(MAT_DIALOG_DATA);
    this._loadForm();
  }

  private _loadForm() {
    this.holidayForm = new FormGroup({
      id: new FormControl<number>(this.data.holiday.id, [Validators.required]),
      date: new FormControl<Date>(this.data.holiday.date, [
        Validators.required,
      ]),
      type: new FormControl<string>(this.data.holiday.type, [
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
}
