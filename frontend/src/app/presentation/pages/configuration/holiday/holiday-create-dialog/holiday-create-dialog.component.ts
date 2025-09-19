import { Component, inject, signal } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatDialogModule, MatDialogRef } from "@angular/material/dialog";
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
import {
  HolidayInterface,
  HolidayTypeInterface,
} from "../../../../../domain/interfaces/holiday.interface";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { HolidayTypeFactory } from "../../../../../utils/factories/holiday-type.factory";

@Component({
  selector: "app-holiday-create-dialog",
  templateUrl: "./holiday-create-dialog.component.html",
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
export class HolidayCreateDialogComponent {
  private readonly dialogRef = inject(
    MatDialogRef<HolidayCreateDialogComponent>
  );

  holidayForm: FormGroup = new FormGroup({});
  holidaysTypes = signal<HolidayTypeInterface[]>(
    HolidayTypeFactory.createHolidayTypes()
  );

  constructor() {
    this._loadForm();
  }

  private _loadForm() {
    this.holidayForm = new FormGroup({
      date: new FormControl<Date | null>(null, [Validators.required]),
      type: new FormControl<HolidayTypeInterface | null>(null, [
        Validators.required,
      ]),
      name: new FormControl<string>("", [Validators.required]),
    });
  }

  save() {
    if (this.holidayForm.valid) {
      const holiday: Omit<HolidayInterface, "id"> = {
        name: this.holidayForm.value.name,
        date: this.holidayForm.value.date,
        type: this.holidayForm.value.type,
      };

      this.dialogRef.close(holiday);
    }
  }

  cancel() {
    this.dialogRef.close();
  }

  compareHolidayTypes(
    type1: HolidayTypeInterface,
    type2: HolidayTypeInterface
  ): boolean {
    return type1 && type2 ? type1.value === type2.value : type1 === type2;
  }
}
