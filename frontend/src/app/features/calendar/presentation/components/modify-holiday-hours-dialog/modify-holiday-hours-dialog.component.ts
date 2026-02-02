import { Component, inject, Inject, OnInit } from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { CommonModule } from "@angular/common";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { IconsModule } from "../../../../../core/modules/tabler-icons.module";
import { HolidayInterface } from "../../../../holidays/domain/interfaces/holiday.interface";
import { DentistHolidayService } from "../../../services/dentist-holiday.service";
import { AuthService } from "../../../../auth/services/auth.service";
import { SnackbarService } from "../../../../../shared/services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../../shared/utils/enums/snackbar-type.enum";
import { HolidayUpdateAvailabilityDto } from "../../../domain/dtos/calendar-holiday.dto";

@Component({
  selector: "app-modify-holiday-hours-dialog",
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
    IconsModule,
  ],
  templateUrl: "./modify-holiday-hours-dialog.component.html",
})
export class ModifyHolidayHoursDialogComponent implements OnInit {
  holiday: HolidayInterface;
  date: Date;
  dentistHolidayId: number;
  workForm!: FormGroup;
  isSaving = false;

  hours = [6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23];
  minutes = [0, 15, 30, 45];

  private fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly dentistHolidayService = inject(DentistHolidayService);
  private readonly snackbarService = inject(SnackbarService);

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: {
      holiday: HolidayInterface;
      date: Date;
      dentistHolidayId: number;
    },
    private dialogRef: MatDialogRef<ModifyHolidayHoursDialogComponent>,
  ) {
    this.holiday = data.holiday;
    this.date = data.date;
    this.dentistHolidayId = data.dentistHolidayId;
  }

  ngOnInit(): void {
    this.initForm();
  }

  /**
   * Initialize form with time fields only
   */
  initForm(): void {
    this.workForm = this.fb.group({
      startTime: this.fb.group({
        hour: [9, Validators.required],
        minute: [0, Validators.required],
      }),
      endTime: this.fb.group({
        hour: [17, Validators.required],
        minute: [0, Validators.required],
      }),
    });
  }

  /**
   * Track by function for ngFor optimization
   */
  trackByValue(index: number, value: number): number {
    return value;
  }

  /**
   * Update availability with new time range
   */
  updateAvailability(): void {
    if (this.workForm.invalid) {
      return;
    }

    const userData = this.authService.getUserData();
    if (!userData) {
      this.snackbarService.openSnackbar(
        "Error: Usuario no autenticado",
        6000,
        "center",
        "bottom",
        SnackbarTypeEnum.Error,
      );
      return;
    }

    this.isSaving = true;

    const formValue = this.workForm.getRawValue();

    const startTimeStr = `${formValue.startTime.hour.toString().padStart(2, "0")}:${formValue.startTime.minute.toString().padStart(2, "0")}`;
    const endTimeStr = `${formValue.endTime.hour.toString().padStart(2, "0")}:${formValue.endTime.minute.toString().padStart(2, "0")}`;

    const dto: HolidayUpdateAvailabilityDto = {
      idDentistHoliday: this.dentistHolidayId,
      startTime: startTimeStr,
      endTime: endTimeStr,
      enabled: true,
    };

    this.dentistHolidayService.update(userData.idUser, dto).subscribe({
      next: (response) => {
        this.isSaving = false;
        this.snackbarService.openSnackbar(
          response.message,
          6000,
          "center",
          "top",
          SnackbarTypeEnum.Success,
        );
        this.dialogRef.close({
          updated: true,
          data: response.data,
        });
      },
      error: (error) => {
        this.isSaving = false;
      },
    });
  }

  /**
   * Cancel and close dialog
   */
  cancel(): void {
    this.dialogRef.close(null);
  }
}
