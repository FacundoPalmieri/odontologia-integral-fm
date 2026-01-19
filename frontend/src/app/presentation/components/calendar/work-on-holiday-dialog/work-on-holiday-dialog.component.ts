import { Component, inject, Inject, OnInit } from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { CommonModule } from "@angular/common";
import { IconsModule } from "../../../../utils/tabler-icons.module";
import { HolidayInterface } from "../../../../domain/interfaces/calendar.interface";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { MatCheckboxModule } from "@angular/material/checkbox";
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from "@angular/forms";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { HolidayWorkConfigDtoInterface } from "../../../../domain/dto/holiday.dto";
import { AuthService } from "../../../../services/auth.service";
import { DentistService } from "../../../../services/dentist.service";
import { SnackbarService } from "../../../../services/snackbar.service";
import { SnackbarTypeEnum } from "../../../../utils/enums/snackbar-type.enum";

@Component({
  selector: "app-work-on-holiday-dialog",
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCheckboxModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
    IconsModule,
  ],
  templateUrl: "./work-on-holiday-dialog.component.html",
})
export class WorkOnHolidayDialogComponent implements OnInit {
  holiday: HolidayInterface;
  date: Date;
  workForm!: FormGroup;
  isLoading = false;
  isSaving = false;

  hours = [6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23];
  minutes = [0, 15, 30, 45];
  durations = [30, 60];

  private fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly dentistService = inject(DentistService);
  private readonly snackbarService = inject(SnackbarService);

  constructor(
    @Inject(MAT_DIALOG_DATA)
    public data: { holiday: HolidayInterface; date: Date },
    private dialogRef: MatDialogRef<WorkOnHolidayDialogComponent>,
  ) {
    this.holiday = data.holiday;
    this.date = data.date;
  }

  ngOnInit(): void {
    this.initForm();
  }

  /**
   * Initialize form with same structure as dentist availability
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
      appointmentDuration: [
        30,
        [Validators.required, Validators.min(5), Validators.max(120)],
      ],
      hasBreak: [false],
      breakStartTime: this.fb.group({
        hour: [12],
        minute: [0],
      }),
      breakEndTime: this.fb.group({
        hour: [13],
        minute: [0],
      }),
    });

    // Deshabilitar campos de descanso si no está marcado
    this.workForm.get("hasBreak")?.valueChanges.subscribe((hasBreak) => {
      if (hasBreak) {
        this.workForm.get("breakStartTime")?.enable();
        this.workForm.get("breakEndTime")?.enable();
      } else {
        this.workForm.get("breakStartTime")?.disable();
        this.workForm.get("breakEndTime")?.disable();
      }
    });

    // Inicialmente deshabilitar campos de descanso
    this.workForm.get("breakStartTime")?.disable();
    this.workForm.get("breakEndTime")?.disable();
  }

  /**
   * Format date to display
   */
  formatDate(date: Date): string {
    return date.toLocaleDateString("es-AR", {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  }

  /**
   * Track by function for ngFor optimization
   */
  trackByValue(index: number, value: number): number {
    return value;
  }

  /**
   * Build time string in HH:mm format
   */
  private buildTimeString(hour: number, minute: number): string {
    const hourStr = hour.toString().padStart(2, "0");
    const minuteStr = minute.toString().padStart(2, "0");
    return `${hourStr}:${minuteStr}`;
  }

  /**
   * Save availability configuration
   */
  saveAvailability(): void {
    if (this.workForm.invalid) {
      return;
    }

    // Get user ID from auth service
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

    // Build DTO matching API structure
    const dto: HolidayWorkConfigDtoInterface = {
      year: this.date.getFullYear(),
      idHoliday: this.holiday.id,
      startTime: this.buildTimeString(
        formValue.startTime.hour,
        formValue.startTime.minute,
      ),
      endTime: this.buildTimeString(
        formValue.endTime.hour,
        formValue.endTime.minute,
      ),
      appointmentDuration: formValue.appointmentDuration,
      breakStartTime: formValue.hasBreak
        ? this.buildTimeString(
            formValue.breakStartTime.hour,
            formValue.breakStartTime.minute,
          )
        : null,
      breakEndTime: formValue.hasBreak
        ? this.buildTimeString(
            formValue.breakEndTime.hour,
            formValue.breakEndTime.minute,
          )
        : null,
    };

    console.log(dto);

    // Call service to save configuration
    this.dentistService
      .saveAvailabilityHoliday(userData.idUser, dto)
      .subscribe({
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
            configured: true,
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
