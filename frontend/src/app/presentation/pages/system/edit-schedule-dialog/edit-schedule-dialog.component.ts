import { Component, inject } from "@angular/core";
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
import { ScheduleInterface } from "../../../../domain/interfaces/schedule.interface";
import { ScheduleUpdateDtoInterface } from "../../../../domain/dto/schedule.dto";

@Component({
  selector: "app-edit-schedule-dialog",
  templateUrl: "./edit-schedule-dialog.component.html",
  imports: [
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatInputModule,
  ],
})
export class EditScheduleDialogComponent {
  scheduleForm: FormGroup;
  data: { schedule: ScheduleInterface };
  dialogRef = inject(MatDialogRef<EditScheduleDialogComponent>);

  constructor() {
    this.data = inject(MAT_DIALOG_DATA);
    this.scheduleForm = new FormGroup({
      id: new FormControl<number>(this.data.schedule.id, [Validators.required]),
      name: new FormControl<string>(this.data.schedule.name, [
        Validators.required,
      ]),
      label: new FormControl<string>(this.data.schedule.label, [
        Validators.required,
      ]),
      cron: new FormControl<string>(this.data.schedule.cron, [
        Validators.required,
      ]),
    });
  }

  scheduleData(): ScheduleUpdateDtoInterface {
    const scheduleData: ScheduleUpdateDtoInterface = {
      id: this.scheduleForm.value.id,
      cronExpression: this.scheduleForm.value.cron,
    };

    return scheduleData;
  }
}
